package net.the_last_sword.compat.lucky_block.lucky_event.events;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.the_last_sword.TheLastSwordMod;
import net.the_last_sword.compat.lucky_block.lucky_event.LuckyEvent;
import net.the_last_sword.compat.lucky_block.lucky_event.LuckyEventCategory;
import net.the_last_sword.compat.lucky_block.lucky_event.LuckyEventContext;

import java.util.Optional;

//时间之伤发射事件 - 以幸运方块位置为锚, 其上方 64 格随机四向放置 wound_of_time 结构; 有玩家时再做 5..1 倒计时并给漂浮 V
public class WoundOfTimeLaunchEvent extends LuckyEvent {

    private static final ResourceLocation STRUCTURE_ID = new ResourceLocation(TheLastSwordMod.MOD_ID, "wound_of_time");
    private static final String LIFTOFF_KEY = "message.the_last_sword.lucky_block.liftoff";
    private static final String LOAD_FAILED_KEY = "message.the_last_sword.arena.load_failed";
    private static final int TICKS_PER_SECOND = 20;
    private static final int SKY_OFFSET = 64;
    private static final int LEVITATION_AMPLIFIER = 4;
    private static final int LEVITATION_DURATION_TICKS = 25 * TICKS_PER_SECOND;
    private static final int SLOW_FALLING_DURATION_TICKS = 30 * TICKS_PER_SECOND;

    @Override
    public LuckyEventCategory getCategory() {
        return LuckyEventCategory.GOOD;
    }

    @Override
    public int getMinLuck() {
        return 0;
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
        Rotation rotation = Rotation.getRandom(ctx.random());
        StructurePlaceSettings settings = new StructurePlaceSettings()
            .setRotation(rotation)
            .setMirror(Mirror.NONE)
            .setIgnoreEntities(false);
        BlockPos ctxPos = ctx.pos();
        BlockPos anchor = ctxPos.above(SKY_OFFSET);
        BoundingBox probe = template.getBoundingBox(settings, anchor);
        int dx = ctxPos.getX() - (probe.minX() + probe.maxX()) / 2;
        int dz = ctxPos.getZ() - (probe.minZ() + probe.maxZ()) / 2;
        BlockPos placePos = anchor.offset(dx, 0, dz);
        template.placeInWorld(world, placePos, placePos, settings, world.getRandom(), 2);

        if (player == null) return;

        for (int n = 5; n >= 1; n--) {
            int delay = (6 - n) * TICKS_PER_SECOND;
            String digit = String.valueOf(n);
            TheLastSwordMod.queueServerWork(delay, () -> {
                if (!isActorAlive(player, world)) return;
                player.sendSystemMessage(Component.literal(digit));
            });
        }

        TheLastSwordMod.queueServerWork(6 * TICKS_PER_SECOND, () -> {
            if (!isActorAlive(player, world)) return;
            player.sendSystemMessage(Component.translatable(LIFTOFF_KEY));
            player.addEffect(new MobEffectInstance(MobEffects.LEVITATION, LEVITATION_DURATION_TICKS, LEVITATION_AMPLIFIER));
            player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, SLOW_FALLING_DURATION_TICKS, 0));
        });
    }

    private static boolean isActorAlive(Player player, ServerLevel world) {
        return player.isAlive() && !player.isRemoved() && player.level() == world;
    }
}
