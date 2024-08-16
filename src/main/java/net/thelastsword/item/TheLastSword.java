package net.thelastsword.item;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.common.Mod;
import net.thelastsword.capability.*;
import net.thelastsword.configuration.TheLastSwordConfiguration;
import net.thelastsword.entity.TheLastSwordProjectile;
import net.thelastsword.init.TheLastSwordModItems;

import java.lang.reflect.Method;
import java.util.List;

@Mod.EventBusSubscriber
public class TheLastSword extends SwordItem implements ICapabilityProvider {
    private final float baseAttackDamage;
    private long lastRangeAttackTime = 0;

    // Constructor/构造函数
    public TheLastSword() {
        super(new Tier() {
            public int getUses() {
                return 0;
            }

            public float getSpeed() {
                return 1024f;
            }

            public float getAttackDamageBonus() {
                return 1022f;
            }

            public int getLevel() {
                return 1024;
            }

            public int getEnchantmentValue() {
                return 1024;
            }

            public Ingredient getRepairIngredient() {
                return Ingredient.of(new ItemStack(TheLastSwordModItems.DRAGON_CRYSTAL.get()));
            }
        }, 3, -2.4f, new Item.Properties().fireResistant().rarity(Rarity.EPIC));
        this.baseAttackDamage = 1022f;
    }

    // Get base attack damage/获取基础攻击伤害
    public float getBaseAttackDamage() {
        return baseAttackDamage;
    }
    // Mining functionality/挖掘功能
    // Check if the tool is correct for drops/检查工具是否适合掉落物
    @Override
    public boolean isCorrectToolForDrops(BlockState blockstate) {
        int tier = 1024;
        if (tier < 3 && blockstate.is(BlockTags.NEEDS_DIAMOND_TOOL)) {
            return false;
        } else if (tier < 2 && blockstate.is(BlockTags.NEEDS_IRON_TOOL)) {
            return false;
        } else {
            return tier < 1 && blockstate.is(BlockTags.NEEDS_STONE_TOOL)
                    ? false
                    : (blockstate.is(BlockTags.MINEABLE_WITH_AXE) || blockstate.is(BlockTags.MINEABLE_WITH_HOE) || blockstate.is(BlockTags.MINEABLE_WITH_PICKAXE) || blockstate.is(BlockTags.MINEABLE_WITH_SHOVEL));
        }
    }
    // Check if the tool can perform action/检查工具是否可以执行动作
    @Override
    public boolean canPerformAction(ItemStack stack, ToolAction toolAction) {
        return ToolActions.DEFAULT_AXE_ACTIONS.contains(toolAction) || ToolActions.DEFAULT_HOE_ACTIONS.contains(toolAction) || ToolActions.DEFAULT_SHOVEL_ACTIONS.contains(toolAction) || ToolActions.DEFAULT_PICKAXE_ACTIONS.contains(toolAction)
                || ToolActions.DEFAULT_SWORD_ACTIONS.contains(toolAction);
    }
    // Get destroy speed/获取破坏速度
    @Override
    public float getDestroySpeed(ItemStack itemstack, BlockState blockstate) {
        return 1024f;
    }
    // Handle block mining/处理方块挖掘
    @Override
    public boolean mineBlock(ItemStack itemstack, Level world, BlockState blockstate, BlockPos pos, LivingEntity entity) {
        itemstack.hurtAndBreak(1, entity, i -> i.broadcastBreakEvent(EquipmentSlot.MAINHAND));
        return true;
    }
    // Mining functionality/挖掘功能

    // Check if the item has an enchanted or special foil effect/检查物品是否具有附魔或特殊的闪光效果
    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean isFoil(ItemStack itemstack) {
        return true;
    }

