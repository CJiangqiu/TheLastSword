package net.the_last_sword.compat.curios;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.the_last_sword.client.renderer.WingsThatCoverTheWorldRenderer;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.List;
import java.util.function.Consumer;

//覆世之翼 - back槽位
public class WingsThatCoverTheWorld extends Item implements ICurioItem {

    public WingsThatCoverTheWorld() {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant());
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        LivingEntity entity = slotContext.entity();
        if (entity instanceof Player player) {
            //非创造/旁观模式下给予飞行能力
            if (!player.isCreative() && !player.isSpectator()) {
                player.getAbilities().mayfly = true;
                player.onUpdateAbilities();
            }
        }
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        LivingEntity entity = slotContext.entity();
        if (entity instanceof Player player) {
            //卸下时移除飞行能力（非创造/旁观模式）
            if (!player.isCreative() && !player.isSpectator()) {
                player.getAbilities().mayfly = false;
                player.getAbilities().flying = false;
                player.onUpdateAbilities();
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        double reduction = TheLastSwordConfiguration.getCuriosWingsVoidDamageReductionSafely();
        //显示减免百分比：例如 multiplier=0.1 -> 减免 90%
        String reductionPercent = String.format("-%.0f", (1.0 - reduction) * 100);
        tooltip.add(Component.translatable("item_tooltip.the_last_sword.wings_that_cover_the_world", reductionPercent));
        tooltip.add(Component.translatable("item_tooltip_lore.the_last_sword.wings_that_cover_the_world")
            .withStyle(net.minecraft.ChatFormatting.GRAY));
    }

    //注册客户端渲染器
    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private WingsThatCoverTheWorldRenderer renderer;

            public ICurioRenderer getCurioRenderer() {
                if (renderer == null) {
                    renderer = new WingsThatCoverTheWorldRenderer();
                }
                return renderer;
            }
        });
    }
}
