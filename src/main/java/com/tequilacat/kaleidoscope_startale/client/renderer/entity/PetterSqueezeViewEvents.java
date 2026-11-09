package com.tequilacat.kaleidoscope_startale.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.tequilacat.kaleidoscope_startale.KaleidoscopeStartale;
import com.tequilacat.kaleidoscope_startale.util.PetterSqueeze;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;

/**
 * 拍击挤压视角与界面事件类。
 * <p>
 * 客户端表现：玩家被拍扁期间，摄像机角度产生轻微摇晃，
 * 同时整个 GUI 以屏幕中心为基准做等比例的纵向压扁变换。
 */
@EventBusSubscriber(modid = KaleidoscopeStartale.MODID, value = Dist.CLIENT)
public class PetterSqueezeViewEvents {

    /** 摇晃的最大滚转角度（度）。 */
    private static final float SHAKE_ROLL_DEGREES = 5.0F;

    /** 摇晃的最大俯仰角度（度）。 */
    private static final float SHAKE_PITCH_DEGREES = 1.5F;

    /** 摇晃速度。 */
    private static final float SHAKE_SPEED = 1.6F;

    /** 标记 GUI 是否已压入挤压变换。 */
    private static boolean guiSquashed;

    /**
     * 处理摄像机角度计算事件。
     * <p>
     * 玩家被压扁时，按挤压程度对滚转与俯仰角叠加正弦摇晃。
     *
     * @param event 摄像机角度计算事件
     */
    @SubscribeEvent
    public static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        float squash = squashAmount(partialTick());
        if (squash <= 0.0F) {
            return;
        }
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        float phase = player.tickCount + partialTick();
        event.setRoll(event.getRoll() + Mth.sin(phase * SHAKE_SPEED) * SHAKE_ROLL_DEGREES * squash);
        event.setPitch(event.getPitch() + Mth.sin(phase * SHAKE_SPEED * 1.7F) * SHAKE_PITCH_DEGREES * squash);
    }

    /**
     * 处理 GUI 渲染前事件。
     * <p>
     * 玩家被压扁时以屏幕中心为基准缩放 GUI 并压入姿态，
     * 并标记已压入以避免重复弹出。
     *
     * @param event GUI 渲染前事件
     */
    @SubscribeEvent
    public static void onRenderGuiPre(RenderGuiEvent.Pre event) {
        float squash = squashAmount(partialTick());
        if (squash <= 0.0F) {
            return;
        }
        float yScale = 1.0F - squash;
        float xzScale = PetterSqueeze.xzScale(yScale);
        Minecraft minecraft = Minecraft.getInstance();
        float centerX = minecraft.getWindow().getGuiScaledWidth() / 2.0F;
        float centerY = minecraft.getWindow().getGuiScaledHeight() / 2.0F;

        PoseStack pose = event.getGuiGraphics().pose();
        pose.pushPose();
        pose.translate(centerX, centerY, 0.0F);
        pose.scale(xzScale, yScale, 1.0F);
        pose.translate(-centerX, -centerY, 0.0F);
        guiSquashed = true;
    }

    /**
     * 处理 GUI 渲染后事件。
     * <p>
     * 若此前压入过挤压姿态，则弹出恢复。
     *
     * @param event GUI 渲染后事件
     */
    @SubscribeEvent
    public static void onRenderGuiPost(RenderGuiEvent.Post event) {
        if (!guiSquashed) {
            return;
        }
        event.getGuiGraphics().pose().popPose();
        guiSquashed = false;
    }

    /** 0 表示没有被捏，数值越大压得越扁。 */
    private static float squashAmount(float partialTick) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return 0.0F;
        }
        float yScale = PetterSqueeze.yScale(player, partialTick);
        return yScale >= 1.0F ? 0.0F : 1.0F - yScale;
    }

    /**
     * 获取当前插值刻。
     *
     * @return 插值刻
     */
    private static float partialTick() {
        return Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);
    }
}