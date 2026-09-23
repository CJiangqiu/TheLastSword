package net.the_last_sword.init;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.the_last_sword.TheLastSwordMod;

//Mod 标签定义
public class ModTags {

    //龙水晶附魔台燃料：可放入红色燃料槽并被消耗发电，整合包可通过 datapack 扩展
    public static final TagKey<Item> DRAGON_CRYSTAL_ENCHANTING_TABLE_FUEL =
            ItemTags.create(ResourceLocation.fromNamespaceAndPath(TheLastSwordMod.MOD_ID, "dragon_crystal_enchanting_table_fuel"));

    //拜龙教装束：四个盔甲槽全部穿着标签内装备即可骗过拜龙教，新增装备只需加入标签
    public static final TagKey<Item> DRAGON_CULT_DISGUISE =
            ItemTags.create(ResourceLocation.fromNamespaceAndPath(TheLastSwordMod.MOD_ID, "dragon_cult_disguise"));

    private ModTags() {}
}
