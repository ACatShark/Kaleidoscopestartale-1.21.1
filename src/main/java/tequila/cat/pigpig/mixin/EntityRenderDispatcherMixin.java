package tequila.cat.pigpig.mixin;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import tequila.cat.pigpig.data.PetterSqueeze;

/**
 * 在所有实体渲染入口统一做被捏时的压扁缩放。
 * 这里不能挂在 RenderLivingEvent.Pre 上：那个事件只在 LivingEntityRenderer#render 里触发，
 * 末影龙这种直接继承 EntityRenderer、或走自定义渲染器的生物完全收不到。
 */
@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {
    @Unique
    private boolean startale$squashPushed;

    @Inject(
            method = "render(Lnet/minecraft/world/entity/Entity;DDDFFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At("HEAD")
    )
    private void startale$pushSquash(Entity entity, double x, double y, double z, float yaw, float partialTick,
                                     PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                                     CallbackInfo ci) {
        startale$squashPushed = false;
        if (!(entity instanceof LivingEntity living)) {
            return;
        }
        float yScale = PetterSqueeze.yScale(living, partialTick);
        if (yScale == 1.0F) {
            return;
        }
        float xzScale = PetterSqueeze.xzScale(yScale);
        poseStack.pushPose();
        poseStack.translate(x, y, z);
        poseStack.scale(xzScale, yScale, xzScale);
        poseStack.translate(-x, -y, -z);
        startale$squashPushed = true;
    }

    @Inject(
            method = "render(Lnet/minecraft/world/entity/Entity;DDDFFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At("RETURN")
    )
    private void startale$popSquash(Entity entity, double x, double y, double z, float yaw, float partialTick,
                                    PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                                    CallbackInfo ci) {
        if (startale$squashPushed) {
            poseStack.popPose();
            startale$squashPushed = false;
        }
    }
}
