package tequila.cat.pigpig.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.PigModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.animal.Pig;

/** 一个渲染器两套模型：普通猪 / 已驯养（戴草帽）的猪，按需切换。 */
public class SwitchablePigModel extends PigModel<Pig> {
    private final PigModel<Pig> vanilla;
    private final TamedPigModel tamed;
    private boolean tamedActive;

    public SwitchablePigModel(ModelPart vanillaRoot, ModelPart tamedRoot) {
        super(vanillaRoot);
        this.vanilla = new PigModel<>(vanillaRoot);
        this.tamed = new TamedPigModel(tamedRoot);
    }

    public void setTamed(boolean tamed) {
        this.tamedActive = tamed;
    }

    @Override
    public void setupAnim(Pig entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        if (this.tamedActive) {
            this.tamed.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        } else {
            this.vanilla.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer consumer, int packedLight, int packedOverlay, int color) {
        if (this.tamedActive) {
            this.tamed.young = this.young;
            this.tamed.attackTime = this.attackTime;
            this.tamed.renderToBuffer(poseStack, consumer, packedLight, packedOverlay, color);
        } else {
            this.vanilla.young = this.young;
            this.vanilla.attackTime = this.attackTime;
            this.vanilla.renderToBuffer(poseStack, consumer, packedLight, packedOverlay, color);
        }
    }
}
