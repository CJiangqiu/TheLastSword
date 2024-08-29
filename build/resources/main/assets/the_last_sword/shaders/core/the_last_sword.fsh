#version 150

uniform sampler2D hiltTexture;
uniform sampler2D bodyTexture;
uniform float time;
uniform float yaw;
uniform float pitch;
uniform float externalScale;
uniform float opacity;
uniform float scale;
uniform float offsetX;
uniform float offsetY;

in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec2 uv = texCoord;
    vec4 color64 = texture(hiltTexture, uv);

    // 计算256x256贴图的UV坐标
    vec2 uv256 = (uv - vec2(offsetX, offsetY)) * scale;
    vec4 color256 = texture(bodyTexture, uv256);

    // 根据条件混合两个贴图
    if (uv256.x >= 0.0 && uv256.x <= 1.0 && uv256.y >= 0.0 && uv256.y <= 1.0) {
        fragColor = color256;
    } else {
        fragColor = color64;
    }
}
