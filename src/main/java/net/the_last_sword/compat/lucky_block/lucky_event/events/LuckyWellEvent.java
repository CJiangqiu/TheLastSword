package net.the_last_sword.compat.lucky_block.lucky_event.events;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.the_last_sword.TheLastSwordMod;
import net.the_last_sword.compat.lucky_block.lucky_event.LuckyEvent;
import net.the_last_sword.compat.lucky_block.lucky_event.LuckyEventCategory;
import net.the_last_sword.compat.lucky_block.lucky_event.LuckyEventContext;
import net.the_last_sword.init.ModItems;

import java.util.Optional;

//幸运水井事件 - 幸运方块原位置直接放置 lucky_well 结构(NBT 原点 = ctx.pos()); 玩家非空则额外给一个重命名"coin"(紫色加粗斜体)的龙水晶
public class LuckyWellEvent extends LuckyEvent {

    private static final ResourceLocation STRUCTURE_ID = new ResourceLocation(TheLastSwordMod.MOD_ID, "lucky_well");
    private static final String LOAD_FAILED_KEY = "message.the_last_sword.arena.load_failed";

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
            .setIgnoreEntities(false)
            .addProcessor(BlockIgnoreProcessor.AIR);
        BlockPos placePos = ctx.pos().below(3);
        template.placeInWorld(world, placePos, placePos, settings, world.getRandom(), 2);

        if (player == null) return;
        ItemStack coin = new ItemStack(ModItems.DRAGON_CRYSTAL.get());
        coin.setHoverName(Component.literal("coin").withStyle(style -> style
            .withColor(ChatFormatting.DARK_PURPLE)
            .withBold(true)
            .withItalic(true)));
        player.addItem(coin);
        if (!coin.isEmpty()) {
            player.drop(coin, false);
        }
    }
}
