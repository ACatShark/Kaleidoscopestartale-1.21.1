package tequila.cat.pigpig.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ChickenModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Chicken;

import tequila.cat.pigpig.KaleidoscopeStartale;
import tequila.cat.pigpig.client.model.ModModelLayers;
import tequila.cat.pigpig.client.model.SwitchableChickenModel;
import tequila.cat.pigpig.data.MobTaming;

/** 已驯养的鸡换成戴帽子的贴图与模型。 */
public class TamedChickenRenderer extends MobRenderer<Chicken, SwitchableChickenModel> {
    private static final ResourceLocation VANILLA_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/chicken.png");
    private static final ResourceLocation TAMED_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            KaleidoscopeStartale.MODID, "textures/entity/tamed_chicken.png");
    private static Boolean tamedTextureAvailable;

    public TamedChickenRenderer(EntityRendererProvider.Context context) {
        super(context, new SwitchableChickenModel(
                context.bakeLayer(ModelLayers.CHICKEN),
                context.bakeLayer(ModModelLayers.TAMED_CHICKEN)), 0.3F);
    }

    @Override
    public void render(Chicken entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        ((SwitchableChickenModel) this.model).setTamed(MobTaming.isTamed(entity));
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(Chicken entity) {
        return MobTaming.isTamed(entity) && tamedTextureExists() ? TAMED_TEXTURE : VANILLA_TEXTURE;
    }

    @Override
    protected float getBob(Chicken livingBase, float partialTicks) {
        float flap = Mth.lerp(partialTicks, livingBase.oFlap, livingBase.flap);
        float speed = Mth.lerp(partialTicks, livingBase.oFlapSpeed, livingBase.flapSpeed);
        return (Mth.sin(flap) + 1.0F) * speed;
    }

    private static boolean tamedTextureExists() {
        tamedTextureAvailable = tamedTextureAvailable == null
                ? Minecraft.getInstance().getResourceManager().getResource(TAMED_TEXTURE).isPresent()
                : tamedTextureAvailable;
        return tamedTextureAvailable;
    }
}
