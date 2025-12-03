#version 150

/* The Last End - 星空剑刃着色器 */
/* 动态流星效果版本 */

uniform vec4 ColorModulator;
uniform sampler2D Sampler0;  // 主纹理（包含完整剑身）
uniform float GameTime;

in vec4 vertexColor;
in vec2 texCoord0;
out vec4 fragColor;


// 检测是否为黑色区域
bool isBlackArea(vec3 color) {
    float brightness = (color.r + color.g + color.b) / 3.0;
    return brightness < 0.2;
}

// 生成随机数
float random(vec2 st) {
    return fract(sin(dot(st.xy, vec2(12.9898, 78.233))) * 43758.5453123);
}

void main() {
    vec4 baseTexture = texture(Sampler0, texCoord0);
    vec3 baseColor = baseTexture.rgb * vertexColor.rgb;

    vec3 effects = vec3(0.0);

    // 只在黑色区域应用星空效果
    if (isBlackArea(baseTexture.rgb)) {
        vec2 screenPos = gl_FragCoord.xy * 0.01;
        vec3 backgroundColor = vec3(0.0);

        // 【星星层】白色星星
        float starSize = 0.2;
        vec2 starGrid = floor(screenPos / starSize);
        vec2 starLocal = fract(screenPos / starSize);
        float starSeed = dot(starGrid, vec2(127.1, 311.7));
        float starRandom = random(starGrid);

        if (starRandom > 0.88) {
            vec2 starCenter = vec2(
                fract(sin(starSeed) * 12.9898),
                fract(cos(starSeed) * 78.233)
            );
            float distToStar = distance(starLocal, starCenter);

            // 随机大小（±20%）
            float sizeRandom = fract(sin(starSeed * 7.123) * 43758.5453);
            float sizeMultiplier = 0.8 + sizeRandom * 0.4;

            float starIntensity = smoothstep(0.25 * sizeMultiplier, 0.1 * sizeMultiplier, distToStar);

            // 70%静态，30%闪烁
            float twinkleProbability = fract(sin(starSeed * 3.789) * 12345.6789);
            float brightness;

            if (twinkleProbability > 0.7) {
                brightness = 0.7 + 0.3 * sin(GameTime * 2000.0 + starSeed);
            } else {
                brightness = 1.0;
            }

            backgroundColor += vec3(1.0) * starIntensity * brightness;
        }

        // 【流星层】紫色流星 - 单向飞行带间隔
        float meteorTime = GameTime * 2000.0;
        float meteorGridSize = 1.5; // 缩小网格，增加覆盖密度
        vec2 meteorGrid = floor(screenPos / meteorGridSize);
        float meteorSeed = dot(meteorGrid, vec2(271.3, 183.4));
        float meteorSpawnRandom = random(meteorGrid);

        // 30%概率显示流星（覆盖更多区域）
        if (meteorSpawnRandom > 0.7) {
            // 时间周期控制，每个网格有大幅不同的相位
            float meteorCycle = fract(meteorTime * 0.3 + meteorSeed * 5.0);

            // 流星只在前40%时间可见，后60%等待
            if (meteorCycle < 0.4) {
                // 归一化移动进度
                float meteorProgress = meteorCycle / 0.4;

                // 流星位置：从右上单向移动到左下
                vec2 meteorStart = vec2(1.2, 1.2);
                vec2 meteorEnd = vec2(-0.2, -0.2);
                vec2 meteorPos = mix(meteorStart, meteorEnd, meteorProgress);

                // 当前像素在网格中的局部坐标
                vec2 localPos = fract(screenPos / meteorGridSize);

                // 流星方向（从右上到左下的45度角）
                vec2 meteorDir = normalize(vec2(-1.0, -1.0));

                // 计算到流星的向量
                vec2 toMeteor = localPos - meteorPos;

                // 投影到流星方向
                float alongTrail = dot(toMeteor, meteorDir);

                // 垂直距离
                vec2 perpVec = vec2(meteorDir.y, -meteorDir.x);
                float perpDist = abs(dot(toMeteor, perpVec));

                // 拖尾参数
                float trailLength = 0.4;
                float trailWidth = 0.05;

                // 检查是否在拖尾范围内
                if (alongTrail > -trailLength && alongTrail < 0.0 && perpDist < trailWidth) {
                    float trailFade = (alongTrail + trailLength) / trailLength;
                    float widthFade = 1.0 - (perpDist / trailWidth);

                    float meteorIntensity = trailFade * widthFade;

                    // 紫色流星
                    backgroundColor += vec3(0.8, 0.5, 1.0) * meteorIntensity;
                }
            }
        }

        effects = backgroundColor;
    }

    vec3 finalColor = baseColor + effects;
    fragColor = vec4(finalColor, baseTexture.a * vertexColor.a) * ColorModulator;
}