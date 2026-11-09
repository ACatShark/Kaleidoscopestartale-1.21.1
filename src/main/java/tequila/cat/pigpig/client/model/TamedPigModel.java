package tequila.cat.pigpig.client.model;

import net.minecraft.client.model.PigModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

import net.minecraft.world.entity.animal.Pig;

/** 已驯养的猪：戴草帽的版本。 */
public class TamedPigModel extends PigModel<Pig> {
    public TamedPigModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0F, 13.0F, 9.0F));
        body.addOrReplaceChild("body_r1",
                CubeListBuilder.create().texOffs(28, 8).addBox(-5.0F, -18.0F, -5.0F, 10.0F, 16.0F, 8.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, (float) Math.PI / 2F, 0.0F, 0.0F));

        PartDefinition head = root.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.0F, -4.0F, -8.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
                        .texOffs(16, 16).addBox(-2.0F, 0.0F, -9.0F, 4.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 12.0F, -7.0F));

        root.addOrReplaceChild("right_hind_leg",
                CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-3.0F, 18.0F, 6.0F));
        root.addOrReplaceChild("left_hind_leg",
                CubeListBuilder.create().texOffs(0, 16).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false),
                PartPose.offset(3.0F, 18.0F, 6.0F));
        root.addOrReplaceChild("right_front_leg",
                CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-3.0F, 18.0F, -6.0F));
        root.addOrReplaceChild("left_front_leg",
                CubeListBuilder.create().texOffs(0, 16).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false),
                PartPose.offset(3.0F, 18.0F, -6.0F));

        PartDefinition body2 = head.addOrReplaceChild("body2", CubeListBuilder.create(),
                PartPose.offsetAndRotation(1.0F, 2.0F, -4.0F, 0.0F, 0.0F, -0.2182F));
        PartDefinition head2 = body2.addOrReplaceChild("head2", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
        head2.addOrReplaceChild("helmet",
                CubeListBuilder.create().texOffs(0, 26).addBox(-4.0F, -10.0F, -4.0F, 8.0F, 5.0F, 8.0F, new CubeDeformation(0.5F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        head2.addOrReplaceChild("brim",
                CubeListBuilder.create().texOffs(30, 47).addBox(-8.0F, -8.0F, -6.0F, 16.0F, 16.0F, 1.0F, new CubeDeformation(0.1F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -1.5708F, 0.0F, 0.0F));

        return LayerDefinition.create(mesh, 64, 64);
    }
}
