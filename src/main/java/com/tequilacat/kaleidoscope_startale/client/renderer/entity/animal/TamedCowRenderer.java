package com.tequilacat.kaleidoscope_startale.client.renderer.entity.animal;

import com.mojang.blaze3d.vertex.PoseStack;
import com.tequilacat.kaleidoscope_startale.KaleidoscopeStartale;
import com.tequilacat.kaleidoscope_startale.client.model.ModModelLayers;
import com.tequilacat.kaleidoscope_startale.client.model.entity.switchable.SwitchableCowModel;
import com.tequilacat.kaleidoscope_startale.data.tame.MobTaming;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Cow;

/**
 * 已驯养牛渲染器。
 * <p>
 * 根据牛是否已驯养切换模型与贴图：
 * 已驯养且自定义贴图存在时使用驯养模型与贴图，否则回退到原版。
 * 贴图是否存在只检测一次并缓存。
 */
public class TamedCowRenderer extends MobRenderer<Cow, SwitchableCowModel> {

    /**
     * 原版牛贴图。
     */
    private static final ResourceLocation VANILLA_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/cow/cow.png");

    /**
     * 已驯养牛贴图。
     */
    private static final ResourceLocation TAMED_TEXTURE = KaleidoscopeStartale.id("textures/entity/tamed_cow.png");

    /** 已驯养贴图是否存在，null 表示尚未检测。 */
    private static Boolean tamedTextureAvailable;

    /**
     * 构造已驯养牛渲染器。
     * <p>
     * 使用可在原版与驯养模型之间切换的模型，并设置阴影半径。
     *
     * @param context 渲染器上下文
     */
    public TamedCowRenderer(EntityRendererProvider.Context context) {
        super(context, new SwitchableCowModel(
                context.bakeLayer(ModelLayers.COW),
                context.bakeLayer(ModModelLayers.TAMED_COW)), 0.7F);
    }

    /**
     * 渲染牛。
     * <p>
     * 渲染前根据驯养状态与贴图可用性设置模型是否使用驯养形态。
     *
     * @param entity      牛实体
     * @param entityYaw   实体偏航角
     * @param partialTicks 插值刻
     * @param poseStack   姿态栈
     * @param buffer      缓冲区
     * @param packedLight 打包光照
     */
    @Override
    public void render(Cow entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        this.model.setTamed(MobTaming.isTamed(entity) && tamedTextureExists());
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    /**
     * 获取牛贴图。
     * <p>
     * 已驯养且自定义贴图存在时返回驯养贴图，否则返回原版贴图。
     *
     * @param entity 牛实体
     * @return 贴图位置
     */
    @Override
    public ResourceLocation getTextureLocation(Cow entity) {
        return MobTaming.isTamed(entity) && tamedTextureExists() ? TAMED_TEXTURE : VANILLA_TEXTURE;
    }

    /**
     * 判断已驯养贴图是否存在。
     * <p>
     * 首次调用时检测并缓存结果。
     *
     * @return 已驯养贴图是否存在
     */
    private static boolean tamedTextureExists() {
        if (tamedTextureAvailable == null) {
            tamedTextureAvailable = Minecraft.getInstance().getResourceManager().getResource(TAMED_TEXTURE).isPresent();
        }
        return tamedTextureAvailable;
    }
}