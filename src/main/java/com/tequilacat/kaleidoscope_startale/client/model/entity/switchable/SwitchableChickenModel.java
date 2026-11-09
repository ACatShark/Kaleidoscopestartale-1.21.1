package com.tequilacat.kaleidoscope_startale.client.model.entity.switchable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.tequilacat.kaleidoscope_startale.client.model.entity.tamed.TamedChickenModel;
import net.minecraft.client.model.ChickenModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.animal.Chicken;

/**
 * 可切换的鸡模型。
 * <p>
 * 同时持有原版鸡模型与已驯养鸡模型，根据驯养状态切换，
 * 在设置动画与渲染时委托给当前激活的模型，
 * 并同步幼年状态与攻击时间。
 */
public class SwitchableChickenModel extends ChickenModel<Chicken> {

    /** 原版鸡模型。 */
    private final ChickenModel<Chicken> vanilla;

    /** 已驯养鸡模型。 */
    private final TamedChickenModel tamed;

    /** 当前是否使用已驯养模型。 */
    private boolean tamedActive;

    /**
     * 构造可切换的鸡模型。
     *
     * @param vanillaRoot 原版模型根部件
     * @param tamedRoot   已驯养模型根部件
     */
    public SwitchableChickenModel(ModelPart vanillaRoot, ModelPart tamedRoot) {
        super(vanillaRoot);
        this.vanilla = new ChickenModel<>(vanillaRoot);
        this.tamed = new TamedChickenModel(tamedRoot);
    }

    /**
     * 设置是否使用已驯养模型。
     *
     * @param tamed 是否使用已驯养模型
     */
    public void setTamed(boolean tamed) {
        this.tamedActive = tamed;
    }

    /**
     * 设置模型动画。
     * <p>
     * 根据驯养状态委托给对应模型的动画设置。
     *
     * @param entity         鸡实体
     * @param limbSwing      肢体摆动
     * @param limbSwingAmount 肢体摆动幅度
     * @param ageInTicks     存在刻数
     * @param netHeadYaw     头部偏航
     * @param headPitch      头部俯仰
     */
    @Override
    public void setupAnim(Chicken entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        if (this.tamedActive) {
            this.tamed.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        } else {
            this.vanilla.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        }
    }

    /**
     * 渲染模型。
     * <p>
     * 根据驯养状态委托给对应模型渲染，并同步幼年状态与攻击时间。
     *
     * @param poseStack    姿态栈
     * @param consumer     顶点消费者
     * @param packedLight  打包光照
     * @param packedOverlay 打包覆盖
     * @param color        颜色
     */
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