    // Get capability/获取能力
    @Override
    public <T> LazyOptional<T> getCapability(net.minecraftforge.common.capabilities.Capability<T> cap, net.minecraft.core.Direction side) {
        if (cap == SwordCapability.SWORD_LEVEL_CAPABILITY) {
            return LazyOptional.of(() -> (T) new SwordLevelProvider(13)).cast();
        } else if (cap == SwordCapability.EXTRA_ATTACK_DAMAGE_CAPABILITY) {
            return LazyOptional.of(() -> (T) new ExtraAttackDamageProvider(13)).cast();
        } else if (cap == SwordCapability.MODE_CAPABILITY) {
            return LazyOptional.of(() -> (T) new ModeProvider(0)).cast();
        }
        return LazyOptional.empty();
    }
    // Handle enemy hit/处理敌人击中
    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        stack.getCapability(SwordCapability.SWORD_LEVEL_CAPABILITY).ifPresent(swordLevel -> {
            int level = swordLevel.getLevel();
            double increaseValue = TheLastSwordConfiguration.INCREASE_VALUE.get();
            double increaseValueHighLevel = TheLastSwordConfiguration.INCREASE_VALUE_HIGH_LEVEL.get();
            double extraDamage = (level < 6 ? increaseValue : increaseValueHighLevel) * level;

            stack.getCapability(SwordCapability.EXTRA_ATTACK_DAMAGE_CAPABILITY).ifPresent(extraAttackDamage -> {
                extraAttackDamage.setExtraAttackDamage((float) extraDamage);
            });

            float totalDamage = (float) (extraDamage + this.getBaseAttackDamage());
            target.hurt(new DamageSource(attacker.getCommandSenderWorld().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.GENERIC)), totalDamage);
        });
        return super.hurtEnemy(stack, target, attacker);
    }
    // Handle inventory tick/处理在背包中时
    @Override
    public void inventoryTick(ItemStack itemstack, Level world, Entity entity, int slot, boolean selected) {
        super.inventoryTick(itemstack, world, entity, slot, selected);
        if (entity instanceof Player player) {
            boolean hasSword = false;
            for (ItemStack stack : player.getInventory().items) {
                if (stack.getItem() instanceof TheLastSword) {
                    hasSword = true;
                    break;
                }
            }
            if (!player.isCreative() && !player.isSpectator()) {
                boolean allowFlying = TheLastSwordConfiguration.ALLOW_FLYING.get();
                if (hasSword) {
                    if (allowFlying && !player.getAbilities().mayfly) {
                        player.getAbilities().mayfly = true;
                        player.onUpdateAbilities();
                    }
                    if (!player.getAbilities().invulnerable) {
                        player.getAbilities().invulnerable = true;
                    }
                } else {
                    if (player.getAbilities().mayfly) {
                        player.getAbilities().mayfly = false;
                        player.getAbilities().flying = false;
                        player.onUpdateAbilities();
                    }
                    if (player.getAbilities().invulnerable) {
                        player.getAbilities().invulnerable = false;
                    }
                }
            }
        }
    }

    // Handle item use/处理物品使用
