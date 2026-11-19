package com.tequilacat.kaleidoscope_startale.client.renderer.entity.vehicle;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.tequilacat.kaleidoscope_startale.KaleidoscopeStartale;
import com.tequilacat.kaleidoscope_startale.client.model.ModModelLayers;
import com.tequilacat.kaleidoscope_startale.client.model.entity.vehicle.WiggleCarModel;
import com.tequilacat.kaleidoscope_startale.entity.WiggleCarEntity;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * 扭扭车渲染器。
 * <p>
 * 模型由 Blockbench 导出，贴图 64×64；模型朝 -Z 方向建模，
 * 因此旋转按「180° - 朝向」处理，与流动小厨保持一致。
 */
public class WiggleCarRenderer extends EntityRenderer<WiggleCarEntity> {

    /** 载具贴图。 */
    private static final ResourceLocation TEXTURE = KaleidoscopeStartale.id("textures/entity/vehicle/wiggle_car.png");

    /** 模型几何中心相对实体原点的 Z 偏移（格）。 */
    private static final double MODEL_Z_OFFSET = 0.125D;
    /** 模型相对实体原点的垂直偏移。 */
    private static final double MODEL_Y_OFFSET = 1.501D;

    /** 扭扭车模型。 */
    private final WiggleCarModel model;

    /**
     * 构造渲染器。
     *
     * @param context 渲染器上下文
     */
    public WiggleCarRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new WiggleCarModel(context.bakeLayer(ModModelLayers.WIGGLE_CAR));
        this.shadowRadius = 0.4F;
    }

    /**
     * 获取实体贴图。
     *
     * @param entity 扭扭车
     * @return 贴图路径
     */
    @Override
    public ResourceLocation getTextureLocation(WiggleCarEntity entity) {
        return TEXTURE;
    }

    /**
     * 渲染扭扭车。
     * <p>
     * 先按插值后的朝向旋转并翻转模型坐标系，再把模型下移到实体原点，
     * 随后更新动画状态并绘制模型。年龄参数传入插值刻，其余动画参数置零。
     *
     * @param entity      扭扭车
     * @param entityYaw   实体偏航角
     * @param partialTick 插值刻
     * @param poseStack   姿态栈
     * @param buffer      顶点缓冲
     * @param packedLight 打包光照
     */
    @Override
    public void render(WiggleCarEntity entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        float yaw = Mth.rotLerp(partialTick, entity.yRotO, entity.getYRot());
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - yaw));
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.translate(0.0D, -MODEL_Y_OFFSET, -MODEL_Z_OFFSET);
        this.model.setupAnim(entity, 0.0F, 0.0F, partialTick, 0.0F, 0.0F);
        VertexConsumer consumer = buffer.getBuffer(this.model.renderType(TEXTURE));
        this.model.renderToBuffer(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY, -1);
        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }
}
