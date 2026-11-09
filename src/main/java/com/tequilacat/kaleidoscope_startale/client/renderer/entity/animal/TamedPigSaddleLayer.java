package com.tequilacat.kaleidoscope_startale.client.renderer.entity.animal;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.tequilacat.kaleidoscope_startale.client.model.entity.switchable.SwitchablePigModel;
import net.minecraft.client.model.PigModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Pig;

/**
 * 已驯养猪鞍具渲染层。
 * <p>
 * 仅在猪已装备鞍具时渲染马鞍：同步父模型属性到鞍具模型，
 * 更新动画后以指定贴图绘制。
 */
public class TamedPigSaddleLayer extends RenderLayer<Pig, SwitchablePigModel> {

    /** 鞍具模型。 */
    private final PigModel<Pig> saddleModel;

    /** 鞍具贴图。 */
    private final ResourceLocation texture;

    /**
     * 构造已驯养猪鞍具渲染层。
     *
     * @param parent      父渲染层
     * @param saddleModel 鞍具模型
     * @param texture     鞍具贴图
     */
    public TamedPigSaddleLayer(RenderLayerParent<Pig, SwitchablePigModel> parent, PigModel<Pig> saddleModel, ResourceLocation texture) {
        super(parent);
        this.saddleModel = saddleModel;
        this.texture = texture;
    }

    /**
     * 渲染鞍具。
     * <p>
     * 若猪未装备鞍具则跳过；否则同步父模型属性、更新鞍具模型动画，
     * 并以不剔除的裁剪贴图绘制。
     *
     * @param poseStack       姿态栈
     * @param buffer          缓冲区
     * @param packedLight     打包光照
     * @param pig             猪实体
     * @param limbSwing       肢体摆动
     * @param limbSwingAmount 肢体摆动幅度
     * @param partialTicks    插值刻
     * @param ageInTicks      存在刻数
     * @param netHeadYaw      头部偏航
     * @param headPitch       头部俯仰
     */
    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, Pig pig,
                       float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        if (!pig.isSaddled()) {
            return;
        }
        ((SwitchablePigModel) this.getParentModel()).copyPropertiesTo(this.saddleModel);
        this.saddleModel.prepareMobModel(pig, limbSwing, limbSwingAmount, partialTicks);
        this.saddleModel.setupAnim(pig, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(this.texture));
        this.saddleModel.renderToBuffer(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY, -1);
    }
}