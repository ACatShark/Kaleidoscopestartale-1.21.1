package tequila.cat.pigpig.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PigModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Pig;

import tequila.cat.pigpig.KaleidoscopeStartale;
import tequila.cat.pigpig.client.model.ModModelLayers;
import tequila.cat.pigpig.client.model.SwitchablePigModel;
import tequila.cat.pigpig.data.MobTaming;

/** 已驯养的猪换成戴草帽的贴图与模型；没装资源包时自动回落到原版外观。 */
public class TamedPigRenderer extends MobRenderer<Pig, SwitchablePigModel> {
    private static final ResourceLocation VANILLA_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/pig/pig.png");
    private static final ResourceLocation TAMED_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            KaleidoscopeStartale.MODID, "textures/entity/tamed_pig.png");
    private static Boolean tamedTextureAvailable;

    public TamedPigRenderer(EntityRendererProvider.Context context) {
        super(context, new SwitchablePigModel(
                context.bakeLayer(ModelLayers.PIG),
                context.bakeLayer(ModModelLayers.TAMED_PIG)), 0.7F);
        this.addLayer(new TamedPigSaddleLayer(this,
                new PigModel<>(context.bakeLayer(ModelLayers.PIG_SADDLE)),
                ResourceLocation.withDefaultNamespace("textures/entity/pig/pig_saddle.png")));
    }

    @Override
    public void render(Pig entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        ((SwitchablePigModel) this.model).setTamed(MobTaming.isTamed(entity));
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(Pig entity) {
        return MobTaming.isTamed(entity) && tamedTextureExists() ? TAMED_TEXTURE : VANILLA_TEXTURE;
    }

    private static boolean tamedTextureExists() {
        if (tamedTextureAvailable == null) {
            tamedTextureAvailable = Minecraft.getInstance().getResourceManager().getResource(TAMED_TEXTURE).isPresent();
        }
        return tamedTextureAvailable;
    }
}
