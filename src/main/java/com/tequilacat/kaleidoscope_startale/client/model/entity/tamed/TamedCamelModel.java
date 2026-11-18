package com.tequilacat.kaleidoscope_startale.client.model.entity.tamed;

import net.minecraft.client.model.CamelModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.animal.camel.Camel;

/**
 * 已驯养骆驼模型。
 * <p>
 * 几何与原版骆驼模型完全一致（部件名、枢轴、体积均照抄原版），
 * 只在头部附加草帽（帽冠与帽檐）。
 * 这样原版的行走、坐下、起身、冲刺等动画在本模型上与原版表现完全相同，
 * 不会出现腿部穿模。
 */
public class TamedCamelModel extends CamelModel<Camel> {

    /**
     * 构造已驯养骆驼模型。
     *
     * @param root 根模型部件
     */
    public TamedCamelModel(ModelPart root) {
        super(root);
    }

    /**
     * 构建骆驼的身体层定义。
     * <p>
     * 原版几何逐项复制，另在头部追加由 body2/head2 嵌套的帽冠与帽檐，
     * 帽子挂在头部之下，因此会跟随头部一起转动。
     *
     * @return 层定义
     */
    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        CubeDeformation overlay = new CubeDeformation(0.05F);

        PartDefinition body = root.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 25).addBox(-7.5F, -12.0F, -23.5F, 15.0F, 12.0F, 27.0F),
                PartPose.offset(0.0F, 4.0F, 9.5F));

        body.addOrReplaceChild("hump",
                CubeListBuilder.create().texOffs(74, 0).addBox(-4.5F, -5.0F, -5.5F, 9.0F, 5.0F, 11.0F),
                PartPose.offset(0.0F, -12.0F, -10.0F));
        body.addOrReplaceChild("tail",
                CubeListBuilder.create().texOffs(122, 0).addBox(-1.5F, 0.0F, 0.0F, 3.0F, 14.0F, 0.0F),
                PartPose.offsetAndRotation(0.0F, -9.0F, 3.5F, 0.0873F, 0.0F, 0.0F));

        PartDefinition head = body.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(60, 24).addBox(-3.5F, -7.0F, -15.0F, 7.0F, 8.0F, 19.0F)
                        .texOffs(21, 0).addBox(-3.5F, -21.0F, -15.0F, 7.0F, 14.0F, 7.0F)
                        .texOffs(50, 0).addBox(-2.5F, -21.0F, -21.0F, 5.0F, 5.0F, 6.0F),
                PartPose.offset(0.0F, -3.0F, -19.5F));

        head.addOrReplaceChild("left_ear",
                CubeListBuilder.create().texOffs(45, 0).addBox(-0.5F, 0.5F, -1.0F, 3.0F, 1.0F, 2.0F),
                PartPose.offset(2.5F, -21.0F, -9.5F));
        head.addOrReplaceChild("right_ear",
                CubeListBuilder.create().texOffs(67, 0).addBox(-2.5F, 0.5F, -1.0F, 3.0F, 1.0F, 2.0F),
                PartPose.offset(-2.5F, -21.0F, -9.5F));

        root.addOrReplaceChild("left_hind_leg",
                CubeListBuilder.create().texOffs(58, 16).addBox(-2.5F, 2.0F, -2.5F, 5.0F, 21.0F, 5.0F),
                PartPose.offset(4.9F, 1.0F, 9.5F));
        root.addOrReplaceChild("right_hind_leg",
                CubeListBuilder.create().texOffs(94, 16).addBox(-2.5F, 2.0F, -2.5F, 5.0F, 21.0F, 5.0F),
                PartPose.offset(-4.9F, 1.0F, 9.5F));
        root.addOrReplaceChild("left_front_leg",
                CubeListBuilder.create().texOffs(0, 0).addBox(-2.5F, 2.0F, -2.5F, 5.0F, 21.0F, 5.0F),
                PartPose.offset(4.9F, 1.0F, -10.5F));
        root.addOrReplaceChild("right_front_leg",
                CubeListBuilder.create().texOffs(0, 26).addBox(-2.5F, 2.0F, -2.5F, 5.0F, 21.0F, 5.0F),
                PartPose.offset(-4.9F, 1.0F, -10.5F));

        body.addOrReplaceChild("saddle",
                CubeListBuilder.create()
                        .texOffs(74, 64).addBox(-4.5F, -17.0F, -15.5F, 9.0F, 5.0F, 11.0F, overlay)
                        .texOffs(92, 114).addBox(-3.5F, -20.0F, -15.5F, 7.0F, 3.0F, 11.0F, overlay)
                        .texOffs(0, 89).addBox(-7.5F, -12.0F, -23.5F, 15.0F, 12.0F, 27.0F, overlay),
                PartPose.ZERO);
        head.addOrReplaceChild("reins",
                CubeListBuilder.create()
                        .texOffs(98, 42).addBox(3.51F, -18.0F, -17.0F, 0.0F, 7.0F, 15.0F)
                        .texOffs(84, 57).addBox(-3.5F, -18.0F, -2.0F, 7.0F, 7.0F, 0.0F)
                        .texOffs(98, 42).addBox(-3.51F, -18.0F, -17.0F, 0.0F, 7.0F, 15.0F),
                PartPose.ZERO);
        head.addOrReplaceChild("bridle",
                CubeListBuilder.create()
                        .texOffs(60, 87).addBox(-3.5F, -7.0F, -15.0F, 7.0F, 8.0F, 19.0F, overlay)
                        .texOffs(21, 64).addBox(-3.5F, -21.0F, -15.0F, 7.0F, 14.0F, 7.0F, overlay)
                        .texOffs(50, 64).addBox(-2.5F, -21.0F, -21.0F, 5.0F, 5.0F, 6.0F, overlay)
                        .texOffs(74, 70).addBox(2.5F, -19.0F, -18.0F, 1.0F, 2.0F, 2.0F)
                        .texOffs(74, 70).mirror().addBox(-3.5F, -19.0F, -18.0F, 1.0F, 2.0F, 2.0F),
                PartPose.ZERO);

        // 草帽：与原版头部同坐标系的偏移，child 链 body2 -> head2 -> helmet/brim
        PartDefinition body2 = head.addOrReplaceChild("body2", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0.0F, -14.0F, -11.0F, -0.2618F, 0.0F, 0.0F));
        PartDefinition head2 = body2.addOrReplaceChild("head2", CubeListBuilder.create(), PartPose.ZERO);
        head2.addOrReplaceChild("helmet",
                CubeListBuilder.create().texOffs(66, 86).addBox(-4.0F, -10.0F, -4.0F, 8.0F, 5.0F, 8.0F, new CubeDeformation(0.5F)),
                PartPose.ZERO);
        head2.addOrReplaceChild("brim",
                CubeListBuilder.create().texOffs(31, 84).addBox(-8.0F, -8.0F, -6.0F, 16.0F, 16.0F, 1.0F, new CubeDeformation(0.1F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -1.5708F, 0.0F, 0.0F));

        return LayerDefinition.create(mesh, 128, 128);
    }
}
