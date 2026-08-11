package net.the_last_sword.compat.lucky_block.lucky_event.events;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.the_last_sword.compat.lucky_block.lucky_event.LuckyEvent;
import net.the_last_sword.compat.lucky_block.lucky_event.LuckyEventCategory;
import net.the_last_sword.compat.lucky_block.lucky_event.LuckyEventContext;
import net.the_last_sword.init.ModCreativeTabs;
import net.the_last_sword.init.ModItems;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

//幸运掉落事件 - 从 mod 创造栏 displayItems 里随机抽一个物品在幸运方块原位置掉落; 创造栏空时回退到龙水晶
public class LuckyDropEvent extends LuckyEvent {

    @Override
    public LuckyEventCategory getCategory() {
        return LuckyEventCategory.GOOD;
    }

    @Override
    public String getId() {
        return "Random Drop";
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
        BlockPos pos = ctx.pos();
        RandomSource random = ctx.random();

        ItemStack picked = pickFromCreativeTab(world, random);
        if (picked == null || picked.isEmpty()) {
            picked = new ItemStack(ModItems.DRAGON_CRYSTAL.get());
        }

        ItemEntity entity = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, picked.copy());
        entity.setDeltaMovement(Vec3.ZERO);
        world.addFreshEntity(entity);
    }

    private static ItemStack pickFromCreativeTab(ServerLevel world, RandomSource random) {
        CreativeModeTab tab = ModCreativeTabs.THE_LAST_SWORD_TAB.get();
        if (tab.getDisplayItems().isEmpty()) {
            tab.buildContents(new CreativeModeTab.ItemDisplayParameters(
                FeatureFlags.VANILLA_SET,
                false,
                world.registryAccess()
            ));
        }
        Collection<ItemStack> items = tab.getDisplayItems();
        if (items.isEmpty()) return null;
        List<ItemStack> pool = new ArrayList<>(items);
        return pool.get(random.nextInt(pool.size()));
    }
}
