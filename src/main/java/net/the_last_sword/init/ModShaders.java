package net.the_last_sword.init;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.the_last_sword.TheLastSwordMod;
import net.the_last_sword.client.shader.TheLastEndEffect;
import net.the_last_sword.client.shader.TheLastEndShaderInstance;

import java.io.IOException;

//着色器注册
@Mod.EventBusSubscriber(modid = TheLastSwordMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModShaders {

    //注册着色器
    @SubscribeEvent
    public static void onRegisterShaders(RegisterShadersEvent event) throws IOException {
        //注册 The Last End 着色器
        ShaderInstance shader = TheLastEndShaderInstance.create(
            event.getResourceProvider(),
            new ResourceLocation(TheLastSwordMod.MOD_ID, "the_last_end"),
            DefaultVertexFormat.BLOCK
        );
        event.registerShader(shader, TheLastEndEffect::setShader);
    }
}
