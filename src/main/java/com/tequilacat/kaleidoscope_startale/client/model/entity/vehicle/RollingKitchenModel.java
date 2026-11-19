package com.tequilacat.kaleidoscope_startale.client.model.entity.vehicle;

import com.tequilacat.kaleidoscope_startale.entity.RollingKitchenEntity;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

/**
 * 流动小厨载具模型。
 * <p>
 * 由车身、把手、六个轮胎与底盘围板组成，木箱挂载在车身之下随车身渲染，
 * 模型为静态几何，不含动画。
 */
public class RollingKitchenModel extends EntityModel<RollingKitchenEntity> {

    /** 车身。 */
    private final ModelPart body;
    /** 木箱，挂载于车身之下。 */
    private final ModelPart chest;
    /** 把手。 */
    private final ModelPart handle;
    /** 前轮。 */
    private final ModelPart front_t;
    /** 后轮，左侧。 */
    private final ModelPart rear_t2;
    /** 后轮，右侧。 */
    private final ModelPart rear_t;
    /** 后轮，左侧。 */
    private final ModelPart rear_t3;
    /** 后轮，右侧。 */
    private final ModelPart rear_t4;
    /** 后轮，左侧。 */
    private final ModelPart rear_t5;
    /** 后轮，右侧。 */
    private final ModelPart rear_t6;
    /** 底盘围板。 */
    private final ModelPart bb_main;

    /**
     * 构造流动小厨载具模型。
     *
     * @param root 根模型部件
     */
    public RollingKitchenModel(ModelPart root) {
        super(RenderType::entityCutoutNoCull);
        this.body = root.getChild("body");
        this.chest = this.body.getChild("chest");
        this.handle = root.getChild("handle");
        this.front_t = root.getChild("front_t");
        this.rear_t2 = root.getChild("rear_t2");
        this.rear_t = root.getChild("rear_t");
        this.rear_t3 = root.getChild("rear_t3");
        this.rear_t4 = root.getChild("rear_t4");
        this.rear_t5 = root.getChild("rear_t5");
        this.rear_t6 = root.getChild("rear_t6");
        this.bb_main = root.getChild("bb_main");
    }

