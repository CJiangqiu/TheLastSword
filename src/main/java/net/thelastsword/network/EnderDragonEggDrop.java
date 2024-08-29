package net.thelastsword.network;

import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;

import net.thelastsword.configuration.TheLastSwordConfiguration;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@Mod.EventBusSubscriber
public class EnderDragonEggDrop {
    private static final Set<String> playerList = new HashSet<>();

    @SubscribeEvent
    public static void onEntityAttacked(LivingAttackEvent event) {
        if (event != null && event.getEntity() != null) {
            executeAttack(event, event.getEntity(), event.getSource().getEntity(), event.getAmount());
        }
    }

    public static void executeAttack(Entity entity, Entity sourceentity, double amount) {
        executeAttack(null, entity, sourceentity, amount);
    }

    private static void executeAttack(@Nullable Event event, Entity entity, Entity sourceentity, double amount) {
        if (entity == null || sourceentity == null)
            return;
        if (entity instanceof EnderDragon && sourceentity instanceof Player) {
            if (amount >= ((LivingEntity) entity).getMaxHealth() * 0.1) {
                playerList.add(sourceentity.getDisplayName().getString());
            }
        }
    }

    @SubscribeEvent
    public static void onEntityDeath(LivingDeathEvent event) {
        if (event != null && event.getEntity() != null) {
            executeDeath(event, event.getEntity().getX(), event.getEntity().getY(), event.getEntity().getZ(), event.getEntity());
        }
    }

    public static void executeDeath(LivingDeathEvent event, double x, double y, double z, Entity entity) {
        executeDeath(null, event, x, y, z, entity);
    }

    private static void executeDeath(@Nullable Event event, LivingDeathEvent originalEvent, double x, double y, double z, Entity entity) {
        if (entity == null)
            return;
        if (entity instanceof EnderDragon) {
            if (TheLastSwordConfiguration.DROP_DRAGON_EGG.get()) {
                Level world = originalEvent.getEntity().getCommandSenderWorld();
                CompletableFuture.runAsync(() -> {
                    if (TheLastSwordConfiguration.MULTIPLE_DRAGON_EGGS.get()) {
                        for (Player player : world.players()) {
                            if (playerList.contains(player.getDisplayName().getString())) {
                                ItemStack dragonEgg = new ItemStack(Blocks.DRAGON_EGG);
                                ItemHandlerHelper.giveItemToPlayer(player, dragonEgg);
                            }
                        }
                    } else {
                        ItemStack dragonEgg = new ItemStack(Blocks.DRAGON_EGG);
                        entity.spawnAtLocation(dragonEgg);
                    }
                });
            }
        }
    }
}
