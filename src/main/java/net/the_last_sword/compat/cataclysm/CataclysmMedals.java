package net.the_last_sword.compat.cataclysm;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import top.theillusivec4.curios.api.SlotContext;

import java.util.List;

//Cataclysm各Boss奖章实现
public class CataclysmMedals {

    //远古残骸奖章
    public static class AncientRemnantMedal extends CataclysmMedal {
        public AncientRemnantMedal() {
            super("ancient_remnant_medal");
        }

        @Override
        public String getDefaultBoundEntityId() {
            return "cataclysm:ancient_remnant";
        }

        @Override
        public String getDefaultBoundEntityDisplayName() {
            return "entity.cataclysm.ancient_remnant";
        }

        //奖章的被动效果（在背包和饰品槽都会生效）
        protected void applyPassiveEffects(Player player, ItemStack stack) {
            //免疫沙漠诅咒效果
            MobEffect curseOfDesertEffect = getCurseOfDesertEffect();
            if (curseOfDesertEffect != null && player.hasEffect(curseOfDesertEffect)) {
                player.removeEffect(curseOfDesertEffect);
            }

            //免疫缓慢效果
            if (player.hasEffect(MobEffects.MOVEMENT_SLOWDOWN)) {
                player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
            }

            //处理shift主动技能
            handleShiftActiveSkill(player, stack);
        }

        @Override
        public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean selected) {
            super.inventoryTick(stack, world, entity, slot, selected);

            if (entity instanceof Player player && !world.isClientSide) {
                applyPassiveEffects(player, stack);
            }
        }

        @Override
        public void curioTick(SlotContext slotContext, ItemStack stack) {
            if (slotContext.entity() instanceof Player player && !player.level().isClientSide) {
                applyPassiveEffects(player, stack);
            }
        }

        private void handleShiftActiveSkill(Player player, ItemStack stack) {
            if (player.isCrouching() && !isOnCooldown(player, stack)) {
                //获取周围5格范围内的生物实体
                List<LivingEntity> nearbyLivingEntities =
                        player.level().getEntitiesOfClass(LivingEntity.class,
                                player.getBoundingBox().inflate(5.0));

                //对生物实体施加缓慢效果
                for (LivingEntity target : nearbyLivingEntities) {
                    if (target != player && target.isAlive()) {
                        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 2));
                    }
                }

                //获取周围5格范围内的弹射物
                List<Projectile> nearbyProjectiles =
                        player.level().getEntitiesOfClass(Projectile.class,
                                player.getBoundingBox().inflate(5.0));

