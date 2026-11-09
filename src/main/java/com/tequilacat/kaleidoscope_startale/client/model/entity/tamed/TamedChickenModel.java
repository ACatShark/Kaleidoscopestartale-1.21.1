package com.tequilacat.kaleidoscope_startale.client.model.entity.tamed;

import net.minecraft.client.model.ChickenModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.animal.Chicken;

/**
 * 已驯养鸡模型。
 * <p>
 * 继承原版鸡模型，并在头部附加帽子模型，
 * 通过静态方法构建包含头、喙、肉垂、身体、腿、翅膀与帽子的网格定义。
 */
public class TamedChickenModel extends ChickenModel<Chicken> {

    /**
     * 构造已驯养鸡模型。
     *
     * @param root 根模型部件
     */
    public TamedChickenModel(ModelPart root) {
        super(root);
    }

    /**
     * 构建鸡的身体层定义。
     * <p>
     * 定义头部、喙、肉垂、身体、双腿与双翼，
     * 并在头部附加帽子部件。
     *
     * @return 层定义
     */
    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition head = root.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -6.0F, -2.0F, 4.0F, 6.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 15.0F, -4.0F));
        root.addOrReplaceChild("beak",
                CubeListBuilder.create().texOffs(14, 0).addBox(-2.0F, -4.0F, -4.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 15.0F, -4.0F));
        root.addOrReplaceChild("red_thing",
                CubeListBuilder.create().texOffs(14, 4).addBox(-1.0F, -2.0F, -3.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 15.0F, -4.0F));
        root.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 9).addBox(-3.0F, -4.0F, -3.0F, 6.0F, 8.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 16.0F, 0.0F, (float) Math.PI / 2F, 0.0F, 0.0F));
        root.addOrReplaceChild("right_leg",
                CubeListBuilder.create().texOffs(26, 0).addBox(-1.0F, 0.0F, -3.0F, 3.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-2.0F, 19.0F, 1.0F));
        root.addOrReplaceChild("left_leg",
                CubeListBuilder.create().texOffs(26, 0).addBox(-1.0F, 0.0F, -3.0F, 3.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offset(1.0F, 19.0F, 1.0F));
        root.addOrReplaceChild("right_wing",
                CubeListBuilder.create().texOffs(24, 13).addBox(0.0F, 0.0F, -3.0F, 1.0F, 4.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-4.0F, 13.0F, 0.0F));
        root.addOrReplaceChild("left_wing",
                CubeListBuilder.create().texOffs(24, 13).addBox(-1.0F, 0.0F, -3.0F, 1.0F, 4.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.offset(4.0F, 13.0F, 0.0F));

        head.addOrReplaceChild("cap",
                CubeListBuilder.create()
                        .texOffs(49, 22).addBox(-2.0F, -18.0F, -6.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
                        .texOffs(47, 20).addBox(-2.0F, -16.0F, -8.0F, 4.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 9.0F, 4.0F));

        return LayerDefinition.create(mesh, 64, 32);
    }
}