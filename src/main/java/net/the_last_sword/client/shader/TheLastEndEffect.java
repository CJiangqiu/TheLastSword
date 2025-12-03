package net.the_last_sword.client.shader;

import com.mojang.blaze3d.shaders.Uniform;
import net.minecraft.client.renderer.ShaderInstance;

//The Last End 着色效果管理器
//统一管理着色器实例和 Uniform 变量，提供给多个渲染场景使用
public class TheLastEndEffect {

    private static ShaderInstance shaderInstance;
    private static Uniform timeUniform;

    //设置着色器实例（在 RegisterShadersEvent 中调用）
    public static void setShader(ShaderInstance shader) {
        shaderInstance = shader;
        if (shader != null) {
            timeUniform = shader.getUniform("GameTime");
        }
    }

    //获取着色器实例（供 RenderType 使用）
    public static ShaderInstance getShader() {
        return shaderInstance;
    }

    //应用 uniforms（在渲染前调用）
    //使用系统时间而不是游戏时间（按用户要求）
    public static void applyUniforms() {
        if (timeUniform != null) {
            try {
                //使用系统时间（毫秒转秒）
                float systemTime = (System.currentTimeMillis() % 1000000L) / 1000.0F;
                timeUniform.set(systemTime);
            } catch (Exception ignored) {
                //静默失败，不影响渲染
            }
        }
    }

    //检查着色器是否可用
    public static boolean isAvailable() {
        return shaderInstance != null;
    }

    //清除着色器引用（资源重载时调用）
    public static void clear() {
        shaderInstance = null;
        timeUniform = null;
    }

    private TheLastEndEffect() {}
}
