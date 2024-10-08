package net.thelastsword.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

public class DragonCrystal extends Item {

	public DragonCrystal() {
		super(new Item.Properties().stacksTo(64).fireResistant().rarity(Rarity.UNCOMMON));
	}


}
