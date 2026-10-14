package net.the_last_sword.compat.projecte;

import net.minecraft.world.item.Item;
import net.the_last_sword.compat.CompatCheck;
import net.the_last_sword.compat.irons_spellbooks.CastingTransmutationStaffItem;

public final class TransmutationStaffFactory {
    private TransmutationStaffFactory() {
    }

    public static Item create() {
        return CompatCheck.isSpellcastingLoaded() ? Casting.create() : Plain.create();
    }

    // 返回类型统一为基础物品，避免公共入口校验字节码时解析可选实现的父类。
    private static final class Casting {
        private static Item create() {
            return new CastingTransmutationStaffItem();
        }
    }

    private static final class Plain {
        private static Item create() {
            return new StaffOfUniversalTransmutationItem();
        }
    }
}
