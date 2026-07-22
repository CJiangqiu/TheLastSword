package net.the_last_sword.event;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.the_last_sword.init.ModBlocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.the_last_sword.TheLastSwordMod;
import net.the_last_sword.client.gui.DefenceConfigScreen;
import net.the_last_sword.client.recipe.ClientDragonCrystalRecipeCache;
import net.the_last_sword.client.overlay.DragonArmorOverlay;
import net.the_last_sword.client.overlay.JustifiedDefenceOverlay;
import net.the_last_sword.client.renderer.DragonShieldRenderer;
import net.the_last_sword.client.shader.TheLastEndEffect;
import net.the_last_sword.configuration.DefenceConfig;
import net.the_last_sword.network.DefenceConfigPacket;
import net.the_last_sword.init.ModKeyMappings;
import net.the_last_sword.item.TheLastSword;
import net.the_last_sword.test.UltraTestSwordItem;
import net.the_last_sword.network.CancelPreviewPacket;
import net.the_last_sword.network.ChangeModePacket;
import net.the_last_sword.network.NetworkHandler;
import net.the_last_sword.network.OpenSummonGuiPacket;
import net.the_last_sword.util.nbt.ItemModeHelper;
import net.the_last_sword.util.TheLastSwordLogger;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

//客户端事件处理器
@Mod.EventBusSubscriber(modid = TheLastSwordMod.MOD_ID, value = Dist.CLIENT)
public class ClientEventHandler {

    //万物终焉渲染相关
    private static final ResourceLocation END_PORTAL_TEXTURE = new ResourceLocation("textures/entity/end_portal.png");
    private static final double EFFECT_RADIUS = 32.0;
    private static float rotation = 0.0f;

    //龙魂灯笼渲染相关
    private static final double LANTERN_RANGE = 8.0;
    private static final double LANTERN_SEARCH_RANGE = 16.0;

    //球体渲染缓冲区（静态重用，避免内存泄漏）
    private static BufferBuilder sphereBufferBuilder = null;
    private static MultiBufferSource.BufferSource sphereBufferSource = null;

    //球体渲染类型（使用终焉着色器）
    private static final RenderType SPHERE_RENDER_TYPE = RenderType.create(
            "all_things_end_sphere",
            DefaultVertexFormat.POSITION_TEX,
            VertexFormat.Mode.QUADS,
            2097152,
            false,
            true,
            RenderType.CompositeState.builder()
                    .setShaderState(new RenderStateShard.ShaderStateShard(TheLastEndEffect::getShader))
                    .setTextureState(new RenderStateShard.TextureStateShard(END_PORTAL_TEXTURE, false, false))
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setCullState(RenderStateShard.NO_CULL)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .createCompositeState(false)
    );

    //========== 竞技场预览相关 ==========
    private static BlockPos arenaPreviewMin = null;
    private static BlockPos arenaPreviewMax = null;
    private static long arenaPreviewUpdateTime = 0;

    //设置竞技场预览（由网络包调用）
    public static void setArenaPreview(BlockPos minPos, BlockPos maxPos) {
        arenaPreviewMin = minPos;
        arenaPreviewMax = maxPos;
        arenaPreviewUpdateTime = System.currentTimeMillis();
    }

    //清除竞技场预览
    public static void clearArenaPreview() {
        arenaPreviewMin = null;
        arenaPreviewMax = null;
    }

    //返回本地玩家最大生命值，无玩家返回 -1（仅客户端调用，供通用代码安全获取本地玩家数据）
    public static float getLocalPlayerMaxHealth() {
        net.minecraft.world.entity.player.Player p = Minecraft.getInstance().player;
        return p != null ? p.getMaxHealth() : -1f;
    }

    //检查是否有活动的竞技场预览
    public static boolean hasActiveArenaPreview() {
        if (arenaPreviewMin == null) return false;
        if (System.currentTimeMillis() - arenaPreviewUpdateTime > 10000) {
            clearArenaPreview();
            return false;
        }
        return true;
    }

