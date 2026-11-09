package tequila.cat.pigpig.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.armadillo.Armadillo;

import tequila.cat.pigpig.KaleidoscopeStartale;
import tequila.cat.pigpig.client.model.ModModelLayers;
import tequila.cat.pigpig.client.model.SwitchableArmadilloModel;
import tequila.cat.pigpig.data.MobTaming;

/** 已驯养的犰狳换成戴草帽的贴图与模型；缺贴图时回落到原版外观。 */
public class TamedArmadilloRenderer extends MobRenderer<Armadillo, SwitchableArmadilloModel> {
    private static final ResourceLocation VANILLA_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/armadillo.png");
    private static final ResourceLocation TAMED_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            KaleidoscopeStartale.MODID, "textures/entity/tamed_armadillo.png");
    private static Boolean tamedTextureAvailable;

    public TamedArmadilloRenderer(EntityRendererProvider.Context context) {
        super(context, new SwitchableArmadilloModel(
                context.bakeLayer(ModelLayers.ARMADILLO),
                context.bakeLayer(ModModelLayers.TAMED_ARMADILLO)), 0.4F);
    }

    @Override
    public void render(Armadillo entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        this.model.setTamed(MobTaming.isTamed(entity) && tamedTextureExists());
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(Armadillo entity) {
        return MobTaming.isTamed(entity) && tamedTextureExists() ? TAMED_TEXTURE : VANILLA_TEXTURE;
    }

    private static boolean tamedTextureExists() {
        if (tamedTextureAvailable == null) {
            tamedTextureAvailable = Minecraft.getInstance().getResourceManager().getResource(TAMED_TEXTURE).isPresent();
        }
        return tamedTextureAvailable;
    }
}
