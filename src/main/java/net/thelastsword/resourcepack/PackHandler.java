package net.thelastsword.resourcepack;

import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.Pack.Position;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.forgespi.locating.IModFile;
import net.thelastsword.TheLastSwordMod;

import java.nio.file.Path;
import java.nio.file.Paths;

@EventBusSubscriber(modid = TheLastSwordMod.MODID, value = Dist.CLIENT, bus = Bus.MOD)
public class PackHandler {
    @SubscribeEvent
    public static void onAddPackFindersEvent(AddPackFindersEvent event) {
        if (event.getPackType().equals(PackType.CLIENT_RESOURCES)) {
            IModFile modFile = ModList.get().getModFileById(TheLastSwordMod.MODID).getFile();
            Path resourcePath = Paths.get(modFile.getFilePath().toString(), "resourcepacks", "The Last Sword Classical Texture Pack");
            PathPackResources pack = new PathPackResources(modFile.getFileName() + ":" + resourcePath, resourcePath, false);
            Pack.ResourcesSupplier sup = (v) -> pack;
            String name = "builtin/the_last_sword";

            event.addRepositorySource((c) -> {
                c.accept(Pack.create(name, Component.translatable("resourcepack.the_last_sword.desc"), false, sup, new Pack.Info(Component.translatable("resourcepack.the_last_sword.desc"), 15, FeatureFlags.VANILLA_SET), PackType.CLIENT_RESOURCES, Position.TOP, false, PackSource.BUILT_IN));
            });
        }
    }
}
