package com.tequilacat.kaleidoscope_startale.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.tequilacat.kaleidoscope_startale.util.PetterSqueeze;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 实体渲染调度器混入类。
 * <p>
 * 在实体渲染前后对活体实体应用挤压形变变换，
 * 根据 PetterSqueeze 提供的缩放比例调整模型显示。
 */
@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {

    /**
     * 标记是否已压入挤压变换矩阵。
     */
    @Unique
    private boolean KS$squashPushed;

    /**
     * 压入挤压变换。
     * <p>
     * 在渲染开始时计算活体实体的纵向缩放比例，
     * 若非默认比例则以实体位置为中心缩放并压入矩阵。
     *
     * @param entity      被渲染实体
     * @param x           渲染 X 坐标
     * @param y           渲染 Y 坐标
     * @param z           渲染 Z 坐标
     * @param yaw         偏航角
     * @param partialTick 插值刻
     * @param poseStack   姿态栈
     * @param buffer      缓冲区
     * @param packedLight 打包光照
     * @param ci          回调信息
     */
    @Inject(
            method = "render(Lnet/minecraft/world/entity/Entity;DDDFFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At("HEAD")
    )
    private void KS$pushSquash(Entity entity, double x, double y, double z, float yaw, float partialTick,
                               PoseStack poseStack, MultiBufferSource buffer, int packedLight, CallbackInfo ci) {
        KS$squashPushed = false;
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
        KS$squashPushed = true;
    }

    /**
     * 弹出挤压变换。
     * <p>
     * 在渲染结束时若此前压入过挤压矩阵则将其弹出，恢复姿态栈。
     *
     * @param entity      被渲染实体
     * @param x           渲染 X 坐标
     * @param y           渲染 Y 坐标
     * @param z           渲染 Z 坐标
     * @param yaw         偏航角
     * @param partialTick 插值刻
     * @param poseStack   姿态栈
     * @param buffer      缓冲区
     * @param packedLight 打包光照
     * @param ci          回调信息
     */
    @Inject(
            method = "render(Lnet/minecraft/world/entity/Entity;DDDFFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At("RETURN")
    )
    private void KS$popSquash(Entity entity, double x, double y, double z, float yaw, float partialTick,
                              PoseStack poseStack, MultiBufferSource buffer, int packedLight, CallbackInfo ci) {
        if (KS$squashPushed) {
            poseStack.popPose();
            KS$squashPushed = false;
        }
    }
}