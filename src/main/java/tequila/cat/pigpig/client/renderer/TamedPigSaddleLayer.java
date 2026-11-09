package tequila.cat.pigpig.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.PigModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Pig;

import tequila.cat.pigpig.client.model.SwitchablePigModel;

/** 因为换掉了原版猪渲染器，这里补回鞍的渲染。 */
public class TamedPigSaddleLayer extends RenderLayer<Pig, SwitchablePigModel> {
    private final PigModel<Pig> saddleModel;
    private final ResourceLocation texture;

    public TamedPigSaddleLayer(RenderLayerParent<Pig, SwitchablePigModel> parent, PigModel<Pig> saddleModel, ResourceLocation texture) {
        super(parent);
        this.saddleModel = saddleModel;
        this.texture = texture;
    }

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
