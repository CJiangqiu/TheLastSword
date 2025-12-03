package net.the_last_sword.client.shader;

import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;

import java.io.IOException;

//The Last End 着色器实例
//扩展标准 ShaderInstance，提供 The Last End 特效的着色器封装
public class TheLastEndShaderInstance extends ShaderInstance {

    public TheLastEndShaderInstance(ResourceProvider resourceProvider, ResourceLocation location, VertexFormat format) throws IOException {
        super(resourceProvider, location, format);
    }

    //创建 The Last End 着色器实例
    public static TheLastEndShaderInstance create(ResourceProvider resourceProvider, ResourceLocation location, VertexFormat format) throws IOException {
        return new TheLastEndShaderInstance(resourceProvider, location, format);
    }
}
