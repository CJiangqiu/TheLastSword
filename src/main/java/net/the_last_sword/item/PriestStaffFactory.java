package net.the_last_sword.item;

import net.minecraft.world.item.Item;
import net.the_last_sword.compat.CompatCheck;
import net.the_last_sword.compat.irons_spellbooks.CastingPriestStaffItem;

public final class PriestStaffFactory {
    private PriestStaffFactory() {
    }

    public static Item create() {
        return CompatCheck.isSpellcastingLoaded() ? Casting.create() : new PriestStaffItem();
    }

    // 隔离可选父类的加载，未安装施法系统时只创建普通物品。
    private static final class Casting {
        private static Item create() {
            return new CastingPriestStaffItem();
        }
    }
}
