package net.thelastsword.init;

import net.minecraft.world.item.Rarity;
import net.thelastsword.item.*;
import net.thelastsword.TheLastSwordMod;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.BlockItem;

public class TheLastSwordModItems {
	public static final DeferredRegister<Item> REGISTRY = DeferredRegister.create(ForgeRegistries.ITEMS, TheLastSwordMod.MODID);
	public static final RegistryObject<Item> DRAGON_CRYSTAL = REGISTRY.register("dragon_crystal", () -> new DragonCrystal());
	public static final RegistryObject<Item> DRAGON_CRYSTAL_SWORD = REGISTRY.register("dragon_crystal_sword", () -> new DragonCrystalSword());

	public static final RegistryObject<Item> DRAGON_SWORD = REGISTRY.register("dragon_sword", () -> new DragonSword());
	public static final RegistryObject<Item> THE_LAST_SWORD = REGISTRY.register("the_last_sword", () -> new TheLastSword());
	public static final RegistryObject<Item> DRAGON_CRYSTAL_UPGRADE_TEMPLATE = REGISTRY.register("dragon_crystal_upgrade_template", () -> new DragonCrystalUpgradeTemplate());
	public static final RegistryObject<Item> DRAGON_CRYSTAL_ARMOR_HELMET = REGISTRY.register("dragon_crystal_armor_helmet", () -> new DragonCrystalArmor.Helmet());
	public static final RegistryObject<Item> DRAGON_CRYSTAL_ARMOR_CHESTPLATE = REGISTRY.register("dragon_crystal_armor_chestplate", () -> new DragonCrystalArmor.Chestplate());
	public static final RegistryObject<Item> DRAGON_CRYSTAL_ARMOR_LEGGINGS = REGISTRY.register("dragon_crystal_armor_leggings", () -> new DragonCrystalArmor.Leggings());
	public static final RegistryObject<Item> DRAGON_CRYSTAL_ARMOR_BOOTS = REGISTRY.register("dragon_crystal_armor_boots", () -> new DragonCrystalArmor.Boots());
	public static final RegistryObject<Item> DRAGON_CRYSTAL_SMITHING_TABLE = REGISTRY.register("dragon_crystal_smithing_table", () -> new BlockItem(TheLastSwordModBlocks.DRAGON_CRYSTAL_SMITHING_TABLE.get(), new Item.Properties().rarity(Rarity.RARE)));


	private static RegistryObject<Item> block(RegistryObject<Block> block) {
		return REGISTRY.register(block.getId().getPath(), () -> new BlockItem(block.get(), new Item.Properties()));
	}
}
