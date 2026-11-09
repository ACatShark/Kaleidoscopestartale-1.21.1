package tequila.cat.pigpig.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Cow;

import tequila.cat.pigpig.KaleidoscopeStartale;
import tequila.cat.pigpig.client.model.ModModelLayers;
import tequila.cat.pigpig.client.model.SwitchableCowModel;
import tequila.cat.pigpig.data.MobTaming;

/** 已驯养的牛换成戴草帽的贴图与模型；缺贴图时回落到原版外观。 */
public class TamedCowRenderer extends MobRenderer<Cow, SwitchableCowModel> {
    private static final ResourceLocation VANILLA_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/cow/cow.png");
    private static final ResourceLocation TAMED_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            KaleidoscopeStartale.MODID, "textures/entity/tamed_cow.png");
    private static Boolean tamedTextureAvailable;

    public TamedCowRenderer(EntityRendererProvider.Context context) {
        super(context, new SwitchableCowModel(
                context.bakeLayer(ModelLayers.COW),
                context.bakeLayer(ModModelLayers.TAMED_COW)), 0.7F);
    }

    @Override
    public void render(Cow entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        this.model.setTamed(MobTaming.isTamed(entity) && tamedTextureExists());
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(Cow entity) {
        return MobTaming.isTamed(entity) && tamedTextureExists() ? TAMED_TEXTURE : VANILLA_TEXTURE;
    }

    private static boolean tamedTextureExists() {
        if (tamedTextureAvailable == null) {
            tamedTextureAvailable = Minecraft.getInstance().getResourceManager().getResource(TAMED_TEXTURE).isPresent();
        }
        return tamedTextureAvailable;
    }
}