@Override
public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
    ItemStack itemstack = player.getItemInHand(hand);
    itemstack.getCapability(SwordCapability.MODE_CAPABILITY).ifPresent(modeCapability -> {
        int mode = modeCapability.getMode();
        if (mode == 0) {
            // Normal mode: Right-click to shoot projectile/普通模式：右键发射弹射物
            if (!world.isClientSide) {
                TheLastSwordProjectile.shoot(world, player, RandomSource.create());
                itemstack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(hand));
            }
        } else if (mode == 1) {
            player.swing(hand);
            BlockHitResult hitResult = getPlayerPOVHitResult(world, player, ClipContext.Fluid.NONE);
            BlockPos pos = hitResult.getBlockPos();
            BlockState blockState = world.getBlockState(pos);
            if (!world.isClientSide && blockState.getBlock() instanceof Block) {
                // Calculate drops based on fortune enchantment/根据时运附魔计算掉落物
                int fortuneLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, itemstack);
                List<ItemStack> drops = Block.getDrops(blockState, (ServerLevel) world, pos, world.getBlockEntity(pos), player, itemstack);
                for (ItemStack drop : drops) {
                    if (fortuneLevel > 0) {
                        drop.setCount(drop.getCount() * (fortuneLevel + 1));
                    }
                    Block.popResource(world, pos, drop);
                }

                // Ensure original block is removed/确保移除原有方块
                world.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                itemstack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(hand));
            }
        } else if (mode == 2) {
                // Powerful range attack mode: Right-click for 64*64 range attack/强力范围攻击模式：右键进行64*64范围攻击
                long currentTime = System.currentTimeMillis();
                int cooldown = TheLastSwordConfiguration.RANGE_ATTACK_COOLDOWN.get();
                if (currentTime - lastRangeAttackTime >= cooldown) {
                    if (!world.isClientSide) {
                        performPowerfulRangeAttack(world, player, itemstack);
                        itemstack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(hand));
                        world.playSound(null, player.blockPosition(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 1.0F, 1.0F);
                        lastRangeAttackTime = currentTime;
                    }
                }
            }
        });
        return InteractionResultHolder.success(itemstack);
    }
    // Perform powerful range attack/执行强力范围攻击
    private void performPowerfulRangeAttack(Level world, Player player, ItemStack itemstack) {
        double range = TheLastSwordConfiguration.RANGE_ATTACK_RANGE.get(); // 使用配置文件中的范围值
        List<Entity> entities = world.getEntities(player, player.getBoundingBox().inflate(range), e ->
                e.isAlive() &&
                        !(e instanceof Player && !TheLastSwordConfiguration.ATTACK_PLAYERS.get()) &&
                        !(e instanceof ItemEntity) &&
                        !(e instanceof Villager && !TheLastSwordConfiguration.ATTACK_VILLAGERS.get()) &&
                        !(e instanceof TamableAnimal && ((TamableAnimal) e).isTame() && !TheLastSwordConfiguration.ATTACK_TAMED.get()) &&
                        !(e instanceof IronGolem && !TheLastSwordConfiguration.ATTACK_GOLEMS.get()) &&
                        !(e instanceof NeutralMob && !TheLastSwordConfiguration.ATTACK_NEUTRAL.get()) &&
                        !(e instanceof Animal && !TheLastSwordConfiguration.ATTACK_ANIMALS.get())
        );
        for (Entity entity : entities) {
            if (entity instanceof LivingEntity livingEntity) {
                // Get extra damage/获取额外伤害
                itemstack.getCapability(SwordCapability.EXTRA_ATTACK_DAMAGE_CAPABILITY).ifPresent(extraAttackDamage -> {
                    float extraDamage = extraAttackDamage.getExtraAttackDamage();
                    // Reduce entity's max health/降低实体的最大生命值
                    livingEntity.getAttribute(Attributes.MAX_HEALTH).setBaseValue(livingEntity.getAttribute(Attributes.MAX_HEALTH).getBaseValue() - extraDamage);
                    // Reduce entity's armor value/降低实体的盔甲值
                    livingEntity.getAttribute(Attributes.ARMOR).setBaseValue(livingEntity.getAttribute(Attributes.ARMOR).getBaseValue() - extraDamage);
                    // Reduce entity's attack damage/降低实体的攻击力
                    livingEntity.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(livingEntity.getAttribute(Attributes.ATTACK_DAMAGE).getBaseValue() - extraDamage);
                    // Deal extra damage to target/对目标造成额外伤害
                    livingEntity.hurt(new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.PLAYER_ATTACK)), extraDamage);
                    livingEntity.hurt(new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.EXPLOSION)), extraDamage);
                    livingEntity.hurt(new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.MAGIC)), extraDamage);
                    livingEntity.hurt(new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.DRAGON_BREATH)), extraDamage);
                    livingEntity.hurt(new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.FELL_OUT_OF_WORLD)), extraDamage);
                    // If entity's current max health is less than extra damage/如果实体的当前最大生命值低于额外伤害数值
                    if (livingEntity.getAttribute(Attributes.MAX_HEALTH).getBaseValue() < extraDamage) {
                        // Drop entity's loot/掉落实体的掉落物
                        try {
                            Method dropLoot = LivingEntity.class.getDeclaredMethod("dropFromLootTable", DamageSource.class, boolean.class);
                            dropLoot.setAccessible(true);
                            dropLoot.invoke(livingEntity, DamageTypes.GENERIC, true);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        // Disable entity's AI/设置关闭实体的AI
                        CompoundTag nbtData = new CompoundTag();
                        nbtData.putBoolean("NoAI", true);
                        livingEntity.load(nbtData);
                        // Remove entity/清除实体
                        livingEntity.setHealth(0);
                        livingEntity.remove(Entity.RemovalReason.KILLED);
                        livingEntity.remove(Entity.RemovalReason.CHANGED_DIMENSION);
                        livingEntity.remove(Entity.RemovalReason.DISCARDED);
                        livingEntity.remove(Entity.RemovalReason.UNLOADED_TO_CHUNK);
                        livingEntity.remove(Entity.RemovalReason.UNLOADED_WITH_PLAYER);
                        // Set entity invisible/设置实体不可见
                        livingEntity.setInvisible(true);
                        // Teleport entity to x, -1024, z/将实体位置设于 x, -1024, z 的位置
                        livingEntity.teleportTo(player.getX(), -1024, player.getZ());
                    }
                });
                // Summon visual lightning bolt at entity's location/在实体位置降下一道仅视觉效果的雷电
                if (world instanceof ServerLevel _level) {
                    LightningBolt lightningBolt = EntityType.LIGHTNING_BOLT.create(_level);
                    lightningBolt.moveTo(livingEntity.getX(), livingEntity.getY(), livingEntity.getZ());
                    lightningBolt.setVisualOnly(true);
                    _level.addFreshEntity(lightningBolt);
                }
                // Add cooldown/添加冷却时间
                int cooldown = TheLastSwordConfiguration.RANGE_ATTACK_COOLDOWN.get();
                player.getCooldowns().addCooldown(itemstack.getItem(), cooldown);
            }
        }
    }


    @Override
    public void appendHoverText(ItemStack itemstack, Level level, List<Component> list, TooltipFlag flag) {
        super.appendHoverText(itemstack, level, list, flag);

        itemstack.getCapability(SwordCapability.SWORD_LEVEL_CAPABILITY).ifPresent(swordLevel -> {
            list.add(Component.translatable("item_tooltip.the_last_sword.dragon_sword").append(" " + swordLevel.getLevel()));
        });

        itemstack.getCapability(SwordCapability.MODE_CAPABILITY).ifPresent(modeCapability -> {
            int mode = modeCapability.getMode();
            String modeText = switch (mode) {
                case 1 -> "item_tooltip.the_last_sword.powerful_mining_mode";
                case 2 -> "item_tooltip.the_last_sword.powerful_range_attack_mode";
                default -> "item_tooltip.the_last_sword.normal_mode";
            };
            list.add(Component.translatable("item_tooltip.the_last_sword.mode").append(Component.translatable(modeText)));

            if (Screen.hasShiftDown()) {
                String modeDescrText = switch (mode) {
                    case 1 -> Component.translatable("item_tooltip.the_last_sword.powerful_mining_mode_descr").getString();
                    case 2 -> Component.translatable("item_tooltip.the_last_sword.powerful_range_attack_mode_descr").getString();
                    default -> Component.translatable("item_tooltip.the_last_sword.normal_mode_descr").getString();
                };
                String[] lines = modeDescrText.split("\n");
                for (String line : lines) {
                    list.add(Component.literal(line).withStyle(style -> style.withColor(TextColor.fromRgb(0xAAAAAA))));
                }
            } else {
                list.add(Component.translatable("item_tooltip.the_last_sword.mode_descr")
                        .withStyle(style -> style.withColor(TextColor.fromRgb(0xAAAAAA))));
            }
        });

        itemstack.getCapability(SwordCapability.EXTRA_ATTACK_DAMAGE_CAPABILITY).ifPresent(extraAttackDamage -> {
            list.add(Component.translatable("item_tooltip.the_last_sword.extra_attack_damage")
                    .withStyle(style -> style.withColor(TextColor.fromRgb(0xAA00FF)))
                    .append(" " + extraAttackDamage.getExtraAttackDamage()));
        });
    }


}
