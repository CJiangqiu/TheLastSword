package net.the_last_sword.event;

import net.eca.api.EcaAPI;
import net.eca.util.raid.RaidInstance;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.StructureTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.AdvancementEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.the_last_sword.TheLastSwordMod;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.init.ModItems;
import net.the_last_sword.raid.DragonCultRaid;

import java.util.UUID;

//最终之剑任务事件处理器
@Mod.EventBusSubscriber(modid = TheLastSwordMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TheLastSwordQuestHandler {

    //播报前缀复用mod译名（创造标签页）翻译键
    private static final String MOD_NAME_KEY = "item_group." + TheLastSwordMod.MOD_ID + ".the_last_sword_tab";
    private static final String PREFIX_KEY = "message.the_last_sword.quest_prefix";

    private static final ResourceLocation WELCOME = new ResourceLocation(TheLastSwordMod.MOD_ID, "the_last_sword_welcome");
    private static final ResourceLocation TROUBLED_BLACKSMITH = new ResourceLocation(TheLastSwordMod.MOD_ID, "troubled_blacksmith");
    private static final ResourceLocation DRAGON_CULT_RAID = new ResourceLocation(TheLastSwordMod.MOD_ID, "dragon_cult_raid");
    public static final ResourceLocation DRAGON_CULT_RAID_VICTORY =
            new ResourceLocation(TheLastSwordMod.MOD_ID, "dragon_cult_raid_victory");
    private static final ResourceLocation NETHER_TRAVELER_OUTPOST =
            new ResourceLocation(TheLastSwordMod.MOD_ID, "nether_traveler_outpost");
    private static final ResourceLocation LIBERATOR = new ResourceLocation(TheLastSwordMod.MOD_ID, "liberator");

    private static final int CHECK_INTERVAL = 40;
    private static final int VILLAGE_HINT_DELAY = 60;
    private static final int VILLAGE_SEARCH_RADIUS = 100;
    private static final int BLACKSMITH_EFFECT_DURATION = 999 * 20;
    private static final double BLACKSMITH_MAX_HEALTH = 100.0D;
    private static final double BLACKSMITH_MIN_PLAYER_DISTANCE = 16.0D;
    private static final int BLACKSMITH_SPAWN_ATTEMPTS = 64;
    private static final int GEAR_TRADE_XP = 10;
    //多人下多名玩家共用同一铁匠，卷轴留足次数
    private static final int SCROLL_TRADE_USES = 12;

    //========== 任务播报API ==========

    //向玩家播报任务消息，统一附加mod名前缀；任务系统关闭时不播报
    public static void broadcast(ServerPlayer player, String key, Object... args) {
        if (!isQuestSystemEnabled()) {
            return;
        }
        player.sendSystemMessage(Component.translatable(PREFIX_KEY,
                Component.translatable(MOD_NAME_KEY),
                Component.translatable(key, args)));
    }

    //任务系统总开关
    public static boolean isQuestSystemEnabled() {
        return TheLastSwordConfiguration.QUEST_SYSTEM_ENABLED.get();
    }

    //坐标组件，与原版/locate一致；结构定位的Y无意义时传null显示为~
    private static Component coordinates(int x, Integer y, int z) {
        return ComponentUtils.wrapInSquareBrackets(Component.translatable("chat.coordinates", x, y == null ? "~" : y, z));
    }

    //========== 任务检测 ==========

    //服务端玩家Tick，节流检测各任务
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) {
            return;
        }
        if (player.tickCount % CHECK_INTERVAL != 0 || !isQuestSystemEnabled()) {
            return;
        }
        checkTroubledBlacksmith(player);
        checkDragonCultRaid(player);
        checkNetherTravelerOutpost(player);
    }

    //达成关键进度后播报下一步任务提示
    @SubscribeEvent
    public static void onAdvancementEarn(AdvancementEvent.AdvancementEarnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (!isQuestSystemEnabled()) {
            return;
        }
        ResourceLocation advancementId = event.getAdvancement().getId();
        if (WELCOME.equals(advancementId)) {
            MinecraftServer server = player.server;
            UUID playerId = player.getUUID();
            TheLastSwordMod.queueServerWork(VILLAGE_HINT_DELAY, () -> locateNearestVillage(server, playerId));
        } else if (LIBERATOR.equals(advancementId)) {
            broadcast(player, "message.the_last_sword.liberator_tower_hint");
        }
    }

    //========== 苦恼的铁匠 ==========

    //搜索最近村庄并播报，搜索开销大所以延迟到进服稳定后执行
    private static void locateNearestVillage(MinecraftServer server, UUID playerId) {
        ServerPlayer player = server.getPlayerList().getPlayer(playerId);
        if (player == null) {
            return;
        }
        ServerLevel level = player.serverLevel();
        if (level.dimension() != Level.OVERWORLD) {
            return;
        }
        BlockPos pos = level.findNearestMapStructure(StructureTags.VILLAGE, player.blockPosition(), VILLAGE_SEARCH_RADIUS, false);
        if (pos == null) {
            broadcast(player, "message.the_last_sword.nearest_village_missing");
            return;
        }
        broadcast(player, "message.the_last_sword.nearest_village_location", coordinates(pos.getX(), null, pos.getZ()));
    }

    //玩家首次到达村庄时授予进度并刷出专属村民
    private static void checkTroubledBlacksmith(ServerPlayer player) {
        if (isDone(player, TROUBLED_BLACKSMITH)) {
            return;
        }
        ServerLevel level = player.serverLevel();
        StructureStart village = level.structureManager()
                .getStructureWithPieceAt(player.blockPosition(), StructureTags.VILLAGE);
        if (!village.isValid()) {
            return;
        }
        if (spawnTroubledBlacksmith(level, player, village)) {
            grant(player, TROUBLED_BLACKSMITH);
        }
    }

    //刷出苦恼的铁匠村民并向玩家播报位置
    private static boolean spawnTroubledBlacksmith(ServerLevel level, ServerPlayer player,
            StructureStart village) {
        Villager villager = EntityType.VILLAGER.create(level);
        if (villager == null) {
            return false;
        }
        BlockPos pos = findBlacksmithSpawnPos(level, player, village, villager);
        if (pos == null) {
            return false;
        }
        villager.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, level.random.nextFloat() * 360.0F, 0.0F);
        ForgeEventFactory.onFinalizeSpawn(villager, level, level.getCurrentDifficultyAt(pos), MobSpawnType.EVENT, null, null);

        villager.setVillagerData(villager.getVillagerData().setProfession(VillagerProfession.WEAPONSMITH).setLevel(5));
        villager.setCustomName(Component.translatable("entity.the_last_sword.troubled_blacksmith"));
        villager.setCustomNameVisible(true);
        AttributeInstance maxHealthAttr = villager.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealthAttr != null) {
            maxHealthAttr.setBaseValue(BLACKSMITH_MAX_HEALTH);
            villager.setHealth((float) BLACKSMITH_MAX_HEALTH);
        }

        villager.addEffect(new MobEffectInstance(MobEffects.GLOWING, BLACKSMITH_EFFECT_DURATION, 0, false, false));
        villager.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, BLACKSMITH_EFFECT_DURATION, 4, false, false));
        villager.addEffect(new MobEffectInstance(MobEffects.HEAL, BLACKSMITH_EFFECT_DURATION, 4, false, false));
        villager.setPersistenceRequired();

        //专属交易：终焉卷轴与变卖的家当
        MerchantOffers offers = villager.getOffers();
        offers.clear();
        offers.add(new MerchantOffer(new ItemStack(Items.EMERALD), new ItemStack(ModItems.THE_LAST_END_SCROLL.get()), SCROLL_TRADE_USES, 100, 0.0F));
        offers.add(gearOffer(2, Items.IRON_SWORD, Enchantments.MOB_LOOTING));
        offers.add(gearOffer(5, Items.IRON_HELMET, Enchantments.PROJECTILE_PROTECTION));
        offers.add(gearOffer(8, Items.IRON_CHESTPLATE, Enchantments.ALL_DAMAGE_PROTECTION));
        offers.add(gearOffer(7, Items.IRON_LEGGINGS, Enchantments.BLAST_PROTECTION));
        offers.add(gearOffer(4, Items.IRON_BOOTS, Enchantments.FALL_PROTECTION));

        if (!level.addFreshEntity(villager)) {
            return false;
        }
        broadcast(player, "message.the_last_sword.troubled_blacksmith_intro");
        broadcast(player, "message.the_last_sword.troubled_blacksmith_location", coordinates(pos.getX(), pos.getY(), pos.getZ()));
        return true;
    }

    //优先在村庄结构内随机选择距玩家至少16格的安全落点；小村庄则选择最远备选点
    private static BlockPos findBlacksmithSpawnPos(ServerLevel level, ServerPlayer player,
            StructureStart village, Villager villager) {
        if (village.getPieces().isEmpty()) {
            return null;
        }

        double minimumDistanceSqr = BLACKSMITH_MIN_PLAYER_DISTANCE * BLACKSMITH_MIN_PLAYER_DISTANCE;
        double farthestDistanceSqr = -1.0D;
        BlockPos farthestPos = null;

        for (int attempt = 0; attempt < BLACKSMITH_SPAWN_ATTEMPTS; attempt++) {
            var piece = village.getPieces().get(level.random.nextInt(village.getPieces().size()));
            var box = piece.getBoundingBox();
            int x = box.minX() + level.random.nextInt(box.getXSpan());
            int z = box.minZ() + level.random.nextInt(box.getZSpan());
            BlockPos pos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    new BlockPos(x, 0, z));

            villager.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, 0.0F, 0.0F);
            if (!level.getFluidState(pos).isEmpty()
                    || !level.getFluidState(pos.above()).isEmpty()
                    || !level.noCollision(villager)) {
                continue;
            }

            double dx = pos.getX() + 0.5D - player.getX();
            double dz = pos.getZ() + 0.5D - player.getZ();
            double distanceSqr = dx * dx + dz * dz;
            if (distanceSqr >= minimumDistanceSqr) {
                return pos;
            }
            if (distanceSqr > farthestDistanceSqr) {
                farthestDistanceSqr = distanceSqr;
                farthestPos = pos;
            }
        }
        return farthestPos;
    }

    //家当交易：绿宝石换经验修补+指定1级附魔的铁装备
    private static MerchantOffer gearOffer(int emeralds, Item item, Enchantment enchantment) {
        ItemStack stack = new ItemStack(item);
        stack.enchant(Enchantments.MENDING, 1);
        stack.enchant(enchantment, 1);
        return new MerchantOffer(new ItemStack(Items.EMERALD, emeralds), stack, 1, GEAR_TRADE_XP, 0.0F);
    }

    //========== 拜龙教袭击 ==========

    //携带龙水晶的玩家首次进入村庄时触发三波拜龙教袭击
    private static void checkDragonCultRaid(ServerPlayer player) {
        if (isDone(player, DRAGON_CULT_RAID_VICTORY)
                || !player.getInventory().contains(new ItemStack(ModItems.DRAGON_CRYSTAL.get()))) {
            return;
        }

        ServerLevel level = player.serverLevel();
        var village = level.structureManager().getStructureWithPieceAt(player.blockPosition(), StructureTags.VILLAGE);
        if (!village.isValid()) {
            return;
        }

        //多人同时进入同一村庄时复用已经开始的袭击，避免波次叠加
        boolean raidAlreadyActive = EcaAPI.getActiveRaids(level).stream()
                .anyMatch(activeRaid -> DragonCultRaid.ID.equals(activeRaid.getDefinitionId())
                        && village.getBoundingBox().isInside(activeRaid.getCenter()));
        if (raidAlreadyActive) {
            grant(player, DRAGON_CULT_RAID);
            return;
        }

        //玩家位置已经确认位于真实村庄结构拼图内，避免包围盒中心落在拼图空隙导致首次 tick 判败
        RaidInstance raid = EcaAPI.startRaidAt(level, player.blockPosition(), DragonCultRaid.ID);
        if (raid != null) {
            grant(player, DRAGON_CULT_RAID);
        }
    }

    //击退拜龙教并取得秘信后，引导玩家前往旅行者的下界据点
    private static void checkNetherTravelerOutpost(ServerPlayer player) {
        if (!isDone(player, NETHER_TRAVELER_OUTPOST)
                && isDone(player, DRAGON_CULT_RAID_VICTORY)
                && player.getInventory().contains(new ItemStack(ModItems.DRAGON_CULT_SECRET_LETTER.get()))) {
            grant(player, NETHER_TRAVELER_OUTPOST);
        }
    }

    //========== 进度工具 ==========

    //判断进度是否已完成
    public static boolean isDone(ServerPlayer player, ResourceLocation id) {
        Advancement advancement = player.server.getAdvancements().getAdvancement(id);
        return advancement != null && player.getAdvancements().getOrStartProgress(advancement).isDone();
    }

    //授予进度（补齐全部条件）
    public static void grant(ServerPlayer player, ResourceLocation id) {
        Advancement advancement = player.server.getAdvancements().getAdvancement(id);
        if (advancement == null) {
            return;
        }
        AdvancementProgress progress = player.getAdvancements().getOrStartProgress(advancement);
        for (String criterion : progress.getRemainingCriteria()) {
            player.getAdvancements().award(advancement, criterion);
        }
    }
}