    //========== 挖掘预览相关 ==========
    private static final Set<BlockPos> miningPreviewBlocks = new HashSet<>();
    private static long miningPreviewUpdateTime = 0;

    //设置预览方块（由网络包调用）
    public static void setMiningPreviewBlocks(Set<BlockPos> blocks) {
        miningPreviewBlocks.clear();
        miningPreviewBlocks.addAll(blocks);
        miningPreviewUpdateTime = System.currentTimeMillis();
    }

    //清除预览（由网络包调用）
    public static void clearMiningPreview() {
        miningPreviewBlocks.clear();
    }

    //检查是否有活动的预览
    public static boolean hasActiveMiningPreview() {
        if (miningPreviewBlocks.isEmpty()) {
            return false;
        }
        //检查预览是否过期（10秒）
        if (System.currentTimeMillis() - miningPreviewUpdateTime > 10000) {
            clearMiningPreview();
            return false;
        }
        return true;
    }

    @Mod.EventBusSubscriber(modid = TheLastSwordMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ModBusEvents {
        //客户端初始化
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> {
                //加载防御配置
                DefenceConfig.load();
            });
        }
    }

    //玩家登录事件 - 同步配置到服务端
    @SubscribeEvent
    public static void onPlayerLogin(ClientPlayerNetworkEvent.LoggingIn event) {
        //发送客户端配置到服务端
        NetworkHandler.sendToServer(new DefenceConfigPacket(DefenceConfig.getData()));
        TheLastSwordLogger.debug("Sent defence config to server on login");
    }

    //断开服务器后清空服务端配方，避免切换服务器期间沿用上一台服务器的数据
    @SubscribeEvent
    public static void onPlayerLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientDragonCrystalRecipeCache.clear();
    }

    //按键输入事件
    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (event.getAction() == GLFW.GLFW_RELEASE) {
            if (event.getKey() == ModKeyMappings.CHANGE_SWORD_MODE.getKey().getValue()) {
                Minecraft minecraft = Minecraft.getInstance();
                if (minecraft.player != null && minecraft.getConnection() != null) {
                    NetworkHandler.sendToServer(new ChangeModePacket());
                }
            }
        }
    }

    //鼠标输入事件 - 左键取消挖掘预览
    @SubscribeEvent
    public static void onMouseInput(InputEvent.MouseButton.Pre event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null || minecraft.screen != null) {
            return; //有GUI界面打开时不处理
        }

        //检测左键按下
        if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT && event.getAction() == GLFW.GLFW_PRESS) {
            ItemStack mainHand = minecraft.player.getMainHandItem();

            //最终之剑强力挖掘模式：左键取消预览
            if (mainHand.getItem() instanceof TheLastSword) {
                int mode = ItemModeHelper.getMode(mainHand);
                if (mode == 1 && hasActiveMiningPreview()) {
                    NetworkHandler.sendToServer(new CancelPreviewPacket());
                    event.setCanceled(true);
                    return;
                }
            }

            //究极测试剑斗兽模式：左键取消预览
            if (mainHand.getItem() instanceof UltraTestSwordItem) {
                int mode = ItemModeHelper.getMode(mainHand);
                if (mode == 1 && hasActiveArenaPreview()) {
                    NetworkHandler.sendToServer(new CancelPreviewPacket());
                    clearArenaPreview();
                    event.setCanceled(true);
                }
            }
        }
    }

    //HUD 渲染事件（Pre）
    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiOverlayEvent.Pre event) {
        JustifiedDefenceOverlay.onRenderGuiOverlay(event);
    }

    //HUD 渲染事件（Post）- 龙之盔甲叠加层
    @SubscribeEvent
    public static void onRenderGuiOverlayPost(RenderGuiOverlayEvent.Post event) {
        DragonArmorOverlay.onRenderGuiOverlay(event);
    }

    //客户端Tick事件 - 处理按键
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Minecraft mc = Minecraft.getInstance();

            //检查防御配置按键
            while (ModKeyMappings.OPEN_DEFENCE_CONFIG.consumeClick()) {
                if (mc.screen == null) {
                    mc.setScreen(new DefenceConfigScreen());
                }
            }

            //检查打开唤灵GUI按键
            while (ModKeyMappings.OPEN_SUMMON_GUI.consumeClick()) {
                if (mc.screen == null && mc.player != null) {
                    NetworkHandler.sendToServer(new OpenSummonGuiPacket());
                }
            }
        }
    }

    //万物终焉球体渲染
    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return;
        }

        //渲染挖掘预览
        if (!miningPreviewBlocks.isEmpty()) {
            if (System.currentTimeMillis() - miningPreviewUpdateTime > 10000) {
                clearMiningPreview();
            } else {
                renderMiningPreview(event.getPoseStack(), event.getCamera());
            }
        }

        //渲染竞技场预览
        if (arenaPreviewMin != null) {
            if (System.currentTimeMillis() - arenaPreviewUpdateTime > 10000) {
                clearArenaPreview();
            } else {
                renderArenaPreview(event.getPoseStack(), event.getCamera());
            }
        }

        //渲染龙魂灯笼范围
        renderDragonSoulLanternRanges(event.getPoseStack(), event.getCamera(), mc.level);

        //渲染龙套护盾（复用全局缓冲避免每帧new导致OOM）
        MultiBufferSource.BufferSource buf = mc.renderBuffers.bufferSource();
        DragonShieldRenderer.render(event.getPoseStack(), buf, event.getPartialTick());
        buf.endBatch();
    }

    //渲染球体
    private static void renderSphere(PoseStack poseStack, Vec3 center, float partialTick) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(false);

        poseStack.pushPose();

        //移动到球心位置
        Vec3 cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        poseStack.translate(
                center.x - cameraPos.x,
                center.y - cameraPos.y,
                center.z - cameraPos.z
        );

        //旋转效果
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation * 0.5f));
        poseStack.mulPose(Axis.XP.rotationDegrees(rotation * 0.3f));

        //渲染球体网格
        renderSphereMesh(poseStack, (float) EFFECT_RADIUS);

        poseStack.popPose();

        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
    }

    //渲染球体网格
    private static void renderSphereMesh(PoseStack poseStack, float radius) {
        //初始化缓冲区（仅首次）
        if (sphereBufferBuilder == null) {
            sphereBufferBuilder = new BufferBuilder(2097152); // 2MB预分配，避免扩容
            sphereBufferSource = MultiBufferSource.immediate(sphereBufferBuilder);
        }

        VertexConsumer buffer = sphereBufferSource.getBuffer(SPHERE_RENDER_TYPE);
        Matrix4f matrix = poseStack.last().pose();

        int segments = 16;
        float segmentAngle = 360.0f / segments;

        //渲染球体（经纬度网格）
        for (int lat = 0; lat < segments; lat++) {
            float lat1 = lat * segmentAngle - 90;
            float lat2 = (lat + 1) * segmentAngle - 90;

            for (int lon = 0; lon < segments; lon++) {
                float lon1 = lon * segmentAngle;
                float lon2 = (lon + 1) * segmentAngle;

                //计算四个顶点
                Vec3 v1 = spherePoint(radius, lat1, lon1);
                Vec3 v2 = spherePoint(radius, lat1, lon2);
                Vec3 v3 = spherePoint(radius, lat2, lon2);
                Vec3 v4 = spherePoint(radius, lat2, lon1);

                //UV坐标
                float u1 = lon / (float) segments;
                float u2 = (lon + 1) / (float) segments;
                float v1f = lat / (float) segments;
                float v2f = (lat + 1) / (float) segments;

                //添加四边形（顺时针顺序）
                buffer.vertex(matrix, (float) v1.x, (float) v1.y, (float) v1.z).uv(u1, v1f).endVertex();
                buffer.vertex(matrix, (float) v2.x, (float) v2.y, (float) v2.z).uv(u2, v1f).endVertex();
                buffer.vertex(matrix, (float) v3.x, (float) v3.y, (float) v3.z).uv(u2, v2f).endVertex();
                buffer.vertex(matrix, (float) v4.x, (float) v4.y, (float) v4.z).uv(u1, v2f).endVertex();
            }
        }

        sphereBufferSource.endBatch();
    }

    //计算球面上的点
    private static Vec3 spherePoint(float radius, float latitude, float longitude) {
        float latRad = (float) Math.toRadians(latitude);
        float lonRad = (float) Math.toRadians(longitude);

        float x = radius * Mth.cos(latRad) * Mth.cos(lonRad);
        float y = radius * Mth.sin(latRad);
        float z = radius * Mth.cos(latRad) * Mth.sin(lonRad);

        return new Vec3(x, y, z);
    }

    //========== 挖掘预览渲染方法 ==========

    //渲染挖掘预览方块
    private static void renderMiningPreview(PoseStack poseStack, Camera camera) {
        Vec3 cameraPos = camera.getPosition();
        poseStack.pushPose();
        poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        MultiBufferSource.BufferSource buf = Minecraft.getInstance().renderBuffers.bufferSource();
        VertexConsumer buffer = buf.getBuffer(RenderType.lines());

        float time = (System.currentTimeMillis() % 2000) / 2000.0f;
        float alpha = 0.5f + 0.3f * (float) Math.sin(time * Math.PI * 2);
        float red = 0.0f;
        float green = 1.0f;
        float blue = 0.0f;

        Matrix4f matrix = poseStack.last().pose();

        for (BlockPos pos : miningPreviewBlocks) {
            renderBlockOutline(buffer, matrix, pos, red, green, blue, alpha);
        }

        buf.endBatch();
        poseStack.popPose();
    }

    //渲染单个方块轮廓
    private static void renderBlockOutline(VertexConsumer buffer, Matrix4f matrix, BlockPos pos,
                                           float red, float green, float blue, float alpha) {
        float x1 = pos.getX();
        float y1 = pos.getY();
        float z1 = pos.getZ();
        float x2 = x1 + 1.0f;
        float y2 = y1 + 1.0f;
        float z2 = z1 + 1.0f;

        //底面的4条边
        addLine(buffer, matrix, x1, y1, z1, x2, y1, z1, red, green, blue, alpha);
        addLine(buffer, matrix, x2, y1, z1, x2, y1, z2, red, green, blue, alpha);
        addLine(buffer, matrix, x2, y1, z2, x1, y1, z2, red, green, blue, alpha);
        addLine(buffer, matrix, x1, y1, z2, x1, y1, z1, red, green, blue, alpha);

        //顶面的4条边
        addLine(buffer, matrix, x1, y2, z1, x2, y2, z1, red, green, blue, alpha);
        addLine(buffer, matrix, x2, y2, z1, x2, y2, z2, red, green, blue, alpha);
        addLine(buffer, matrix, x2, y2, z2, x1, y2, z2, red, green, blue, alpha);
        addLine(buffer, matrix, x1, y2, z2, x1, y2, z1, red, green, blue, alpha);

        //4条竖直边
        addLine(buffer, matrix, x1, y1, z1, x1, y2, z1, red, green, blue, alpha);
        addLine(buffer, matrix, x2, y1, z1, x2, y2, z1, red, green, blue, alpha);
        addLine(buffer, matrix, x2, y1, z2, x2, y2, z2, red, green, blue, alpha);
        addLine(buffer, matrix, x1, y1, z2, x1, y2, z2, red, green, blue, alpha);
    }

    //添加一条线段
    private static void addLine(VertexConsumer buffer, Matrix4f matrix,
                                float x1, float y1, float z1, float x2, float y2, float z2,
                                float red, float green, float blue, float alpha) {
        buffer.vertex(matrix, x1, y1, z1).color(red, green, blue, alpha).normal(1, 0, 0).endVertex();
        buffer.vertex(matrix, x2, y2, z2).color(red, green, blue, alpha).normal(1, 0, 0).endVertex();
    }

    //========== 竞技场预览渲染方法 ==========

    //渲染竞技场预览方框
    private static void renderArenaPreview(PoseStack poseStack, Camera camera) {
        Vec3 cameraPos = camera.getPosition();
        poseStack.pushPose();
        poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        MultiBufferSource.BufferSource buf = Minecraft.getInstance().renderBuffers.bufferSource();
        VertexConsumer buffer = buf.getBuffer(RenderType.lines());

        //绿色闪烁效果（与挖掘预览一致）
        float time = (System.currentTimeMillis() % 2000) / 2000.0f;
        float alpha = 0.5f + 0.3f * (float) Math.sin(time * Math.PI * 2);
        Matrix4f matrix = poseStack.last().pose();

        float x1 = arenaPreviewMin.getX();
        float y1 = arenaPreviewMin.getY();
        float z1 = arenaPreviewMin.getZ();
        float x2 = arenaPreviewMax.getX() + 1.0f;
        float y2 = arenaPreviewMax.getY() + 1.0f;
        float z2 = arenaPreviewMax.getZ() + 1.0f;

        //底面
        addLine(buffer, matrix, x1, y1, z1, x2, y1, z1, 0.0f, 1.0f, 0.0f, alpha);
        addLine(buffer, matrix, x2, y1, z1, x2, y1, z2, 0.0f, 1.0f, 0.0f, alpha);
        addLine(buffer, matrix, x2, y1, z2, x1, y1, z2, 0.0f, 1.0f, 0.0f, alpha);
        addLine(buffer, matrix, x1, y1, z2, x1, y1, z1, 0.0f, 1.0f, 0.0f, alpha);

        //顶面
        addLine(buffer, matrix, x1, y2, z1, x2, y2, z1, 0.0f, 1.0f, 0.0f, alpha);
        addLine(buffer, matrix, x2, y2, z1, x2, y2, z2, 0.0f, 1.0f, 0.0f, alpha);
        addLine(buffer, matrix, x2, y2, z2, x1, y2, z2, 0.0f, 1.0f, 0.0f, alpha);
        addLine(buffer, matrix, x1, y2, z2, x1, y2, z1, 0.0f, 1.0f, 0.0f, alpha);

        //竖直边
        addLine(buffer, matrix, x1, y1, z1, x1, y2, z1, 0.0f, 1.0f, 0.0f, alpha);
        addLine(buffer, matrix, x2, y1, z1, x2, y2, z1, 0.0f, 1.0f, 0.0f, alpha);
        addLine(buffer, matrix, x2, y1, z2, x2, y2, z2, 0.0f, 1.0f, 0.0f, alpha);
        addLine(buffer, matrix, x1, y1, z2, x1, y2, z2, 0.0f, 1.0f, 0.0f, alpha);

        buf.endBatch();
        poseStack.popPose();
    }

    //========== 龙魂灯笼范围渲染方法 ==========

    //渲染所有激活的龙魂灯笼范围
    private static void renderDragonSoulLanternRanges(PoseStack poseStack, Camera camera, Level level) {
        List<BlockPos> activeLanterns = findNearbyActiveLanterns(level, Minecraft.getInstance().player.blockPosition());
        if (activeLanterns.isEmpty()) {
            return;
        }

        Vec3 cameraPos = camera.getPosition();
        poseStack.pushPose();
        poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        MultiBufferSource.BufferSource buf = Minecraft.getInstance().renderBuffers.bufferSource();
        VertexConsumer buffer = buf.getBuffer(RenderType.lines());

        //紫色呼吸效果
        float time = (System.currentTimeMillis() % 2000) / 2000.0f;
        float alpha = 0.4f + 0.3f * (float) Math.sin(time * Math.PI * 2);
        float red = 0.5f;
        float green = 0.0f;
        float blue = 1.0f;
        Matrix4f matrix = poseStack.last().pose();

        for (BlockPos lanternPos : activeLanterns) {
            renderLanternRangeBox(buffer, matrix, lanternPos, red, green, blue, alpha);
        }

        buf.endBatch();
        poseStack.popPose();
    }

    //查找附近激活的龙魂灯笼
    private static List<BlockPos> findNearbyActiveLanterns(Level level, BlockPos playerPos) {
        List<BlockPos> lanterns = new ArrayList<>();
        int range = (int) Math.ceil(LANTERN_SEARCH_RANGE);

        for (int x = -range; x <= range; x++) {
            for (int y = -range; y <= range; y++) {
                for (int z = -range; z <= range; z++) {
                    BlockPos checkPos = playerPos.offset(x, y, z);

                    if (playerPos.distSqr(checkPos) <= LANTERN_SEARCH_RANGE * LANTERN_SEARCH_RANGE) {
                        BlockState state = level.getBlockState(checkPos);
                        if (state.getBlock() == ModBlocks.DRAGON_SOUL_LANTERN.get()) {
                            if (isLanternActivated(level, checkPos)) {
                                lanterns.add(checkPos);
                            }
                        }
                    }
                }
            }
        }

        return lanterns;
    }

    //检查龙魂灯笼是否激活（底部有黑曜石或哭泣的黑曜石）
    private static boolean isLanternActivated(Level level, BlockPos lanternPos) {
        BlockPos belowPos = lanternPos.below();
        BlockState belowState = level.getBlockState(belowPos);
        return belowState.is(Blocks.OBSIDIAN) ||
               belowState.is(Blocks.CRYING_OBSIDIAN);
    }

    //渲染龙魂灯笼范围方框
    private static void renderLanternRangeBox(VertexConsumer buffer, Matrix4f matrix, BlockPos center,
                                              float red, float green, float blue, float alpha) {
        int range = (int) LANTERN_RANGE;

        float x1 = center.getX() - range;
        float y1 = center.getY() - range;
        float z1 = center.getZ() - range;
        float x2 = center.getX() + range + 1;
        float y2 = center.getY() + range + 1;
        float z2 = center.getZ() + range + 1;

        //底面的4条边
        addLine(buffer, matrix, x1, y1, z1, x2, y1, z1, red, green, blue, alpha);
        addLine(buffer, matrix, x2, y1, z1, x2, y1, z2, red, green, blue, alpha);
        addLine(buffer, matrix, x2, y1, z2, x1, y1, z2, red, green, blue, alpha);
        addLine(buffer, matrix, x1, y1, z2, x1, y1, z1, red, green, blue, alpha);

        //顶面的4条边
        addLine(buffer, matrix, x1, y2, z1, x2, y2, z1, red, green, blue, alpha);
        addLine(buffer, matrix, x2, y2, z1, x2, y2, z2, red, green, blue, alpha);
        addLine(buffer, matrix, x2, y2, z2, x1, y2, z2, red, green, blue, alpha);
        addLine(buffer, matrix, x1, y2, z2, x1, y2, z1, red, green, blue, alpha);

        //4条竖直边
        addLine(buffer, matrix, x1, y1, z1, x1, y2, z1, red, green, blue, alpha);
        addLine(buffer, matrix, x2, y1, z1, x2, y2, z1, red, green, blue, alpha);
        addLine(buffer, matrix, x2, y1, z2, x2, y2, z2, red, green, blue, alpha);
        addLine(buffer, matrix, x1, y1, z2, x1, y2, z2, red, green, blue, alpha);
    }
}
