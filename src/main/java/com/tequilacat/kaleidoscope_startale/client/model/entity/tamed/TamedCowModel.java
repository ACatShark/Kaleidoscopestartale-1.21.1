package com.tequilacat.kaleidoscope_startale.client.model.entity.tamed;

import net.minecraft.client.model.CowModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.animal.Cow;

/**
 * 已驯养牛模型。
 * <p>
 * 继承原版牛模型，并在头部附加草帽模型（帽冠与帽檐），
 * 通过静态方法构建包含头部、身体、四肢与帽子的网格定义。
 */
public class TamedCowModel extends CowModel<Cow> {

    /**
     * 构造已驯养牛模型。
     *
     * @param root 根模型部件
     */
    public TamedCowModel(ModelPart root) {
        super(root);
    }

    /**
     * 构建牛的身体层定义。
     * <p>
     * 定义头部、身体与四肢，并在头部附加由 body2/head2 嵌套的
     * 帽冠与帽檐部件。
     *
     * @return 层定义
     */
    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition head = root.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.0F, -4.0F, -6.0F, 8.0F, 8.0F, 6.0F, new CubeDeformation(0.0F))
                        .texOffs(22, 0).addBox(4.0F, -3.0F, -5.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(22, 0).addBox(-5.0F, -3.0F, -5.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(1, 33).addBox(-3.0F, 1.0F, -7.0F, 6.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 4.0F, -8.0F));

        root.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(18, 4).addBox(-6.0F, -10.0F, -7.0F, 12.0F, 18.0F, 10.0F, new CubeDeformation(0.0F))
                        .texOffs(52, 0).addBox(-2.0F, 2.0F, -8.0F, 4.0F, 6.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 5.0F, 2.0F, (float) Math.PI / 2F, 0.0F, 0.0F));

        // 1.21 的四足模型部件名是 right_hind_leg 等，Blockbench 旧模板导出的 leg1~leg4 必须改名
        root.addOrReplaceChild("right_hind_leg",
                CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-4.0F, 12.0F, 7.0F));
        root.addOrReplaceChild("left_hind_leg",
                CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offset(4.0F, 12.0F, 7.0F));
        root.addOrReplaceChild("right_front_leg",
                CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -1.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-4.0F, 12.0F, -6.0F));
        root.addOrReplaceChild("left_front_leg",
                CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -1.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offset(4.0F, 12.0F, -6.0F));

        // Blockbench 导出时草帽挂在 root 上（不动），这里挂到 head 下（跟随头部转动）并换算偏移。
        PartDefinition body2 = head.addOrReplaceChild("body2", CubeListBuilder.create(), PartPose.offset(0.0F, 3.0F, -3.0F));
        PartDefinition head2 = body2.addOrReplaceChild("head2", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
        head2.addOrReplaceChild("helmet",
                CubeListBuilder.create().texOffs(0, 51).addBox(-4.0F, -10.0F, -4.0F, 8.0F, 5.0F, 8.0F, new CubeDeformation(0.5F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        head2.addOrReplaceChild("brim",
                CubeListBuilder.create().texOffs(30, 42).addBox(-8.0F, -8.0F, -6.0F, 16.0F, 16.0F, 1.0F, new CubeDeformation(0.1F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -1.5708F, 0.0F, 0.0F));

        return LayerDefinition.create(mesh, 64, 64);
    }
}