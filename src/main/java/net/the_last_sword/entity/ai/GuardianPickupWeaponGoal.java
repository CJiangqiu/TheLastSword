package net.the_last_sword.entity.ai;

import java.util.EnumSet;
import java.util.List;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.entity.GuardianOfSealedSpireEntity;

//守卫拾取武器Goal
public class GuardianPickupWeaponGoal extends Goal {
    private static final double ITEM_REACH_DISTANCE = 1.5;

    private final GuardianOfSealedSpireEntity guardian;
    private ItemEntity targetItem;
    private int scanCooldown;

    public GuardianPickupWeaponGoal(GuardianOfSealedSpireEntity guardian) {
        this.guardian = guardian;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!shouldPickup()) {
            return false;
        }

        if (scanCooldown > 0) {
            scanCooldown--;
            return false;
        }

        targetItem = findNearbyValidItem();
        scanCooldown = TheLastSwordConfiguration.getGuardianPickupScanIntervalSafely();
        return targetItem != null;
    }

    @Override
    public boolean canContinueToUse() {
        if (!shouldPickup()) {
            return false;
        }
        return targetItem != null && targetItem.isAlive();
    }

    @Override
    public void start() {
        if (targetItem != null) {
            guardian.getNavigation().moveTo(targetItem, 1.0);
        }
    }

    @Override
    public void tick() {
        if (!shouldPickup()) {
            return;
        }

        if (targetItem == null || !targetItem.isAlive()) {
            if (scanCooldown <= 0) {
                targetItem = findNearbyValidItem();
                scanCooldown = TheLastSwordConfiguration.getGuardianPickupScanIntervalSafely();
            }
            return;
        }

        double distance = guardian.distanceTo(targetItem);
        if (distance > ITEM_REACH_DISTANCE) {
            guardian.getNavigation().moveTo(targetItem, 1.0);
        } else {
            pickupItem(targetItem);
        }
    }

    @Override
    public void stop() {
        targetItem = null;
        guardian.getNavigation().stop();
    }

    private boolean shouldPickup() {
        if (guardian.getClass() != GuardianOfSealedSpireEntity.class) {
            return false;
        }
        return guardian.getMainHandItem().isEmpty();
    }

    private ItemEntity findNearbyValidItem() {
        List<ItemEntity> nearbyItems = guardian.level().getEntitiesOfClass(
            ItemEntity.class,
            guardian.getBoundingBox().inflate(TheLastSwordConfiguration.getGuardianPickupDistanceSafely()),
            item -> item.isAlive() && isValidWeaponItem(item.getItem())
        );

        if (nearbyItems.isEmpty()) {
            return null;
        }

        ItemEntity nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (ItemEntity item : nearbyItems) {
            double distance = guardian.distanceTo(item);
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearest = item;
            }
        }

        return nearest;
    }

    private boolean isValidWeaponItem(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        return stack.getItem() instanceof SwordItem
            || stack.getItem() instanceof BowItem
            || stack.getItem() instanceof AxeItem;
    }

    private void pickupItem(ItemEntity itemEntity) {
        ItemStack stack = itemEntity.getItem();
        guardian.setItemSlot(EquipmentSlot.MAINHAND, stack.copy());
        itemEntity.discard();
        guardian.getNavigation().stop();
        targetItem = null;
    }
}
