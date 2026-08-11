package net.the_last_sword.compat.lucky_block.lucky_event.events;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
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

//天降末地休憩处事件 - 以幸运方块位置为锚, 其上方 16 格随机四向旋转放置 end_reset 结构; 玩家仅用于消息播报
public class EndResetSkyEvent extends LuckyEvent {

    private static final ResourceLocation STRUCTURE_ID = new ResourceLocation(TheLastSwordMod.MOD_ID, "end_reset");
    private static final String MESSAGE_KEY = "message.the_last_sword.lucky_block.look_sky";
    private static final String LOAD_FAILED_KEY = "message.the_last_sword.arena.load_failed";
    private static final int SKY_OFFSET = 16;

    @Override
    public LuckyEventCategory getCategory() {
        return LuckyEventCategory.GOOD;
    }

    @Override
    public String getId() {
        return "End Resting Place";
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
        if (player != null) {
            player.sendSystemMessage(Component.translatable(MESSAGE_KEY));
        }
    }
}
