package tequila.cat.pigpig.client;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;

import tequila.cat.pigpig.KaleidoscopeStartale;
import tequila.cat.pigpig.data.PetterSqueeze;

/** 被捏的玩家自己视角里的镜头晃动与画面压扁（第一人称看不到自己的模型）。 */
@EventBusSubscriber(modid = KaleidoscopeStartale.MODID, value = Dist.CLIENT)
public final class PetterSqueezeViewEvents {
    private static final float SHAKE_ROLL_DEGREES = 5.0F;
    private static final float SHAKE_PITCH_DEGREES = 1.5F;
    private static final float SHAKE_SPEED = 1.6F;

    private static boolean guiSquashed;

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

    private static float partialTick() {
        return Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);
    }

    private PetterSqueezeViewEvents() {
    }
}
