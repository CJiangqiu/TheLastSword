package net.the_last_sword.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.InstrumentItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.the_last_sword.event.TheLastSwordQuestHandler;
import net.the_last_sword.init.ModInstruments;

import javax.annotation.Nullable;
import java.util.List;

//拜龙教号角，吹响后在村庄内召来一场拜龙教袭击
public class DragonCultHornItem extends InstrumentItem {

    private static final String LORE_KEY = "item_tooltip_lore.the_last_sword.dragon_cult_horn";
    private static final String NO_VILLAGE_KEY = "message.the_last_sword.dragon_cult_horn_no_village";
    private static final String RAID_ACTIVE_KEY = "message.the_last_sword.dragon_cult_horn_raid_active";

    public DragonCultHornItem() {
        super(new Item.Properties()
                .stacksTo(1)
                .rarity(Rarity.UNCOMMON),
            ModInstruments.DRAGON_CULT_HORNS
        );
    }

    //右键即吹响：播号角声、进冷却，并当场判定袭击
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        //音量取自原版乐器算法：传播距离除以16
        level.playSound(player, player, SoundEvents.RAID_HORN.value(), SoundSource.RECORDS,
                ModInstruments.RANGE / 16.0F, 1.0F);
        level.gameEvent(GameEvent.INSTRUMENT_PLAY, player.position(), GameEvent.Context.of(player));
        player.getCooldowns().addCooldown(this, ModInstruments.USE_DURATION);
        player.awardStat(Stats.ITEM_USED.get(this));

        //只有真正召来一场新袭击才消耗号角
        if (player instanceof ServerPlayer serverPlayer) {
            switch (TheLastSwordQuestHandler.tryStartDragonCultRaid(serverPlayer)) {
                case STARTED -> {
                    if (!serverPlayer.getAbilities().instabuild) {
                        stack.shrink(1);
                    }
                }
                case ALREADY_ACTIVE ->
                    serverPlayer.displayClientMessage(Component.translatable(RAID_ACTIVE_KEY), true);
                case UNAVAILABLE ->
                    serverPlayer.displayClientMessage(Component.translatable(NO_VILLAGE_KEY), true);
            }
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable(LORE_KEY).withStyle(ChatFormatting.GRAY));
    }
}
