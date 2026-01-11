package net.the_last_sword.compat.curios;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.the_last_sword.compat.CompatCheck;
import net.the_last_sword.init.ModItems;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

//处理龙水晶饰品的特殊效果
@Mod.EventBusSubscriber(modid = "the_last_sword", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CuriosEffectHandler {

    //防止虚空伤害转换递归
    private static final ThreadLocal<Boolean> CONVERTING_DAMAGE = ThreadLocal.withInitial(() -> false);

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        if (!CompatCheck.isCuriosLoaded()) {
            return;
        }

        if (!(event.getSource().getEntity() instanceof Player attacker)) {
            return;
        }

        handleDragonCrystalRingDamageBonus(event, attacker);
    }

    @SubscribeEvent
    public static void onCriticalHit(CriticalHitEvent event) {
        if (!CompatCheck.isCuriosLoaded()) {
            return;
        }

        //只处理真正的暴击
        if (!event.isVanillaCritical()) {
            return;
        }

        Player player = event.getEntity();
        if (!hasCurioEquipped(player, ModItems.DRAGON_CRYSTAL_NECKLACE.get())) {
            return;
        }

        //计算暴击加成：20% + 幸运值×20%
        double luck = player.getAttributeValue(Attributes.LUCK);
        float bonusMultiplier = (float) (0.2 + luck * 0.2);

        //在原有暴击倍率基础上增加
        float newModifier = event.getDamageModifier() + bonusMultiplier;
        event.setDamageModifier(newModifier);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!CompatCheck.isCuriosLoaded()) {
            return;
        }

        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        //覆世之翼：虚空伤害减免90%（优先处理）
        handleWingsVoidDamageReduction(event, player);

        //龙水晶项链：概率免疫伤害
        handleDragonCrystalNecklaceImmunity(event, player);

        //龙水晶皇冠：虚空伤害转换
        handleDragonCrystalCrownVoidConversion(event, player);
    }

    //处理覆世之翼的虚空伤害减免（-90%）
    private static void handleWingsVoidDamageReduction(LivingHurtEvent event, Player player) {
        if (!hasCurioEquipped(player, ModItems.WINGS_THAT_COVER_THE_WORLD.get())) {
            return;
        }

        //检查是否是虚空伤害（掉出世界）
        if (isOutOfWorldDamage(event.getSource())) {
            event.setAmount(event.getAmount() * 0.1f);
        }
    }

    //检查是否是虚空伤害源
    private static boolean isOutOfWorldDamage(DamageSource source) {
        return source.is(DamageTypes.FELL_OUT_OF_WORLD);
    }

    //处理龙水晶指环的伤害加成（+50%伤害）
    private static void handleDragonCrystalRingDamageBonus(LivingDamageEvent event, Player attacker) {
        if (!hasCurioEquipped(attacker, ModItems.DRAGON_CRYSTAL_RING.get())) {
            return;
        }

        float originalDamage = event.getAmount();
        event.setAmount(originalDamage * 1.5f);
    }

    //处理龙水晶项链的概率免疫伤害
    private static void handleDragonCrystalNecklaceImmunity(LivingHurtEvent event, Player player) {
        if (!hasCurioEquipped(player, ModItems.DRAGON_CRYSTAL_NECKLACE.get())) {
            return;
        }

        //计算免疫概率：20% + 幸运值×20%，最高90%
        double luck = player.getAttributeValue(Attributes.LUCK);
        double totalChance = Math.min(0.9, 0.2 + luck * 0.2);

        if (player.getRandom().nextDouble() < totalChance) {
            event.setCanceled(true);
        }
    }

    //处理龙水晶皇冠的虚空伤害转换
    private static void handleDragonCrystalCrownVoidConversion(LivingHurtEvent event, Player player) {
        //防止递归
        if (CONVERTING_DAMAGE.get()) {
            return;
        }

        if (!hasCurioEquipped(player, ModItems.DRAGON_CRYSTAL_CROWN.get())) {
            return;
        }

        //如果已经是虚空伤害，不再转换
        if (isOutOfWorldDamage(event.getSource())) {
            return;
        }

        //取消原伤害，转换为虚空伤害
        float damage = event.getAmount();
        event.setCanceled(true);

        //清除无敌帧，让虚空伤害能够生效
        player.invulnerableTime = 0;

        try {
            CONVERTING_DAMAGE.set(true);
            player.hurt(player.damageSources().fellOutOfWorld(), damage);
        } finally {
            CONVERTING_DAMAGE.set(false);
        }
    }

    //检查玩家是否装备了指定的Curios饰品
    public static boolean hasCurioEquipped(LivingEntity entity, Item item) {
        return CuriosApi.getCuriosInventory(entity).map(handler -> {
            var curios = handler.getCurios();
            for (var entry : curios.entrySet()) {
                IDynamicStackHandler stacks = entry.getValue().getStacks();
                for (int i = 0; i < stacks.getSlots(); i++) {
                    ItemStack stack = stacks.getStackInSlot(i);
                    if (stack.getItem() == item) {
                        return true;
                    }
                }
            }
            return false;
        }).orElse(false);
    }
}
