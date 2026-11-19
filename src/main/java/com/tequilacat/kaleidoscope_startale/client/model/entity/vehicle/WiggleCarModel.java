package com.tequilacat.kaleidoscope_startale.client.model.entity.vehicle;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.tequilacat.kaleidoscope_startale.entity.WiggleCarEntity;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * 扭扭车的实体模型。
 * <p>
 * 车把按左右扭动的方向偏转，前后轮跟随速度滚动。
 * 车把与车轮的角度由实体每刻计算，在本模型里缓存后在渲染时应用。
 */
public class WiggleCarModel extends EntityModel<WiggleCarEntity> {

    /** 车架。 */
    private final ModelPart rack;
    /** 车把，挂载于车架之上。 */
    private final ModelPart handle;
    /** 前轮，挂载于车把之上，随车把一起偏转。 */
    private final ModelPart wheelF;
    /** 后轮，挂载于车架之上。 */
    private final ModelPart wheelH;
    /** 当前车把偏转角。 */
    private float handleTilt;
    /** 当前车轮滚动角。 */
    private float wheelRotation;

    /**
     * 构造扭扭车模型。
     * <p>
     * 前轮从车把获取、后轮从车架获取，二者层级不同。
     *
     * @param root 根模型部件
     */
    public WiggleCarModel(ModelPart root) {
        super(RenderType::entityCutoutNoCull);
        this.rack = root.getChild("rack");
        this.handle = this.rack.getChild("handle");
        this.wheelF = this.handle.getChild("wheel_f");
        this.wheelH = this.rack.getChild("wheel_h");
    }

    /**
     * 构建扭扭车的身体层定义。
     * <p>
     * 定义车架、车把、前轮与后轮，车把上的把手补块、车轮上的辐条补块
     * 均以子部件形式挂载到对应部件下。
     *
     * @return 层定义
     */
    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition rack = root.addOrReplaceChild("rack", CubeListBuilder.create()
                        .texOffs(44, 14).addBox(-3.0F, -2.0F, 5.0F, 6.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
                        .texOffs(40, 26).addBox(-2.0F, -3.0F, 4.0F, 4.0F, 1.0F, 6.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 0).addBox(-6.0F, -2.0F, 9.0F, 12.0F, 2.0F, 12.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 26).addBox(-5.0F, -3.0F, 10.0F, 10.0F, 1.0F, 10.0F, new CubeDeformation(0.0F))
                        .texOffs(14, 49).addBox(-2.0F, -5.0F, 20.0F, 4.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(20, 53).addBox(2.0F, -8.0F, 20.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(48, 53).addBox(-3.0F, -8.0F, 20.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(48, 11).addBox(-3.0F, -6.0F, 20.0F, 6.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 49).addBox(-3.0F, -9.0F, 20.0F, 6.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(30, 44).addBox(-2.0F, -7.0F, -2.0F, 4.0F, 2.0F, 5.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 14).addBox(-6.0F, -2.0F, -5.0F, 12.0F, 2.0F, 10.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 37).addBox(-3.0F, -5.0F, -5.0F, 6.0F, 3.0F, 9.0F, new CubeDeformation(0.0F))
                        .texOffs(44, 20).addBox(-2.0F, 0.0F, 17.0F, 4.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 20.0F, -6.0F));

        PartDefinition handle = rack.addOrReplaceChild("handle", CubeListBuilder.create()
                        .texOffs(48, 0).addBox(-1.0F, 0.0F, -2.0F, 2.0F, 9.0F, 2.0F, new CubeDeformation(0.0F))
                        .texOffs(30, 37).addBox(-5.0F, -1.0F, -3.0F, 10.0F, 1.0F, 6.0F, new CubeDeformation(0.0F))
                        .texOffs(48, 49).addBox(-1.0F, 9.0F, -4.0F, 2.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
                        .texOffs(48, 44).addBox(-2.0F, 9.0F, -1.0F, 4.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, -8.0F, 1.0F));

        handle.addOrReplaceChild("cube_r1", CubeListBuilder.create()
                        .texOffs(14, 53).addBox(0.0F, -2.0F, -1.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                        .texOffs(8, 51).addBox(-3.0F, -2.0F, -1.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(1.0F, 10.0F, -4.0F, -0.7854F, 0.0F, 0.0F));

        PartDefinition wheelF = handle.addOrReplaceChild("wheel_f", CubeListBuilder.create()
                        .texOffs(40, 35).addBox(-4.0F, -0.5F, -0.5F, 8.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 10.5F, 1.5F));

        wheelF.addOrReplaceChild("cube_r2", CubeListBuilder.create()
                        .texOffs(0, 51).addBox(0.0F, -3.0F, -1.0F, 1.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
                        .texOffs(24, 51).addBox(5.0F, -3.0F, -1.0F, 1.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-3.0F, 0.5F, -1.5F, -0.7854F, 0.0F, 0.0F));

        PartDefinition wheelH = rack.addOrReplaceChild("wheel_h", CubeListBuilder.create()
                        .texOffs(40, 33).addBox(-6.0F, -0.5F, -0.5F, 8.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(2.0F, 2.5F, 18.5F));

        wheelH.addOrReplaceChild("cube_r3", CubeListBuilder.create()
                        .texOffs(40, 51).addBox(0.0F, -3.0F, -1.0F, 1.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
                        .texOffs(32, 51).addBox(5.0F, -3.0F, -1.0F, 1.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-5.0F, 0.5F, -1.5F, -0.7854F, 0.0F, 0.0F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    /**
     * 设置车把与车轮的动画状态。
     * <p>
     * 从实体取出当刻的偏转角与滚动角缓存下来，供渲染时使用。
     *
     * @param entity          扭扭车
     * @param limbSwing       行走摆动（未使用）
     * @param limbSwingAmount 行走幅度（未使用）
     * @param ageInTicks      存在时间
     * @param netHeadYaw      头部偏转（未使用）
     * @param headPitch       头部俯仰（未使用）
     */
    @Override
    public void setupAnim(WiggleCarEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        this.handleTilt = entity.handleTilt();
        this.wheelRotation = entity.wheelRotation();
    }

    /**
     * 渲染模型。
     * <p>
     * 渲染前把缓存的角度写入车把与车轮，渲染车架后把车把偏转复位，
     * 避免角度残留影响下一帧。前轮与后轮均为子部件，随车架一并渲染。
     *
     * @param poseStack      姿态栈
     * @param vertexConsumer 顶点消费者
     * @param packedLight    打包光照坐标
     * @param packedOverlay  打包覆写坐标
     * @param color          叠加颜色
     */
    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer,
                               int packedLight, int packedOverlay, int color) {
        this.handle.yRot = this.handleTilt;
        this.wheelF.xRot = this.wheelRotation;
        this.wheelH.xRot = this.wheelRotation;
        this.rack.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.handle.yRot = 0.0F;
    }
}