    /**
     * 构建流动小厨载具的身体层定义。
     * <p>
     * 定义车身、把手、六个轮胎与底盘围板，车身经旋转补块拼出斜面轮廓，
     * 木箱作为子部件挂载在车身之下。
     *
     * @return 层定义
     */
    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 25).addBox(-4.2542F, -1.8217F, -5.0417F, 8.5085F, 5.005F, 8.008F, new CubeDeformation(0.0F))
                .texOffs(26, 19).addBox(-7.25F, -1.8248F, 2.9567F, 14.5F, 6.0F, 17.0F, new CubeDeformation(0.0F))
                .texOffs(29, 0).addBox(-5.0F, -9.8248F, 2.9567F, 10.0F, 8.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(20, 45).addBox(-4.25F, -6.8248F, 14.9567F, 8.5F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 16.0F, 0.0F));

        PartDefinition cube_r1 = body.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(8, 36).addBox(2.25F, 3.8729F, -0.0478F, 1.0F, 19.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(14, 41).addBox(-3.25F, 3.8729F, -0.0478F, 1.0F, 19.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -19.149F, -10.074F, -0.3927F, 0.0F, 0.0F));
        PartDefinition cube_r2 = body.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(37, 12).addBox(-4.25F, -3.0F, -5.0F, 8.5F, 5.0F, 13.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -3.8958F, -9.2859F, -0.7854F, 0.0F, 0.0F));
        PartDefinition cube_r3 = body.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(31, 0).mirror().addBox(-4.2542F, -6.9902F, -9.7485F, 8.5085F, 10.0F, 5.005F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, -4.8603F, -6.5856F, -0.3927F, 0.0F, 0.0F));
        PartDefinition chest = body.addOrReplaceChild("chest", CubeListBuilder.create().texOffs(-10, -7).mirror().addBox(-1.0F, -1.0F, -1.5F, 1.5F, 1.0F, 12.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(-10, -7).addBox(4.5F, -1.0F, -1.5F, 1.5F, 1.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(1, 2).addBox(0.5F, -1.0F, -0.5F, 4.0F, 0.5F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(-96, -76).addBox(-21.0F, -9.0F, 1.0F, 47.0F, 2.0F, 78.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.5F, 2.25F, 22.25F));

        PartDefinition handle = partdefinition.addOrReplaceChild("handle", CubeListBuilder.create().texOffs(7, 38).mirror().addBox(-2.5F, -21.7981F, -13.3933F, 5.0F, 5.0F, 7.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 18.0F, 0.0F));

        PartDefinition cube_r4 = handle.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(38, 12).addBox(-11.1125F, -1.0F, -2.4329F, 10.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(52, 75).addBox(-6.1125F, -1.25F, -3.4329F, 5.0F, 2.5F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -19.2981F, -9.8779F, 0.0F, 0.3927F, 0.0F));

        PartDefinition cube_r5 = handle.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(70, 75).addBox(1.3435F, -1.25F, -3.0285F, 5.0F, 2.5F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(65, 0).addBox(1.3038F, -1.0F, -1.9709F, 10.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -19.2981F, -9.8779F, 0.0F, -0.3927F, 0.0F));

        PartDefinition front_t = partdefinition.addOrReplaceChild("front_t", CubeListBuilder.create().texOffs(8, 0).mirror().addBox(0.0F, 17.078F, -10.9615F, 0.0F, 8.0711F, 8.0711F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(22, 70).mirror().addBox(-2.002F, 18.611F, -12.9675F, 4.004F, 5.005F, 2.002F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(66, 48).addBox(-2.002F, 25.1531F, -9.4285F, 4.004F, 2.002F, 5.005F, new CubeDeformation(0.0F))
                .texOffs(34, 0).addBox(-2.002F, 18.611F, -2.8864F, 4.004F, 5.005F, 2.002F, new CubeDeformation(0.0F))
                .texOffs(34, 60).mirror().addBox(-2.002F, 15.0719F, -9.4285F, 4.004F, 2.002F, 5.005F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, -3.149F, -9.824F));

        PartDefinition cube_r6 = front_t.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(29, 53).mirror().addBox(-2.0F, -2.5F, -6.0355F, 4.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(66, 21).addBox(-2.0F, -6.0355F, -2.5F, 4.0F, 2.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(51, 70).addBox(-2.0F, -2.5F, 4.0355F, 4.0F, 5.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(60, 61).mirror().addBox(-2.0F, 4.0355F, -2.5F, 4.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(57, 18).addBox(-3.2344F, -1.5F, -1.5F, 6.4688F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 21.1135F, -6.926F, -0.7854F, 0.0F, 0.0F));

        PartDefinition rear_t2 = partdefinition.addOrReplaceChild("rear_t2", CubeListBuilder.create().texOffs(8, 0).mirror().addBox(-10.0F, 17.078F, 22.0385F, 0.0F, 8.0711F, 8.0711F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(22, 70).mirror().addBox(-12.002F, 18.611F, 20.0325F, 4.004F, 5.005F, 2.002F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(66, 48).addBox(-12.002F, 25.1531F, 23.5716F, 4.004F, 2.002F, 5.005F, new CubeDeformation(0.0F))
                .texOffs(34, 0).addBox(-12.002F, 18.611F, 30.1136F, 4.004F, 5.005F, 2.002F, new CubeDeformation(0.0F))
                .texOffs(34, 60).mirror().addBox(-12.002F, 15.0719F, 23.5716F, 4.004F, 2.002F, 5.005F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, -3.149F, -9.824F));

        PartDefinition cube_r7 = rear_t2.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(29, 53).mirror().addBox(-2.0F, -2.5F, -6.0355F, 4.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(66, 21).addBox(-2.0F, -6.0355F, -2.5F, 4.0F, 2.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(51, 70).addBox(-2.0F, -2.5F, 4.0355F, 4.0F, 5.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(60, 61).mirror().addBox(-2.0F, 4.0355F, -2.5F, 4.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-10.0F, 21.1135F, 26.074F, -0.7854F, 0.0F, 0.0F));

        PartDefinition cube_r8 = rear_t2.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(69, 32).addBox(-3.25F, -1.5F, -1.5F, 6.5F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-10.0F, 21.1135F, 26.074F, 0.7854F, 0.0F, 0.0F));

        PartDefinition rear_t = partdefinition.addOrReplaceChild("rear_t", CubeListBuilder.create().texOffs(8, 0).mirror().addBox(10.0F, 17.078F, 22.0385F, 0.0F, 8.0711F, 8.0711F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(22, 70).mirror().addBox(7.998F, 18.611F, 20.0325F, 4.004F, 5.005F, 2.002F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(66, 48).addBox(7.998F, 25.1531F, 23.5716F, 4.004F, 2.002F, 5.005F, new CubeDeformation(0.0F))
                .texOffs(34, 0).addBox(7.998F, 18.611F, 30.1136F, 4.004F, 5.005F, 2.002F, new CubeDeformation(0.0F))
                .texOffs(34, 60).mirror().addBox(7.998F, 15.0719F, 23.5716F, 4.004F, 2.002F, 5.005F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, -3.149F, -9.824F));

        PartDefinition cube_r9 = rear_t.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(29, 53).mirror().addBox(-2.0F, -2.5F, -6.0355F, 4.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(66, 21).addBox(-2.0F, -6.0355F, -2.5F, 4.0F, 2.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(51, 70).addBox(-2.0F, -2.5F, 4.0355F, 4.0F, 5.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(60, 61).mirror().addBox(-2.0F, 4.0355F, -2.5F, 4.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(10.0F, 21.1135F, 26.074F, -0.7854F, 0.0F, 0.0F));

        PartDefinition cube_r10 = rear_t.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(69, 32).addBox(-3.25F, -1.5F, -1.5F, 6.5F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.0F, 21.1135F, 26.074F, 0.7854F, 0.0F, 0.0F));

        PartDefinition rear_t3 = partdefinition.addOrReplaceChild("rear_t3", CubeListBuilder.create().texOffs(8, 0).mirror().addBox(0.0F, -4.0355F, -4.0355F, 0.0F, 8.0711F, 8.0711F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(22, 70).mirror().addBox(-2.002F, -2.5025F, -6.0416F, 4.004F, 5.005F, 2.002F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(66, 48).addBox(-2.002F, 4.0396F, -2.5025F, 4.004F, 2.002F, 5.005F, new CubeDeformation(0.0F))
                .texOffs(34, 0).addBox(-2.002F, -2.5025F, 4.0396F, 4.004F, 5.005F, 2.002F, new CubeDeformation(0.0F))
                .texOffs(34, 60).mirror().addBox(-2.002F, -6.0416F, -2.5025F, 4.004F, 2.002F, 5.005F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(22.0F, 17.9645F, 33.25F));

        PartDefinition cube_r11 = rear_t3.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(29, 53).mirror().addBox(-2.0F, -2.5F, -6.0355F, 4.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(66, 21).addBox(-2.0F, -6.0355F, -2.5F, 4.0F, 2.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(51, 70).addBox(-2.0F, -2.5F, 4.0355F, 4.0F, 5.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(60, 61).mirror().addBox(-2.0F, 4.0355F, -2.5F, 4.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.7854F, 0.0F, 0.0F));

        PartDefinition cube_r12 = rear_t3.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(69, 32).addBox(-3.25F, -1.5F, -1.5F, 6.5F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.7854F, 0.0F, 0.0F));

        PartDefinition rear_t4 = partdefinition.addOrReplaceChild("rear_t4", CubeListBuilder.create().texOffs(8, 0).mirror().addBox(0.0F, -4.0355F, -4.0355F, 0.0F, 8.0711F, 8.0711F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(22, 70).mirror().addBox(-2.002F, -2.5025F, -6.0416F, 4.004F, 5.005F, 2.002F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(66, 48).addBox(-2.002F, 4.0396F, -2.5025F, 4.004F, 2.002F, 5.005F, new CubeDeformation(0.0F))
                .texOffs(34, 0).addBox(-2.002F, -2.5025F, 4.0396F, 4.004F, 5.005F, 2.002F, new CubeDeformation(0.0F))
                .texOffs(34, 60).mirror().addBox(-2.002F, -6.0416F, -2.5025F, 4.004F, 2.002F, 5.005F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-22.0F, 17.9645F, 34.25F));

        PartDefinition cube_r13 = rear_t4.addOrReplaceChild("cube_r13", CubeListBuilder.create().texOffs(29, 53).mirror().addBox(-2.0F, -2.5F, -6.0355F, 4.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(66, 21).addBox(-2.0F, -6.0355F, -2.5F, 4.0F, 2.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(51, 70).addBox(-2.0F, -2.5F, 4.0355F, 4.0F, 5.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(60, 61).mirror().addBox(-2.0F, 4.0355F, -2.5F, 4.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.7854F, 0.0F, 0.0F));

        PartDefinition cube_r14 = rear_t4.addOrReplaceChild("cube_r14", CubeListBuilder.create().texOffs(69, 32).addBox(-3.25F, -1.5F, -1.5F, 6.5F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.7854F, 0.0F, 0.0F));

        PartDefinition rear_t5 = partdefinition.addOrReplaceChild("rear_t5", CubeListBuilder.create().texOffs(8, 0).mirror().addBox(0.0F, -4.0355F, -4.0355F, 0.0F, 8.0711F, 8.0711F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(22, 70).mirror().addBox(-2.002F, -2.5025F, -6.0416F, 4.004F, 5.005F, 2.002F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(66, 48).addBox(-2.002F, 4.0396F, -2.5025F, 4.004F, 2.002F, 5.005F, new CubeDeformation(0.0F))
                .texOffs(34, 0).addBox(-2.002F, -2.5025F, 4.0396F, 4.004F, 5.005F, 2.002F, new CubeDeformation(0.0F))
                .texOffs(34, 60).mirror().addBox(-2.002F, -6.0416F, -2.5025F, 4.004F, 2.002F, 5.005F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-22.0F, 17.9645F, 92.25F));

        PartDefinition cube_r15 = rear_t5.addOrReplaceChild("cube_r15", CubeListBuilder.create().texOffs(29, 53).mirror().addBox(-2.0F, -2.5F, -6.0355F, 4.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(66, 21).addBox(-2.0F, -6.0355F, -2.5F, 4.0F, 2.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(51, 70).addBox(-2.0F, -2.5F, 4.0355F, 4.0F, 5.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(60, 61).mirror().addBox(-2.0F, 4.0355F, -2.5F, 4.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.7854F, 0.0F, 0.0F));

        PartDefinition cube_r16 = rear_t5.addOrReplaceChild("cube_r16", CubeListBuilder.create().texOffs(69, 32).addBox(-3.25F, -1.5F, -1.5F, 6.5F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.7854F, 0.0F, 0.0F));

        PartDefinition rear_t6 = partdefinition.addOrReplaceChild("rear_t6", CubeListBuilder.create().texOffs(8, 0).mirror().addBox(0.0F, -4.0355F, -4.0355F, 0.0F, 8.0711F, 8.0711F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(22, 70).mirror().addBox(-2.002F, -2.5025F, -6.0416F, 4.004F, 5.005F, 2.002F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(66, 48).addBox(-2.002F, 4.0396F, -2.5025F, 4.004F, 2.002F, 5.005F, new CubeDeformation(0.0F))
                .texOffs(34, 0).addBox(-2.002F, -2.5025F, 4.0396F, 4.004F, 5.005F, 2.002F, new CubeDeformation(0.0F))
                .texOffs(34, 60).mirror().addBox(-2.002F, -6.0416F, -2.5025F, 4.004F, 2.002F, 5.005F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(22.0F, 17.9645F, 92.25F));

        PartDefinition cube_r17 = rear_t6.addOrReplaceChild("cube_r17", CubeListBuilder.create().texOffs(29, 53).mirror().addBox(-2.0F, -2.5F, -6.0355F, 4.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(66, 21).addBox(-2.0F, -6.0355F, -2.5F, 4.0F, 2.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(51, 70).addBox(-2.0F, -2.5F, 4.0355F, 4.0F, 5.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(60, 61).mirror().addBox(-2.0F, 4.0355F, -2.5F, 4.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.7854F, 0.0F, 0.0F));

        PartDefinition cube_r18 = rear_t6.addOrReplaceChild("cube_r18", CubeListBuilder.create().texOffs(69, 32).addBox(-3.25F, -1.5F, -1.5F, 6.5F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.7854F, 0.0F, 0.0F));

        PartDefinition bb_main = partdefinition.addOrReplaceChild("bb_main", CubeListBuilder.create().texOffs(-21, 1).addBox(-23.0F, -22.5F, 100.0F, 46.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition cube_r19 = bb_main.addOrReplaceChild("cube_r19", CubeListBuilder.create().texOffs(-50, 1).addBox(-52.0F, -8.0F, -1.0F, 75.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(22.0F, -14.5F, 77.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition cube_r20 = bb_main.addOrReplaceChild("cube_r20", CubeListBuilder.create().texOffs(-50, 1).addBox(-52.0F, -8.0F, -1.0F, 75.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-23.0F, -14.5F, 77.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition cube_r21 = bb_main.addOrReplaceChild("cube_r21", CubeListBuilder.create().texOffs(52, 32).addBox(-20.25F, -1.5F, -1.5F, 38.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, -6.0355F, 34.25F, 0.7854F, 0.0F, 0.0F));

        PartDefinition cube_r22 = bb_main.addOrReplaceChild("cube_r22", CubeListBuilder.create().texOffs(52, 32).addBox(-20.25F, -1.5F, -1.5F, 38.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, -6.0355F, 92.25F, 0.7854F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    /**
     * 设置动画状态。
     * <p>
     * 载具为静态模型，没有四肢与头部动作，故不设置姿态。
     *
     * @param entity 流动小厨实体
     * @param limbSwing 行走摆动相位
     * @param limbSwingAmount 行走摆动幅度
     * @param ageInTicks 存在刻数
     * @param netHeadYaw 头部偏航角
     * @param headPitch 头部俯仰角
     */
    @Override
    public void setupAnim(RollingKitchenEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
    }

    /**
     * 渲染模型。
     * <p>
     * 依次渲染车身、把手、轮胎与底盘，木箱随车身一并渲染。
     *
     * @param poseStack 姿态栈
     * @param vertexConsumer 顶点消费者
     * @param packedLight 打包光照
     * @param packedOverlay 打包覆绘
     * @param color 颜色
     */
    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        body.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        handle.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        front_t.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        rear_t2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        rear_t.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        rear_t3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        rear_t4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        rear_t5.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        rear_t6.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        bb_main.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