                //弹开弹射物
                for (Projectile projectile : nearbyProjectiles) {
                    if (projectile.isAlive()) {
                        double deltaX = projectile.getX() - player.getX();
                        double deltaY = projectile.getY() - player.getY();
                        double deltaZ = projectile.getZ() - player.getZ();

                        double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
                        if (distance > 0) {
                            double force = 2.0;
                            deltaX = (deltaX / distance) * force;
                            deltaY = (deltaY / distance) * force + 0.3;
                            deltaZ = (deltaZ / distance) * force;
                            projectile.setDeltaMovement(deltaX, deltaY, deltaZ);
                        }
                    }
                }
            }
        }

        private boolean isOnCooldown(Player player, ItemStack stack) {
            return player.getCooldowns().isOnCooldown(stack.getItem());
        }

        private void setCooldown(Player player, ItemStack stack, int ticks) {
            player.getCooldowns().addCooldown(stack.getItem(), ticks);
        }

        //被攻击时的反击效果
        public void onOwnerAttacked(Player owner, ItemStack stack, LivingEntity attacker) {
            if (owner.isCrouching() && !isOnCooldown(owner, stack)) {
                MobEffect curseOfDesertEffect = getCurseOfDesertEffect();
                if (curseOfDesertEffect != null) {
                    attacker.addEffect(new MobEffectInstance(curseOfDesertEffect, 200, 0));
                }

                MobEffect stunEffect = getStunEffect();
                if (stunEffect != null) {
                    attacker.addEffect(new MobEffectInstance(stunEffect, 200, 0));
                }

                setCooldown(owner, stack, 200);
            }
        }

        private MobEffect getCurseOfDesertEffect() {
            try {
                return ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation("cataclysm", "curse_of_desert"));
            } catch (Exception ignored) {
                return null;
            }
        }

        private MobEffect getStunEffect() {
            try {
                return ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation("cataclysm", "stun"));
            } catch (Exception ignored) {
                return null;
            }
        }

        @Override
        public void appendHoverText(ItemStack stack, Level world, List<Component> tooltip, TooltipFlag flag) {
            super.appendHoverText(stack, world, tooltip, flag);
            tooltip.add(Component.translatable("item_tooltip.the_last_sword.ancient_remnant_medal"));
        }
    }

    //末影守卫者奖章
    public static class EnderGuardianMedal extends CataclysmMedal {
        public EnderGuardianMedal() {
            super("ender_guardian_medal");
        }

        @Override
        public String getDefaultBoundEntityId() {
            return "cataclysm:ender_guardian";
        }

        @Override
        public String getDefaultBoundEntityDisplayName() {
            return "entity.cataclysm.ender_guardian";
        }

        @Override
        public void appendHoverText(ItemStack stack, Level world, List<Component> tooltip, TooltipFlag flag) {
            super.appendHoverText(stack, world, tooltip, flag);
            tooltip.add(Component.translatable("item_tooltip.the_last_sword.ender_guardian_medal"));
        }
    }

    //伊格尼斯奖章
    public static class IgnisMedal extends CataclysmMedal {
        public IgnisMedal() {
            super("ignis_medal");
        }

        @Override
        public String getDefaultBoundEntityId() {
            return "cataclysm:ignis";
        }

        @Override
        public String getDefaultBoundEntityDisplayName() {
            return "entity.cataclysm.ignis";
        }

        @Override
        public void appendHoverText(ItemStack stack, Level world, List<Component> tooltip, TooltipFlag flag) {
            super.appendHoverText(stack, world, tooltip, flag);
            tooltip.add(Component.translatable("item_tooltip.the_last_sword.ignis_medal"));
        }
    }

    //马莱迪克斯奖章
    public static class MaledictusMedal extends CataclysmMedal {
        public MaledictusMedal() {
            super("maledictus_medal");
        }

        @Override
        public String getDefaultBoundEntityId() {
            return "cataclysm:maledictus";
        }

        @Override
        public String getDefaultBoundEntityDisplayName() {
            return "entity.cataclysm.maledictus";
        }

        @Override
        public void appendHoverText(ItemStack stack, Level world, List<Component> tooltip, TooltipFlag flag) {
            super.appendHoverText(stack, world, tooltip, flag);
            tooltip.add(Component.translatable("item_tooltip.the_last_sword.maledictus_medal"));
        }
    }

    //下界合金巨兽奖章
    public static class NetheriteMonstrosityMedal extends CataclysmMedal {
        public NetheriteMonstrosityMedal() {
            super("netherite_monstrosity_medal");
        }

        @Override
        public String getDefaultBoundEntityId() {
            return "cataclysm:netherite_monstrosity";
        }

        @Override
        public String getDefaultBoundEntityDisplayName() {
            return "entity.cataclysm.netherite_monstrosity";
        }

        //奖章的被动效果（在背包和饰品槽都会生效）
        protected void applyPassiveEffects(Player player, ItemStack stack) {
            //给予3级抗火效果
            player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 60, 2, false, false));

            //如果玩家着火，给予1级生命回复效果
            if (player.isOnFire()) {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 0, false, false));
            }
        }

        @Override
        public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean selected) {
            super.inventoryTick(stack, world, entity, slot, selected);

            if (entity instanceof Player player && !world.isClientSide) {
                applyPassiveEffects(player, stack);
            }
        }

        @Override
        public void curioTick(SlotContext slotContext, ItemStack stack) {
            if (slotContext.entity() instanceof Player player && !player.level().isClientSide) {
                applyPassiveEffects(player, stack);
            }
        }

        @Override
        public void appendHoverText(ItemStack stack, Level world, List<Component> tooltip, TooltipFlag flag) {
            super.appendHoverText(stack, world, tooltip, flag);
            tooltip.add(Component.translatable("item_tooltip.the_last_sword.netherite_monstrosity_medal"));
        }
    }

    //先驱者奖章
    public static class TheHarbingerMedal extends CataclysmMedal {
        public TheHarbingerMedal() {
            super("the_harbinger_medal");
        }

        @Override
        public String getDefaultBoundEntityId() {
            return "cataclysm:the_harbinger";
        }

        @Override
        public String getDefaultBoundEntityDisplayName() {
            return "entity.cataclysm.the_harbinger";
        }

        //奖章的被动效果（在背包和饰品槽都会生效）
        protected void applyPassiveEffects(Player player, ItemStack stack) {
            //免疫凋零效果
            if (player.hasEffect(MobEffects.WITHER)) {
                player.removeEffect(MobEffects.WITHER);
            }
        }

        @Override
        public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean selected) {
            super.inventoryTick(stack, world, entity, slot, selected);

            if (entity instanceof Player player && !world.isClientSide) {
                applyPassiveEffects(player, stack);
            }
        }

        @Override
        public void curioTick(SlotContext slotContext, ItemStack stack) {
            if (slotContext.entity() instanceof Player player && !player.level().isClientSide) {
                applyPassiveEffects(player, stack);
            }
        }

        @Override
        public void appendHoverText(ItemStack stack, Level world, List<Component> tooltip, TooltipFlag flag) {
            super.appendHoverText(stack, world, tooltip, flag);
            tooltip.add(Component.translatable("item_tooltip.the_last_sword.harbinger_medal"));
        }
    }

    //利维坦奖章
    public static class TheLeviathanMedal extends CataclysmMedal {
        public TheLeviathanMedal() {
            super("the_leviathan_medal");
        }

        @Override
        public String getDefaultBoundEntityId() {
            return "cataclysm:the_leviathan";
        }

        @Override
        public String getDefaultBoundEntityDisplayName() {
            return "entity.cataclysm.the_leviathan";
        }

        //奖章的被动效果（在背包和饰品槽都会生效）
        protected void applyPassiveEffects(Player player, ItemStack stack) {
            //给予3级潮涌能量效果
            player.addEffect(new MobEffectInstance(MobEffects.CONDUIT_POWER, 60, 2, false, false));

            //给予3级海豚的恩惠效果
            player.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 60, 2, false, false));
        }

        @Override
        public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean selected) {
            super.inventoryTick(stack, world, entity, slot, selected);

            if (entity instanceof Player player && !world.isClientSide) {
                applyPassiveEffects(player, stack);
            }
        }

        @Override
        public void curioTick(SlotContext slotContext, ItemStack stack) {
            if (slotContext.entity() instanceof Player player && !player.level().isClientSide) {
                applyPassiveEffects(player, stack);
            }
        }

        @Override
        public void appendHoverText(ItemStack stack, Level world, List<Component> tooltip, TooltipFlag flag) {
            super.appendHoverText(stack, world, tooltip, flag);
            tooltip.add(Component.translatable("item_tooltip.the_last_sword.the_leviathan_medal"));
        }
    }

    //斯库拉奖章
    public static class ScyllaMedal extends CataclysmMedal {
        public ScyllaMedal() {
            super("scylla_medal");
        }

        @Override
        public String getDefaultBoundEntityId() {
            return "cataclysm:scylla";
        }

        @Override
        public String getDefaultBoundEntityDisplayName() {
            return "entity.cataclysm.scylla";
        }

        //右键切换天气
        @Override
        public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
            ItemStack stack = player.getItemInHand(hand);

            if (player.getCooldowns().isOnCooldown(stack.getItem())) {
                return InteractionResultHolder.fail(stack);
            }

            if (world instanceof ServerLevel serverLevel) {
                if (serverLevel.isRaining()) {
                    if (serverLevel.isThundering()) {
                        serverLevel.setWeatherParameters(6000, 0, false, false);
                    } else {
                        serverLevel.setWeatherParameters(0, 6000, true, true);
                    }
                } else {
                    serverLevel.setWeatherParameters(0, 6000, true, false);
                }
                player.getCooldowns().addCooldown(stack.getItem(), 600);
            }

            return InteractionResultHolder.success(stack);
        }

        //攻击时的被动效果
        public void onOwnerAttack(Player owner, ItemStack stack, LivingEntity target) {
            Level world = owner.level();
            if (world.isClientSide) return;

            //给予/叠加敌人潮湿效果
            MobEffect moistureEffect = getMoistureEffect();
            if (moistureEffect != null) {
                MobEffectInstance currentEffect = target.getEffect(moistureEffect);
                int newAmplifier = currentEffect != null ? Math.min(currentEffect.getAmplifier() + 1, 4) : 0;
                target.addEffect(new MobEffectInstance(moistureEffect, 60, newAmplifier));
            }

            //检查天气和潮湿效果触发落雷
            if (moistureEffect != null && target.hasEffect(moistureEffect)) {
                boolean isRaining = world.isRaining();
                boolean isThundering = world.isThundering();

                if (isRaining || isThundering) {
                    double triggerChance = isThundering ? 0.5 : 0.25;

                    if (world.random.nextDouble() < triggerChance) {
                        int moistureLevel = target.getEffect(moistureEffect).getAmplifier() + 1;
                        float lightningDamage = moistureLevel * 5.0f;

                        LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(world);
                        if (lightning != null) {
                            lightning.moveTo(target.getX(), target.getY(), target.getZ());
                            lightning.setVisualOnly(false);
                            world.addFreshEntity(lightning);
                            //清除无敌帧，确保额外伤害能够生效
                            target.invulnerableTime = 0;
                            target.hurt(world.damageSources().lightningBolt(), lightningDamage);
                        }
                    }
                }
            }
        }

        private MobEffect getMoistureEffect() {
            try {
                return ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation("cataclysm", "wetness"));
            } catch (Exception ignored) {
                return null;
            }
        }

        @Override
        public void appendHoverText(ItemStack stack, Level world, List<Component> tooltip, TooltipFlag flag) {
            super.appendHoverText(stack, world, tooltip, flag);
            tooltip.add(Component.translatable("item_tooltip.the_last_sword.scylla_medal"));
        }
    }
}
