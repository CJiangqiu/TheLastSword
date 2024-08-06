package net.thelastsword.init;

import net.thelastsword.TheLastSwordMod;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.Registries;

public class TheLastSwordModTabs {
	public static final DeferredRegister<CreativeModeTab> REGISTRY = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TheLastSwordMod.MODID);
	public static final RegistryObject<CreativeModeTab> THE_LAST_SWORD_TAB = REGISTRY.register("the_last_sword_tab",
			() -> CreativeModeTab.builder().title(Component.translatable("item_group.the_last_sword.the_last_sword_tab")).icon(() -> new ItemStack(TheLastSwordModItems.THE_LAST_SWORD.get())).displayItems((parameters, tabData) -> {
				tabData.accept(TheLastSwordModItems.DRAGON_CRYSTAL.get());
				tabData.accept(TheLastSwordModItems.DRAGON_CRYSTAL_UPGRADE_TEMPLATE.get());
				tabData.accept(TheLastSwordModItems.DRAGON_CRYSTAL_SWORD.get());
				tabData.accept(TheLastSwordModItems.DRAGON_CRYSTAL_ARMOR_HELMET.get());
				tabData.accept(TheLastSwordModItems.DRAGON_CRYSTAL_ARMOR_CHESTPLATE.get());
				tabData.accept(TheLastSwordModItems.DRAGON_CRYSTAL_ARMOR_LEGGINGS.get());
				tabData.accept(TheLastSwordModItems.DRAGON_CRYSTAL_ARMOR_BOOTS.get());
				tabData.accept(TheLastSwordModItems.DRAGON_CRYSTAL_SMITHING_TABLE.get());
				tabData.accept(TheLastSwordModItems.DRAGON_SWORD.get());
				tabData.accept(TheLastSwordModItems.THE_LAST_SWORD.get());


			})

					.build());
}
