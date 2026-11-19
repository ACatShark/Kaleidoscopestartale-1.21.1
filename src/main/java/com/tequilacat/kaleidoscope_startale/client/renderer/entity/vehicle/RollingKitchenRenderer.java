package com.tequilacat.kaleidoscope_startale.client.renderer.entity.vehicle;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.tequilacat.kaleidoscope_startale.KaleidoscopeStartale;
import com.tequilacat.kaleidoscope_startale.client.model.ModModelLayers;
import com.tequilacat.kaleidoscope_startale.client.model.entity.vehicle.RollingKitchenModel;
import com.tequilacat.kaleidoscope_startale.entity.RollingKitchenEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * 流动小厨载具渲染器。
 * <p>
 * 模型由 Blockbench 导出，贴图 128×128；模型本身朝 -Z 方向建模，
 * 因此旋转按「180° - 朝向」处理，与原版生物渲染器的做法一致。
 * <p>
 * 模型的几何中心在 Z 轴正方向约 2.45 格处，渲染前先平移把中心对到实体原点，
 * 否则转向时会绕着车头打转。
 */
public class RollingKitchenRenderer extends EntityRenderer<RollingKitchenEntity> {

    /** 载具贴图（暂为纯白占位）。 */
    private static final ResourceLocation TEXTURE =
            KaleidoscopeStartale.id("textures/entity/vehicle/rolling_kitchen.png");

    /** 模型几何中心相对实体原点的 Z 偏移（格）。 */
    private static final double MODEL_Z_OFFSET = 2.45D;

    /** 模型相对实体原点的垂直偏移（与原版生物渲染器一致）。 */
    private static final double MODEL_Y_OFFSET = 1.501D;

    private final RollingKitchenModel model;

    /**
     * 构造渲染器。
     *
     * @param context 渲染器上下文
     */
    public RollingKitchenRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new RollingKitchenModel(context.bakeLayer(ModModelLayers.ROLLING_KITCHEN));
        this.shadowRadius = 1.3F;
    }

    @Override
    public ResourceLocation getTextureLocation(RollingKitchenEntity entity) {
        return TEXTURE;
    }

    @Override
    public void render(RollingKitchenEntity entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        float yaw = Mth.rotLerp(partialTick, entity.yRotO, entity.getYRot());
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - yaw));
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.translate(0.0D, -MODEL_Y_OFFSET, -MODEL_Z_OFFSET);

        VertexConsumer consumer = buffer.getBuffer(this.model.renderType(TEXTURE));
        this.model.renderToBuffer(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY, -1);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }
}
