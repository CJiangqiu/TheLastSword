package net.thelastsword.init;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.thelastsword.TheLastSwordMod;
import net.thelastsword.entity.DragonCrystalSwordProjectile;
import net.thelastsword.entity.DragonSwordProjectile;
import net.thelastsword.entity.TestEntity;
import net.thelastsword.entity.TheLastSwordProjectile;

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
	public static final RegistryObject<EntityType<TestEntity>> TEST_ENTITY = register("test_entity", EntityType.Builder.<TestEntity>of(TestEntity::new, MobCategory.MONSTER).setShouldReceiveVelocityUpdates(true).setTrackingRange(64)
			.setUpdateInterval(3).setCustomClientFactory(TestEntity::new).fireImmune().sized(0.6f, 1.8f));



	private static <T extends Entity> RegistryObject<EntityType<T>> register(String registryname, EntityType.Builder<T> entityTypeBuilder) {
		return REGISTRY.register(registryname, () -> (EntityType<T>) entityTypeBuilder.build(registryname));
	}
	@SubscribeEvent
	public static void init(FMLCommonSetupEvent event) {
		event.enqueueWork(() -> {
			TestEntity.init();
		});
	}

	@SubscribeEvent
	public static void registerAttributes(EntityAttributeCreationEvent event) {
		event.put(TEST_ENTITY.get(), TestEntity.createAttributes().build());
	}

}
