package net.the_last_sword.compat.projecte;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.the_last_sword.TheLastSwordMod;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import top.theillusivec4.curios.api.CuriosApi;

@Mod.EventBusSubscriber(modid = TheLastSwordMod.MOD_ID)
public final class TransmutationStaffField {
    public static final int FADE_TICKS = 10;
    private static final int ITEM_COOLDOWN_TICKS = 30 * 20;
    private static final Map<ServerLevel, List<Field>> FIELDS = new WeakHashMap<>();

    private TransmutationStaffField() {
    }

    public static void disableItems(ServerPlayer target) {
        Set<Item> items = new HashSet<>();
        for (int slot = 0; slot < target.getInventory().getContainerSize(); slot++) {
            collect(items, target.getInventory().getItem(slot));
        }
        collect(items, target.containerMenu.getCarried());
        CuriosApi.getCuriosInventory(target).ifPresent(handler -> handler.getCurios().values().forEach(slots -> {
            for (int slot = 0; slot < slots.getStacks().getSlots(); slot++) {
                collect(items, slots.getStacks().getStackInSlot(slot));
            }
        }));
        target.stopUsingItem();
        // 原版按物品类型共享冷却，重复物品只需同步一次。
        for (Item item : items) {
            target.getCooldowns().addCooldown(item, ITEM_COOLDOWN_TICKS);
        }
    }

    private static void collect(Set<Item> items, ItemStack stack) {
        if (!stack.isEmpty()) {
            items.add(stack.getItem());
        }
    }

    public static void release(ServerLevel level, Vec3 center, double radius) {
        clearProjectiles(level, center, radius);
        FIELDS.computeIfAbsent(level, ignored -> new ArrayList<>())
            .add(new Field(center, radius, level.getGameTime() + FADE_TICKS));
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.START || !(event.level instanceof ServerLevel level)) {
            return;
        }
        for (ServerPlayer player : level.players()) {
            ItemStack stack = player.getUseItem();
            if (player.isAlive() && player.isUsingItem() && TransmutationStaffBehavior.isStaff(stack)
                && TransmutationStaffBehavior.isTermination(stack)) {
                double progress = Math.min(1.0, (double) player.getTicksUsingItem()
                    / TransmutationStaffBehavior.getTerminationUseTicks());
                clearProjectiles(level, player.position(),
                    TheLastSwordConfiguration.getTransmutationStaffTerminationRadiusSafely() * progress);
            }
        }
        List<Field> fields = FIELDS.get(level);
        if (fields != null) {
            fields.removeIf(field -> level.getGameTime() >= field.expiresAt());
            for (Field field : fields) {
                clearProjectiles(level, field.center(), field.radius());
            }
            if (fields.isEmpty()) {
                FIELDS.remove(level);
            }
        }
    }

    private static void clearProjectiles(ServerLevel level, Vec3 center, double radius) {
        if (radius <= 0.0) {
            return;
        }
        for (Projectile projectile : level.getEntitiesOfClass(Projectile.class,
            new AABB(center, center).inflate(radius),
            entity -> entity.position().distanceToSqr(center) <= radius * radius)) {
            projectile.discard();
        }
    }

    @SubscribeEvent
    public static void onUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel level) {
            FIELDS.remove(level);
        }
    }

    @SubscribeEvent
    public static void onStop(ServerStoppedEvent event) {
        FIELDS.clear();
    }

    private record Field(Vec3 center, double radius, long expiresAt) {
    }
}
