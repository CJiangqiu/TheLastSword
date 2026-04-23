package net.the_last_sword.compat.jade;

import net.minecraft.world.entity.LivingEntity;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

//Jade 插件入口: Jade 存在时由 Jade 扫描加载
@WailaPlugin
public class TheLastSwordJadePlugin implements IWailaPlugin {

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        //registerEntityComponent 内部自动调 tryAddConfig 注册 config (enabledByDefault=true)
        //不要手动再调 addConfig, 会因 UID 重复触发 Preconditions 抛异常
        registration.registerEntityComponent(JustifiedDefenceProvider.INSTANCE, LivingEntity.class);
        registration.markAsClientFeature(JustifiedDefenceProvider.UID);

        registration.registerEntityComponent(HealBanProvider.INSTANCE, LivingEntity.class);
        registration.markAsClientFeature(HealBanProvider.UID);
    }
}
