package com.tequilacat.kaleidoscope_startale.client.renderer.entity.animal;

import com.mojang.blaze3d.vertex.PoseStack;
import com.tequilacat.kaleidoscope_startale.KaleidoscopeStartale;
import com.tequilacat.kaleidoscope_startale.client.model.ModModelLayers;
import com.tequilacat.kaleidoscope_startale.client.model.entity.switchable.SwitchableChickenModel;
import com.tequilacat.kaleidoscope_startale.data.tame.MobTaming;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Chicken;

/**
 * 已驯养鸡渲染器。
 * <p>
 * 根据鸡是否已驯养切换模型与贴图：
 * 已驯养时使用驯养模型，且自定义贴图存在时使用驯养贴图，否则回退到原版。
 * 贴图是否存在只检测一次并缓存，并覆写拍翅动画的抖动计算。
 */
public class TamedChickenRenderer extends MobRenderer<Chicken, SwitchableChickenModel> {

    /**
     * 原版鸡贴图。
     */
    private static final ResourceLocation VANILLA_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/chicken.png");

    /**
     * 已驯养鸡贴图。
     */
    private static final ResourceLocation TAMED_TEXTURE = KaleidoscopeStartale.id("textures/entity/tamed_chicken.png");

    /** 已驯养贴图是否存在，null 表示尚未检测。 */
    private static Boolean tamedTextureAvailable;

    /**
     * 构造已驯养鸡渲染器。
     * <p>
     * 使用可在原版与驯养模型之间切换的模型，并设置阴影半径。
     *
     * @param context 渲染器上下文
     */
    public TamedChickenRenderer(EntityRendererProvider.Context context) {
        super(context, new SwitchableChickenModel(
                context.bakeLayer(ModelLayers.CHICKEN),
                context.bakeLayer(ModModelLayers.TAMED_CHICKEN)), 0.3F);
    }

    /**
     * 渲染鸡。
     * <p>
     * 渲染前根据驯养状态设置模型是否使用驯养形态。
     *
     * @param entity      鸡实体
     * @param entityYaw   实体偏航角
     * @param partialTicks 插值刻
     * @param poseStack   姿态栈
     * @param buffer      缓冲区
     * @param packedLight 打包光照
     */
    @Override
    public void render(Chicken entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        ((SwitchableChickenModel) this.model).setTamed(MobTaming.isTamed(entity));
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    /**
     * 获取鸡贴图。
     * <p>
     * 已驯养且自定义贴图存在时返回驯养贴图，否则返回原版贴图。
     *
     * @param entity 鸡实体
     * @return 贴图位置
     */
    @Override
    public ResourceLocation getTextureLocation(Chicken entity) {
        return MobTaming.isTamed(entity) && tamedTextureExists() ? TAMED_TEXTURE : VANILLA_TEXTURE;
    }

    /**
     * 计算拍翅抖动幅度。
     * <p>
     * 对拍翅角度与速度做插值，返回正弦抖动值。
     *
     * @param livingBase 鸡实体
     * @param partialTicks 插值刻
     * @return 抖动幅度
     */
    @Override
    protected float getBob(Chicken livingBase, float partialTicks) {
        float flap = Mth.lerp(partialTicks, livingBase.oFlap, livingBase.flap);
        float speed = Mth.lerp(partialTicks, livingBase.oFlapSpeed, livingBase.flapSpeed);
        return (Mth.sin(flap) + 1.0F) * speed;
    }

    /**
     * 判断已驯养贴图是否存在。
     * <p>
     * 首次调用时检测并缓存结果。
     *
     * @return 已驯养贴图是否存在
     */
    private static boolean tamedTextureExists() {
        tamedTextureAvailable = tamedTextureAvailable == null
                ? Minecraft.getInstance().getResourceManager().getResource(TAMED_TEXTURE).isPresent()
                : tamedTextureAvailable;
        return tamedTextureAvailable;
    }
}