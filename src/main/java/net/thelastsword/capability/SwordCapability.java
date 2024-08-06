package net.thelastsword.capability;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.thelastsword.item.DragonCrystalSword;
import net.thelastsword.item.DragonSword;
import net.thelastsword.item.TheLastSword;

@Mod.EventBusSubscriber(modid = "the_last_sword")
public class SwordCapability {
    public static Capability<ISwordLevel> SWORD_LEVEL_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});
    public static Capability<IExtraAttackDamage> EXTRA_ATTACK_DAMAGE_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});

    @SubscribeEvent
    public static void onRegisterCapabilitiesEvent(RegisterCapabilitiesEvent event) {
        event.register(ISwordLevel.class);
        event.register(IExtraAttackDamage.class);
    }

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {

    }

    @SubscribeEvent
    public static void attachItemCapabilities(AttachCapabilitiesEvent<ItemStack> event) {
        if (event.getObject().getItem() instanceof DragonCrystalSword) {
            event.addCapability(new ResourceLocation("the_last_sword", "sword_level"), new SwordLevelProvider(0)); // 设置默认剑等级为0
            event.addCapability(new ResourceLocation("the_last_sword", "extra_attack_damage"), new ExtraAttackDamageProvider(0)); // 设置默认额外攻击力为0
        }
        if (event.getObject().getItem() instanceof DragonSword) {
            event.addCapability(new ResourceLocation("the_last_sword","sword_level"), new SwordLevelProvider(6)); // 设置默认剑等级为6
            event.addCapability(new ResourceLocation("the_last_sword", "extra_attack_damage"), new ExtraAttackDamageProvider(6)); // 设置默认额外攻击力
        }
        if (event.getObject().getItem() instanceof TheLastSword) {
            event.addCapability(new ResourceLocation("the_last_sword","sword_level"), new SwordLevelProvider(13)); // 设置默认剑等级为6
            event.addCapability(new ResourceLocation("the_last_sword", "extra_attack_damage"), new ExtraAttackDamageProvider(13)); // 设置默认额外攻击力
        }
    }
}
