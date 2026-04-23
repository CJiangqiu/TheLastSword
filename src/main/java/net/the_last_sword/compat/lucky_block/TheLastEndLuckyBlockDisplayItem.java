package net.the_last_sword.compat.lucky_block;

import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

//终焉幸运方块的显示物品 - 背包/手持时使用 GeckoLib 渲染带动画的 3D 模型
public class TheLastEndLuckyBlockDisplayItem extends BlockItem implements GeoItem {

    //幸运值 NBT 键, 与 BlockEntity 保持一致
    private static final String TAG_LUCK = "Luck";

    //耐久条颜色: 正幸运绿 / 负幸运红
    private static final int BAR_COLOR_POSITIVE = 0x5FFF5F;
    private static final int BAR_COLOR_NEGATIVE = 0xFF5F5F;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public TheLastEndLuckyBlockDisplayItem(Block block, Properties properties) {
        super(block, properties);
    }

    //从 ItemStack NBT 读取 luck 值, 不存在则为 0
    private static int readLuck(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) return 0;
        //方块物品 NBT 存在两层结构: 直接 Luck (合成后) 或 BlockEntityTag.Luck (破坏掉落)
        if (tag.contains("BlockEntityTag")) {
            CompoundTag beTag = tag.getCompound("BlockEntityTag");
            if (beTag.contains(TAG_LUCK)) return beTag.getInt(TAG_LUCK);
        }
        return tag.contains(TAG_LUCK) ? tag.getInt(TAG_LUCK) : 0;
    }

    //复刻 LuckyBlock 原版 tooltip 格式: "幸运值: +20"(灰前缀 + 绿/红/金数字)
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        int luck = readLuck(stack);
        Component luckNum;
        if (luck > 0) {
            luckNum = Component.literal("+" + luck).withStyle(ChatFormatting.GREEN);
        } else if (luck < 0) {
            luckNum = Component.literal(String.valueOf(luck)).withStyle(ChatFormatting.RED);
        } else {
            luckNum = Component.literal("0").withStyle(ChatFormatting.GOLD);
        }
        tooltip.add(Component.translatable("item.the_last_sword.the_last_end_lucky_block.luck")
            .withStyle(ChatFormatting.GRAY)
            .append(Component.literal(": ").withStyle(ChatFormatting.GRAY))
            .append(luckNum));
    }

    //耐久条: luck = 0 不显示, 正负按绝对值填充(范围 [-100, +100] 对齐 clamp)
    @Override
    public boolean isBarVisible(ItemStack stack) {
        return readLuck(stack) != 0;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        int luck = readLuck(stack);
        int abs = Math.min(Math.abs(luck), 100);
        return Math.round(abs * 13.0F / 100.0F);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return readLuck(stack) >= 0 ? BAR_COLOR_POSITIVE : BAR_COLOR_NEGATIVE;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        super.initializeClient(consumer);
        consumer.accept(new IClientItemExtensions() {
            private final BlockEntityWithoutLevelRenderer renderer = new TheLastEndLuckyBlockDisplayItemRenderer();

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return renderer;
            }
        });
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, state -> PlayState.CONTINUE));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
