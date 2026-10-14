#version 150

uniform sampler2D Sampler0;
uniform vec2 LocalUvMin;
uniform vec2 LocalUvScale;
uniform float GlowPhase;

in vec2 texCoord;
out vec4 fragColor;

float coreAlpha(vec2 pixel) {
    // 中央区域独立取样，避免白色外壳被当作球体。
    if (pixel.x < 10.0 || pixel.x >= 22.0 || pixel.y < 10.0 || pixel.y >= 22.0) return 0.0;
    vec2 uv = (floor(pixel) + 0.5) / 32.0;
    return step(0.5, texture(Sampler0, LocalUvMin + uv / LocalUvScale).a);
}

float expandingHalo(float radius, float phase) {
    float waveRadius = mix(5.0, 10.5, phase);
    float offset = (radius - waveRadius) / 1.1;
    // 两端平滑淡入淡出，使波纹重启时不会突然跳回球体边缘。
    float fade = smoothstep(0.0, 0.15, phase) * (1.0 - smoothstep(0.55, 1.0, phase));
    return exp(-offset * offset) * fade;
}

void main() {
    vec2 uv = (texCoord - LocalUvMin) * LocalUvScale;
    vec2 pixel = uv * 32.0;
    float distanceFromCore = length(pixel - vec2(16.0));
    if (distanceFromCore > 10.5) discard;

    float inside = coreAlpha(pixel);
    float neighbors = min(min(coreAlpha(pixel + vec2(1.0, 0.0)), coreAlpha(pixel - vec2(1.0, 0.0))),
                          min(coreAlpha(pixel + vec2(0.0, 1.0)), coreAlpha(pixel - vec2(0.0, 1.0))));
    float rim = inside * (1.0 - neighbors);
    float baseAlpha = texture(Sampler0, texCoord).a;
    // 外扩光晕避开外壳，球面薄雾保留底图的明暗层次。
    float waves = expandingHalo(distanceFromCore, GlowPhase)
                + expandingHalo(distanceFromCore, fract(GlowPhase + 0.5));
    float halo = (1.0 - step(0.1, baseAlpha))
               * (0.10 * exp(-max(distanceFromCore - 5.5, 0.0) * 1.15) + waves * 0.55)
               * (1.0 - smoothstep(9.5, 10.5, distanceFromCore));
    float breath = 0.95 + 0.05 * cos(GlowPhase * 6.2831853);
    float surfaceGlow = inside * mix(0.25, 0.40, smoothstep(0.0, 6.0, distanceFromCore)) * breath;
    float alpha = max(surfaceGlow, max(rim * 0.65 * breath, halo));
    if (alpha < 0.005) discard;
    vec3 purple = mix(vec3(0.48, 0.10, 1.0), vec3(0.78, 0.40, 1.0), rim);
    fragColor = vec4(purple, alpha);
}
