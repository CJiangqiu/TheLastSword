package net.the_last_sword.compat.projecte.client;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.the_last_sword.compat.projecte.TransmutationStaffBehavior;

public class TransmutationStaffClientExtension implements IClientItemExtensions {
    private final IClientItemExtensions poseExtension;
    private StaffOfUniversalTransmutationRenderer<?> renderer;

    public TransmutationStaffClientExtension(IClientItemExtensions poseExtension) {
        this.poseExtension = poseExtension;
    }

    @Override
    public HumanoidModel.ArmPose getArmPose(LivingEntity entity, InteractionHand hand, ItemStack stack) {
        return TransmutationStaffBehavior.isStaffMode(stack) ? poseExtension.getArmPose(entity, hand, stack) : null;
    }

    @Override
    public BlockEntityWithoutLevelRenderer getCustomRenderer() {
        if (renderer == null) {
            renderer = new StaffOfUniversalTransmutationRenderer<>();
        }
        return renderer;
    }
}
