package net.the_last_sword.compat.curios;

import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

//Curios饰品条件注册类 - 只在Curios加载时才会被使用
public class CuriosItemsRegistry {

    //龙水晶指环
    public static RegistryObject<Item> registerDragonCrystalRing(DeferredRegister<Item> registry) {
        return registry.register("dragon_crystal_ring", DragonCrystalRing::new);
    }

    //龙水晶项链
    public static RegistryObject<Item> registerDragonCrystalNecklace(DeferredRegister<Item> registry) {
        return registry.register("dragon_crystal_necklace", DragonCrystalNecklace::new);
    }

    //龙水晶王冠
    public static RegistryObject<Item> registerDragonCrystalCrown(DeferredRegister<Item> registry) {
        return registry.register("dragon_crystal_crown", DragonCrystalCrown::new);
    }

    //覆世之翼
    public static RegistryObject<Item> registerWingsThatCoverTheWorld(DeferredRegister<Item> registry) {
        return registry.register("wings_that_cover_the_world", WingsThatCoverTheWorld::new);
    }

    private CuriosItemsRegistry() {}
}
