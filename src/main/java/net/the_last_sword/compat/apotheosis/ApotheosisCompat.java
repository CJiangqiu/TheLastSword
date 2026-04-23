package net.the_last_sword.compat.apotheosis;

import net.minecraft.world.item.Item;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.the_last_sword.compat.CompatCheck;
import net.the_last_sword.init.ModItems;
import net.the_last_sword.util.TheLastSwordLogger;

import java.util.AbstractMap;

//神化mod（Apotheosis）兼容：通过IMC接口将本mod的剑类物品声明为sword类别
//解决因 canPerformAction(PICKAXE_DIG) 返回 true 导致被误判为 pickaxe、无法镶嵌宝石的问题
public class ApotheosisCompat {

    private static final String APOTHEOSIS_MODID = "apotheosis";
    private static final String IMC_METHOD = "loot_category_override";
    private static final String SWORD_CATEGORY = "sword";

    @SubscribeEvent
    public void onEnqueueIMC(InterModEnqueueEvent event) {
        if (!CompatCheck.isApotheosisLoaded()) return;

        sendOverride(ModItems.DRAGON_CRYSTAL_SWORD.get());
        sendOverride(ModItems.DRAGON_SWORD.get());
        sendOverride(ModItems.THE_LAST_SWORD.get());

        TheLastSwordLogger.info("Registered Apotheosis loot_category_override for TLS swords -> sword");
    }

    private static void sendOverride(Item item) {
        InterModComms.sendTo(APOTHEOSIS_MODID, IMC_METHOD,
                () -> new AbstractMap.SimpleEntry<Item, String>(item, SWORD_CATEGORY));
    }
}
