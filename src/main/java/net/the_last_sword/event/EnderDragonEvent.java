package net.the_last_sword.event;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.ItemHandlerHelper;
import net.the_last_sword.configuration.TheLastSwordConfiguration;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Mod.EventBusSubscriber
public class EnderDragonEvent {
    private static final String CHALLENGE_COUNT_KEY = "EnderDragonChallengeCount";
    private static final String PLAYER_UUID_LIST_KEY = "DragonAttackers";
    private static final String MODIFIER_ADDED_KEY = "ModifiersAdded";

    // 持久化存储挑战次数和攻击者列表
    public static class ChallengeData extends SavedData {
        private int challengeCount;
        private final Set<UUID> attackerUUIDs = new HashSet<>();

        public ChallengeData() {
            this.challengeCount = 0;
        }

        public ChallengeData(CompoundTag tag) {
            this.challengeCount = tag.getInt(CHALLENGE_COUNT_KEY);
            ListTag uuidList = tag.getList(PLAYER_UUID_LIST_KEY, Tag.TAG_COMPOUND);
            for (int i = 0; i < uuidList.size(); i++) {
                CompoundTag uuidTag = uuidList.getCompound(i);
                this.attackerUUIDs.add(uuidTag.getUUID("UUID"));
            }
        }

        @Override
        public CompoundTag save(CompoundTag tag) {
            tag.putInt(CHALLENGE_COUNT_KEY, this.challengeCount);
            ListTag uuidList = new ListTag();
            for (UUID uuid : this.attackerUUIDs) {
                CompoundTag uuidTag = new CompoundTag();
                uuidTag.putUUID("UUID", uuid);
                uuidList.add(uuidTag);
            }
            tag.put(PLAYER_UUID_LIST_KEY, uuidList);
            return tag;
        }

        public int getChallengeCount() {
            return challengeCount;
        }

        public void incrementChallengeCount() {
            this.challengeCount++;
            this.setDirty();
        }

        public Set<UUID> getAttackerUUIDs() {
            return attackerUUIDs;
        }

        public void addAttacker(UUID playerUUID) {
            this.attackerUUIDs.add(playerUUID);
            this.setDirty();
        }

        public void clearAttackers() {
            this.attackerUUIDs.clear();
            this.setDirty();
        }

        public static ChallengeData load(CompoundTag tag) {
            return new ChallengeData(tag);
        }
    }

    // 获取挑战数据（仅限末地）
    private static ChallengeData getChallengeData(ServerLevel level) {
        if (level.dimension() == Level.END) {
            DimensionDataStorage storage = level.getDataStorage();
            return storage.computeIfAbsent(ChallengeData::load, ChallengeData::new, "dragon_challenges");
        }
        return null;
    }

    // 玩家攻击末影龙时记录UUID
    @SubscribeEvent
    public static void onEntityAttacked(LivingAttackEvent event) {
        if (event.getEntity().level().isClientSide()) return;

        if (event.getEntity() instanceof EnderDragon dragon
                && event.getSource().getEntity() instanceof Player player) {
            ServerLevel level = (ServerLevel) dragon.level();
            ChallengeData data = getChallengeData(level);
            if (data != null) {
                data.addAttacker(player.getUUID());
            }
        }
    }

