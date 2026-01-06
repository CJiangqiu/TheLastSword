package net.the_last_sword.init;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.the_last_sword.TheLastSwordMod;
import net.the_last_sword.entity.DragonCrystalSwordProjectile;
import net.the_last_sword.entity.DragonLightingEntity;
import net.the_last_sword.entity.DragonSwordProjectile;
import net.the_last_sword.entity.GuardianArcherEntity;
import net.the_last_sword.entity.GuardianBerserkerEntity;
import net.the_last_sword.entity.GuardianOfSealedSpireEntity;
import net.the_last_sword.entity.GuardianSaberEntity;
import net.the_last_sword.entity.LostWraithEntity;
import net.the_last_sword.entity.TheLastEndLightingEntity;
import net.the_last_sword.entity.TheLastEndSwordProjectile;
import net.the_last_sword.entity.TheLastEndSwordWraithEntity;
import net.the_last_sword.test.TestEntity;

@Mod.EventBusSubscriber(modid = TheLastSwordMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)

public class ModEntities {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
        DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, TheLastSwordMod.MOD_ID);

    //龙水晶剑弹射物
    public static final RegistryObject<EntityType<DragonCrystalSwordProjectile>> DRAGON_CRYSTAL_SWORD_PROJECTILE =
        ENTITY_TYPES.register("dragon_crystal_sword_projectile",
            () -> EntityType.Builder.<DragonCrystalSwordProjectile>of(DragonCrystalSwordProjectile::new, MobCategory.MISC)
                .sized(0.5f, 0.5f)
                .clientTrackingRange(4)
                .updateInterval(10)
                .build("dragon_crystal_sword_projectile")
        );

    //龙之剑弹射物
    public static final RegistryObject<EntityType<DragonSwordProjectile>> DRAGON_SWORD_PROJECTILE =
        ENTITY_TYPES.register("dragon_sword_projectile",
            () -> EntityType.Builder.<DragonSwordProjectile>of(DragonSwordProjectile::new, MobCategory.MISC)
                .sized(0.5f, 0.5f)
                .clientTrackingRange(4)
                .updateInterval(10)
                .build("dragon_sword_projectile")
        );

    //龙之剑闪电特效
    public static final RegistryObject<EntityType<DragonLightingEntity>> DRAGON_LIGHTING =
        ENTITY_TYPES.register("dragon_lighting",
            () -> EntityType.Builder.<DragonLightingEntity>of(DragonLightingEntity::new, MobCategory.MISC)
                .sized(0.25f, 0.25f)
                .clientTrackingRange(64)
                .updateInterval(20)
                .fireImmune()
                .build("dragon_lighting")
        );

    //最终之剑闪电特效
    public static final RegistryObject<EntityType<TheLastEndLightingEntity>> THE_LAST_END_LIGHTING =
        ENTITY_TYPES.register("the_last_end_lighting",
            () -> EntityType.Builder.<TheLastEndLightingEntity>of(TheLastEndLightingEntity::new, MobCategory.MISC)
                .sized(0.25f, 0.25f)
                .clientTrackingRange(64)
                .updateInterval(20)
                .fireImmune()
                .build("the_last_end_lighting")
        );

    //最终之剑弹射物
    public static final RegistryObject<EntityType<TheLastEndSwordProjectile>> THE_LAST_END_SWORD_PROJECTILE =
        ENTITY_TYPES.register("the_last_end_sword_projectile",
            () -> EntityType.Builder.<TheLastEndSwordProjectile>of(TheLastEndSwordProjectile::new, MobCategory.MISC)
                .sized(0.5f, 0.5f)
                .clientTrackingRange(4)
                .updateInterval(10)
                .build("the_last_end_sword_projectile")
        );

    //测试实体
    public static final RegistryObject<EntityType<TestEntity>> TEST_ENTITY =
        ENTITY_TYPES.register("test_entity",
            () -> EntityType.Builder.of(TestEntity::new, MobCategory.MONSTER)
                .sized(0.6f, 1.8f)
                .clientTrackingRange(64)
                .updateInterval(3)
                .fireImmune()
                .build("test_entity")
        );

    //封印尖塔守卫
    public static final RegistryObject<EntityType<GuardianOfSealedSpireEntity>> GUARDIAN_OF_SEALED_SPIRE =
        ENTITY_TYPES.register("guardian_of_sealed_spire",
            () -> EntityType.Builder.of(GuardianOfSealedSpireEntity::new, MobCategory.MONSTER)
                .sized(0.6f, 1.95f)
                .clientTrackingRange(64)
                .updateInterval(3)
                .fireImmune()
                .build("guardian_of_sealed_spire")
        );

    //守卫剑士
    public static final RegistryObject<EntityType<GuardianSaberEntity>> GUARDIAN_SABER =
        ENTITY_TYPES.register("guardian_saber",
            () -> EntityType.Builder.of(GuardianSaberEntity::new, MobCategory.MONSTER)
                .sized(0.6f, 1.95f)
                .clientTrackingRange(64)
                .updateInterval(3)
                .fireImmune()
                .build("guardian_saber")
        );

    //守卫狂战士
    public static final RegistryObject<EntityType<GuardianBerserkerEntity>> GUARDIAN_BERSERKER =
        ENTITY_TYPES.register("guardian_berserker",
            () -> EntityType.Builder.of(GuardianBerserkerEntity::new, MobCategory.MONSTER)
                .sized(0.6f, 1.95f)
                .clientTrackingRange(64)
                .updateInterval(3)
                .fireImmune()
                .build("guardian_berserker")
        );

    //守卫弓箭手
    public static final RegistryObject<EntityType<GuardianArcherEntity>> GUARDIAN_ARCHER =
        ENTITY_TYPES.register("guardian_archer",
            () -> EntityType.Builder.of(GuardianArcherEntity::new, MobCategory.MONSTER)
                .sized(0.6f, 1.95f)
                .clientTrackingRange(64)
                .updateInterval(3)
                .fireImmune()
                .build("guardian_archer")
        );

    //迷失战魂
    public static final RegistryObject<EntityType<LostWraithEntity>> LOST_WRAITH =
        ENTITY_TYPES.register("lost_wraith",
            () -> EntityType.Builder.of(LostWraithEntity::new, MobCategory.MONSTER)
                .sized(0.6f, 1.95f)
                .clientTrackingRange(64)
                .updateInterval(3)
                .fireImmune()
                .build("lost_wraith")
        );

    //终焉剑灵
    public static final RegistryObject<EntityType<TheLastEndSwordWraithEntity>> THE_LAST_END_SWORD_WRAITH =
        ENTITY_TYPES.register("the_last_end_sword_wraith",
            () -> EntityType.Builder.of(TheLastEndSwordWraithEntity::new, MobCategory.MONSTER)
                .sized(0.6f, 1.95f)
                .clientTrackingRange(64)
                .updateInterval(3)
                .fireImmune()
                .build("the_last_end_sword_wraith")
        );

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }

    //注册实体属性
    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(TEST_ENTITY.get(), TestEntity.createAttributes().build());
        event.put(GUARDIAN_OF_SEALED_SPIRE.get(), GuardianOfSealedSpireEntity.createAttributes().build());
        event.put(GUARDIAN_SABER.get(), GuardianSaberEntity.createAttributes().build());
        event.put(GUARDIAN_BERSERKER.get(), GuardianBerserkerEntity.createAttributes().build());
        event.put(GUARDIAN_ARCHER.get(), GuardianArcherEntity.createAttributes().build());
        event.put(LOST_WRAITH.get(), LostWraithEntity.createAttributes().build());
        event.put(THE_LAST_END_SWORD_WRAITH.get(), TheLastEndSwordWraithEntity.createAttributes().build());
    }
}
