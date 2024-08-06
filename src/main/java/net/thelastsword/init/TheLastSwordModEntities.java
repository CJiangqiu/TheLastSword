package net.thelastsword.init;

import net.thelastsword.entity.TheLastSwordProjectile;
import net.thelastsword.entity.DragonSwordProjectile;
import net.thelastsword.entity.DragonCrystalSwordProjectile;
import net.thelastsword.TheLastSwordMod;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.fml.common.Mod;

import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Entity;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class TheLastSwordModEntities {
	public static final DeferredRegister<EntityType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, TheLastSwordMod.MODID);
	public static final RegistryObject<EntityType<DragonSwordProjectile>> DRAGON_SWORD_PROJECTILE = register("dragon_sword_projectile", EntityType.Builder.<DragonSwordProjectile>of(DragonSwordProjectile::new, MobCategory.MISC)
			.setCustomClientFactory(DragonSwordProjectile::new).setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(1).sized(0.5f, 0.5f));
	public static final RegistryObject<EntityType<TheLastSwordProjectile>> THE_LAST_SWORD_POJECTILE = register("the_last_sword_pojectile", EntityType.Builder.<TheLastSwordProjectile>of(TheLastSwordProjectile::new, MobCategory.MISC)
			.setCustomClientFactory(TheLastSwordProjectile::new).setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(1).sized(0.5f, 0.5f));
	public static final RegistryObject<EntityType<DragonCrystalSwordProjectile>> DRAGON_CRYSTAL_SWORD_PROJECTILE = register("dragon_crystal_sword_projectile",
			EntityType.Builder.<DragonCrystalSwordProjectile>of(DragonCrystalSwordProjectile::new, MobCategory.MISC).setCustomClientFactory(DragonCrystalSwordProjectile::new).setShouldReceiveVelocityUpdates(true)
					.setTrackingRange(64).setUpdateInterval(1).sized(0.5f, 0.5f));


	private static <T extends Entity> RegistryObject<EntityType<T>> register(String registryname, EntityType.Builder<T> entityTypeBuilder) {
		return REGISTRY.register(registryname, () -> (EntityType<T>) entityTypeBuilder.build(registryname));
	}
}
