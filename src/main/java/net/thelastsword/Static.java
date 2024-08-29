package net.thelastsword;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.ModList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Static {
    public static final String MOD_ID = "the_last_sword";
    public static final Logger LOGGER = LogManager.getLogger();

    public static ResourceLocation rl(String path) {
        return new ResourceLocation(MOD_ID, path);
    }

    public static boolean isLoad(String name) {
        return ModList.get().isLoaded(name);
    }
}
