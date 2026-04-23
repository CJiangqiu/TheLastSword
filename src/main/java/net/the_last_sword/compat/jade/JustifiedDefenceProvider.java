package net.the_last_sword.compat.jade;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.the_last_sword.TheLastSwordMod;
import net.the_last_sword.init.ModAttributes;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

//Jade 实体组件提供者: 在实体浮标中显示肃正防御 (仅当前值 > 0 显示)
public enum JustifiedDefenceProvider implements IEntityComponentProvider {

    INSTANCE;

    public static final ResourceLocation UID = new ResourceLocation(TheLastSwordMod.MOD_ID, "justified_defence");

    @Override
    public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
        if (!(accessor.getEntity() instanceof LivingEntity living)) return;

        AttributeInstance currentAttr = living.getAttribute(ModAttributes.JUSTIFIED_DEFENCE.get());
        if (currentAttr == null) return;
        double current = currentAttr.getValue();
        if (current <= 0.0) return;

        AttributeInstance maxAttr = living.getAttribute(ModAttributes.MAX_JUSTIFIED_DEFENCE.get());
        double max = maxAttr != null ? maxAttr.getValue() : current;

        tooltip.add(new JustifiedDefenceElement(current, max));
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}