    // 末影龙死亡时发放龙蛋并增加挑战次数
    @SubscribeEvent
    public static void onEntityDeath(LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        if (!(event.getEntity() instanceof EnderDragon dragon)) return;

        ServerLevel level = (ServerLevel) dragon.level();
        ChallengeData data = getChallengeData(level);
        if (data == null) return;

        data.incrementChallengeCount();

        if (!TheLastSwordConfiguration.ENDER_DRAGON_EGG_DROP.get()) return;

        int radius = TheLastSwordConfiguration.ENDER_DRAGON_EGG_RADIUS.get();
        int amount = TheLastSwordConfiguration.ENDER_DRAGON_EGG_AMOUNT.get();
        boolean giveAbsent = TheLastSwordConfiguration.ENDER_DRAGON_EGG_GIVE_TO_ABSENT_PLAYERS.get();
        boolean multiple = TheLastSwordConfiguration.ENDER_DRAGON_EGG_MULTIPLE.get();

        Set<UUID> attackerUUIDs = data.getAttackerUUIDs();
        Set<UUID> remainingUUIDs = new HashSet<>(attackerUUIDs);

        // 给在场玩家发放龙蛋
        for (Player player : level.players()) {
            if (!attackerUUIDs.contains(player.getUUID())) continue;

            double distance = player.distanceToSqr(dragon.getX(), dragon.getY(), dragon.getZ());
            if (distance > radius * radius) continue;

            ItemStack dragonEgg = new ItemStack(Blocks.DRAGON_EGG, amount);
            ItemHandlerHelper.giveItemToPlayer(player, dragonEgg);
            remainingUUIDs.remove(player.getUUID());

            // 非多人模式下只给第一个在场玩家
            if (!multiple) break;
        }

        // 给未在场玩家发放龙蛋（仅多人模式下生效）
        if (giveAbsent && multiple) {
            for (UUID uuid : remainingUUIDs) {
                Player absentPlayer = level.getServer().getPlayerList().getPlayer(uuid);
                if (absentPlayer != null) {
                    ItemStack dragonEgg = new ItemStack(Blocks.DRAGON_EGG, amount);
                    ItemHandlerHelper.giveItemToPlayer(absentPlayer, dragonEgg);
                }
            }
        }

        data.clearAttackers();
    }

    // 末影龙生成时根据挑战次数强化属性
    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getEntity() instanceof EnderDragon dragon)) return;

        ServerLevel level = (ServerLevel) event.getLevel();
        if (level.dimension() != Level.END) return;

        ChallengeData data = getChallengeData(level);
        if (data == null || data.getChallengeCount() <= 0) return;

        CompoundTag dragonData = dragon.getPersistentData();
        if (dragonData.getBoolean(MODIFIER_ADDED_KEY)) return;

        int challengeCount = data.getChallengeCount();
        int cappedLevel = Math.min(challengeCount, TheLastSwordConfiguration.ENDER_DRAGON_MAX_LEVEL.get());

        // 设置等级名称
        dragon.setCustomName(Component.literal(dragon.getDisplayName().getString() + " LV" + cappedLevel));

        applyDragonUpgrades(dragon, cappedLevel);
        dragonData.putBoolean(MODIFIER_ADDED_KEY, true);
    }

    // 根据等级强化末影龙属性
    private static void applyDragonUpgrades(EnderDragon dragon, int cappedLevel) {
        double healthIncrease = cappedLevel * (cappedLevel < 6 ?
                TheLastSwordConfiguration.ENDER_DRAGON_HEALTH_INCREASE_VALUE.get() :
                TheLastSwordConfiguration.ENDER_DRAGON_HEALTH_INCREASE_VALUE_HIGH_LEVEL.get());

        AttributeInstance maxHealthAttr = dragon.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealthAttr != null) {
            double newMaxHealth = maxHealthAttr.getBaseValue() + healthIncrease;
            maxHealthAttr.setBaseValue(newMaxHealth);
            dragon.setHealth((float) newMaxHealth);
        }

        double armorIncrease = cappedLevel * (cappedLevel < 6 ?
                TheLastSwordConfiguration.ENDER_DRAGON_ARMOR_INCREASE_VALUE.get() :
                TheLastSwordConfiguration.ENDER_DRAGON_ARMOR_INCREASE_VALUE_HIGH_LEVEL.get());
        addAttributeModifier(dragon, Attributes.ARMOR, armorIncrease, "dragon_armor_boost");

        double attackIncrease = cappedLevel * (cappedLevel < 6 ?
                TheLastSwordConfiguration.ENDER_DRAGON_ATTACK_INCREASE_VALUE.get() :
                TheLastSwordConfiguration.ENDER_DRAGON_ATTACK_INCREASE_VALUE_HIGH_LEVEL.get());
        addAttributeModifier(dragon, Attributes.ATTACK_DAMAGE, attackIncrease, "dragon_attack_boost");
    }

    // 添加属性修饰符
    private static void addAttributeModifier(EnderDragon dragon, Attribute attribute, double value, String name) {
        AttributeInstance attr = dragon.getAttribute(attribute);
        if (attr != null) {
            UUID modifierId = UUID.nameUUIDFromBytes(name.getBytes());
            attr.addPermanentModifier(new AttributeModifier(
                    modifierId, name, value, AttributeModifier.Operation.ADDITION));
        }
    }
}