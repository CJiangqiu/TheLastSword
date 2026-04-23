package net.the_last_sword.compat.lucky_block.lucky_event.events;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.registries.ForgeRegistries;
import net.the_last_sword.TheLastSwordMod;
import net.the_last_sword.compat.lucky_block.lucky_event.ArenaBattleHandler;
import net.the_last_sword.compat.lucky_block.lucky_event.LuckyEvent;
import net.the_last_sword.compat.lucky_block.lucky_event.LuckyEventCategory;
import net.the_last_sword.compat.lucky_block.lucky_event.LuckyEventContext;

import java.util.List;
import java.util.Optional;

//终焉竞技场事件 - 放置 the_last_end_arena 结构, 扫描红石块/青金石块作为玩家/对手出生点; 1s 后发欢迎, 4s 后生成随机对手并公布名字
public class ArenaEvent extends LuckyEvent {

    private static final ResourceLocation STRUCTURE_ID = new ResourceLocation(TheLastSwordMod.MOD_ID, "the_last_end_arena");
    private static final String LOAD_FAILED_KEY = "message.the_last_sword.arena.load_failed";
    private static final String INTRO_KEY = "message.the_last_sword.lucky_block.arena_intro";
    private static final String OPPONENT_KEY = "message.the_last_sword.lucky_block.arena_opponent";
    private static final int TICKS_PER_SECOND = 20;
    private static final int INTRO_DELAY = 1 * TICKS_PER_SECOND;
    private static final int OPPONENT_DELAY = 4 * TICKS_PER_SECOND;
    private static final int OPPONENT_PICK_RETRIES = 20;

    @Override
    public LuckyEventCategory getCategory() {
        return LuckyEventCategory.NEUTRAL;
    }

    @Override
    public int getMinLuck() {
        return -100;
    }

    @Override
    public int getMaxLuck() {
        return 100;
    }

    @Override
    public void execute(LuckyEventContext ctx) {
        ServerLevel world = ctx.world();
        Player player = ctx.player();
        StructureTemplateManager manager = world.getStructureManager();
        Optional<StructureTemplate> optional = manager.get(STRUCTURE_ID);
        if (optional.isEmpty()) {
            if (player != null) {
                player.sendSystemMessage(Component.translatable(LOAD_FAILED_KEY));
            }
            return;
        }
        StructureTemplate template = optional.get();
        StructurePlaceSettings settings = new StructurePlaceSettings()
            .setRotation(Rotation.NONE)
            .setMirror(Mirror.NONE)
            .setIgnoreEntities(false);
        BlockPos placePos = ctx.pos();
        template.placeInWorld(world, placePos, placePos, settings, world.getRandom(), 2);

        if (player == null) return;

        BlockPos[] anchors = scanAnchors(world, placePos, template.getSize());
        BlockPos playerSpawn = anchors[0];
        BlockPos enemySpawn = anchors[1];
        if (playerSpawn == null || enemySpawn == null) {
            player.sendSystemMessage(Component.translatable(LOAD_FAILED_KEY));
            return;
        }

        player.teleportTo(playerSpawn.getX() + 0.5, playerSpawn.getY(), playerSpawn.getZ() + 0.5);

        TheLastSwordMod.queueServerWork(INTRO_DELAY, () -> {
            if (!isActorAlive(player, world)) return;
            player.sendSystemMessage(Component.translatable(INTRO_KEY, player.getDisplayName()));
        });

        TheLastSwordMod.queueServerWork(OPPONENT_DELAY, () -> {
            if (!isActorAlive(player, world)) return;
            Mob opponent = pickRandomMob(world);
            if (opponent == null) return;
            opponent.moveTo(enemySpawn.getX() + 0.5, enemySpawn.getY(), enemySpawn.getZ() + 0.5, 0.0F, 0.0F);
            ForgeEventFactory.onFinalizeSpawn(opponent, world, world.getCurrentDifficultyAt(enemySpawn), MobSpawnType.EVENT, null, null);
            world.addFreshEntity(opponent);
            opponent.setTarget(player);
            ArenaBattleHandler.tagParticipants(player, opponent);
            player.sendSystemMessage(Component.translatable(OPPONENT_KEY, opponent.getType().getDescription()));
        });
    }

    //返回 [playerSpawn (红石块上方), enemySpawn (青金石块上方)]; 找不到的项为 null
    private static BlockPos[] scanAnchors(ServerLevel world, BlockPos origin, Vec3i size) {
        BlockPos playerSpawn = null;
        BlockPos enemySpawn = null;
        for (int dx = 0; dx < size.getX(); dx++) {
            for (int dy = 0; dy < size.getY(); dy++) {
                for (int dz = 0; dz < size.getZ(); dz++) {
                    if (playerSpawn != null && enemySpawn != null) {
                        return new BlockPos[] {playerSpawn, enemySpawn};
                    }
                    BlockPos pos = origin.offset(dx, dy, dz);
                    BlockState state = world.getBlockState(pos);
                    if (playerSpawn == null && state.is(Blocks.REDSTONE_BLOCK)) {
                        playerSpawn = pos.above();
                    } else if (enemySpawn == null && state.is(Blocks.LAPIS_BLOCK)) {
                        enemySpawn = pos.above();
                    }
                }
            }
        }
        return new BlockPos[] {playerSpawn, enemySpawn};
    }

    //从所有 category != MISC 的 EntityType 中随机抽一个可创建的 Mob; 最多重试 20 次
    private static Mob pickRandomMob(ServerLevel world) {
        RandomSource random = world.getRandom();
        List<EntityType<?>> pool = ForgeRegistries.ENTITY_TYPES.getValues().stream()
            .filter(t -> t.getCategory() != MobCategory.MISC)
            .toList();
        if (pool.isEmpty()) return null;
        for (int tries = 0; tries < OPPONENT_PICK_RETRIES; tries++) {
            EntityType<?> type = pool.get(random.nextInt(pool.size()));
            Entity e = type.create(world);
            if (e instanceof Mob mob) return mob;
        }
        return null;
    }

    private static boolean isActorAlive(Player player, ServerLevel world) {
        return player.isAlive() && !player.isRemoved() && player.level() == world;
    }
}
