
package net.thelastsword.item;

import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item;

public class DragonCrystalUpgradeTemplate extends Item {
	public DragonCrystalUpgradeTemplate() {
		super(new Item.Properties().stacksTo(16).fireResistant().rarity(Rarity.UNCOMMON));
	}

	@Override
	public int getEnchantmentValue() {
		return 22;
	}
}