package tequila.cat.pigpig.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.ChickenModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.animal.Chicken;

/** 一个渲染器两套模型：普通鸡 / 已驯养（戴帽子）的鸡，按需切换。 */
public class SwitchableChickenModel extends ChickenModel<Chicken> {
    private final ChickenModel<Chicken> vanilla;
    private final TamedChickenModel tamed;
    private boolean tamedActive;

    public SwitchableChickenModel(ModelPart vanillaRoot, ModelPart tamedRoot) {
        super(vanillaRoot);
        this.vanilla = new ChickenModel<>(vanillaRoot);
        this.tamed = new TamedChickenModel(tamedRoot);
    }

    public void setTamed(boolean tamed) {
        this.tamedActive = tamed;
    }

    @Override
    public void setupAnim(Chicken entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
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
