package net.thelastsword.item;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.thelastsword.capability.ExtraAttackDamageProvider;
import net.thelastsword.capability.ModeProvider;
import net.thelastsword.capability.SwordCapability;
import net.thelastsword.capability.SwordLevelProvider;
import net.thelastsword.configuration.TheLastSwordConfiguration;
import net.thelastsword.event.JustifiedDefense;
import net.thelastsword.entity.TheLastSwordProjectile;
import net.thelastsword.event.PowerfulRangeAttack;
import net.thelastsword.init.TheLastSwordModItems;

import java.util.List;

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
                return 196f;
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

    // Check if the item has an enchanted or special foil effect/检查物品是否具有附魔或特殊的闪光效果
    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean isFoil(ItemStack itemstack) {
        return itemstack.getCapability(SwordCapability.MODE_CAPABILITY).map(modeCapability -> modeCapability.getMode() == 2).orElse(false);
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
                    player.getPersistentData().putBoolean("TheLastSwordFly", true);
                    player.onUpdateAbilities();
                }
                if (!JustifiedDefense.hasJustifiedDefense(player)) {
                    JustifiedDefense.applyJustifiedDefense(player);
                }

            } else {
                if (player.getPersistentData().getBoolean("TheLastSwordFly")) {
                    player.getAbilities().mayfly = false;
                    player.getAbilities().flying = false;
                    player.getPersistentData().remove("TheLastSwordFly");
                    player.onUpdateAbilities();
                }
                if (JustifiedDefense.hasJustifiedDefense(player)) {
                    JustifiedDefense.removeJustifiedDefense(player);
                }
            }
        }
    }
}

// Event handler for player tick/玩家tick事件处理
@Mod.EventBusSubscriber
public static class TheLastSwordEventHandler {

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        Player player = event.player;
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
                    player.getPersistentData().putBoolean("TheLastSwordFly", true);
                    player.onUpdateAbilities();
                }
                if (!JustifiedDefense.hasJustifiedDefense(player)) {
                    JustifiedDefense.applyJustifiedDefense(player);
                }

            } else {
                if (player.getPersistentData().getBoolean("TheLastSwordFly")) {
                    player.getAbilities().mayfly = false;
                    player.getAbilities().flying = false;
                    player.getPersistentData().remove("TheLastSwordFly");
                    player.onUpdateAbilities();
                }
                if (JustifiedDefense.hasJustifiedDefense(player)) {
                    JustifiedDefense.removeJustifiedDefense(player);
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

                    // Drop experience if the block has experience drops/如果方块有经验掉落，则掉落经验
                    int exp = blockState.getBlock().getExpDrop(blockState, (ServerLevel) world, RandomSource.create(), pos, fortuneLevel, 0);
                    if (exp > 0) {
                        blockState.getBlock().popExperience((ServerLevel) world, pos, exp);
                    }

                    // Ensure original block is removed/确保移除原有方块
                    if (blockState.getDestroySpeed(world, pos) < 0 || blockState.getBlock() == Blocks.BEDROCK || blockState.getBlock() == Blocks.BARRIER) {
                        // If block is unbreakable, drop its item form/如果方块不可破坏，则掉落其物品形式
                        Block.popResource(world, pos, new ItemStack(blockState.getBlock().asItem()));
                        world.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                    } else {
                        world.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                    }
                    itemstack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(hand));
                }
            } else if (mode == 2) {
                // Powerful range attack mode: Right-click for range attack/强力范围攻击模式：右键进行范围攻击
                long currentTime = System.currentTimeMillis();
                int cooldown = TheLastSwordConfiguration.RANGE_ATTACK_COOLDOWN.get();
                if (currentTime - lastRangeAttackTime >= cooldown) {
                    if (!world.isClientSide) {
                        PowerfulRangeAttack.performPowerfulRangeAttack(world, player, itemstack);
                        itemstack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(hand));
                        lastRangeAttackTime = currentTime;
                    }
                }
            }
        });
        return InteractionResultHolder.success(itemstack);
    }



    @Override
    public void appendHoverText(ItemStack itemstack, Level level, List<Component> list, TooltipFlag flag) {
        super.appendHoverText(itemstack, level, list, flag);

        itemstack.getCapability(SwordCapability.SWORD_LEVEL_CAPABILITY).ifPresent(swordLevel -> {
            list.add(Component.translatable("item_tooltip.the_last_sword.sword_level").append(" " + swordLevel.getLevel()));
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