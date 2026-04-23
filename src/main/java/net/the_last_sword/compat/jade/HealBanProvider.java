package net.the_last_sword.compat.jade;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.the_last_sword.TheLastSwordMod;
import net.the_last_sword.util.EntityUtil;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

//Jade 实体组件提供者: 在实体浮标中显示禁疗剩余时间 (仅当剩余 > 0 显示)
public enum HealBanProvider implements IEntityComponentProvider {

    INSTANCE;

    public static final ResourceLocation UID = new ResourceLocation(TheLastSwordMod.MOD_ID, "heal_ban");

    @Override
    public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
        if (!(accessor.getEntity() instanceof LivingEntity living)) return;

        int remaining = EntityUtil.getHealBanTime(living);
        if (remaining <= 0) return;

        tooltip.add(new HealBanElement(remaining));
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}
