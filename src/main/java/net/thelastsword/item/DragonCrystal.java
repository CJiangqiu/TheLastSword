package net.thelastsword.item;

import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item;

public class DragonCrystal extends Item {

	public DragonCrystal() {
		super(new Item.Properties().stacksTo(64).fireResistant().rarity(Rarity.UNCOMMON));
	}


}
