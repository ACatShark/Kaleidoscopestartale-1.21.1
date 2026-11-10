package com.tequilacat.kaleidoscope_startale.client.renderer.powder;

import com.github.ysbbbbbb.kaleidoscopecookery.api.client.render.ISoupBaseRender;
import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.kitchen.StockpotBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * 细雪汤底渲染器。
 * <p>
 * 在汤锅的放入原料、烹饪与完成三个阶段，
 * 以细雪方块材质绘制汤底表面，颜色为白色。
 */
@OnlyIn(Dist.CLIENT)
public class PowderSnowSoupBaseRender implements ISoupBaseRender {

    /** 单例实例 */
    public static final PowderSnowSoupBaseRender INSTANCE = new PowderSnowSoupBaseRender();

    /** 细雪方块的材质位置 */
    private static final ResourceLocation POWDER_SNOW_SPRITE =
            ResourceLocation.fromNamespaceAndPath("minecraft", "block/powder_snow");

    /** 完全不透明白色 */
    private static final int COLOR_WHITE_OPAQUE = -1;
    /** Y 轴下降偏移量 */
    private static final float Y_OFFSET = 0.05F;

    /**
     * 私有构造函数，防止外部实例化。
     */
    private PowderSnowSoupBaseRender() {}

    /**
     * 获取细雪方块的材质精灵。
     *
     * @return 细雪方块材质精灵
     */
    private TextureAtlasSprite getSnowSprite() {
        return Minecraft.getInstance()
                .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(POWDER_SNOW_SPRITE);
    }

    /**
     * 渲染放入原料阶段的汤底表面。
     * <p>
     * 以细雪材质与完全不透明白色在传入的汤底高度处绘制表面。
     *
     * @param entity      汤锅方块实体
     * @param partialTick 部分 tick 插值
     * @param poseStack   姿态栈
     * @param buffer      多重缓冲区来源
     * @param light       光照值
     * @param overlay     覆盖层
     * @param soupHeight  汤底表面高度
     */
    @Override
    public void renderWhenPutIngredient(StockpotBlockEntity entity, float partialTick,
                                        PoseStack poseStack, MultiBufferSource buffer,
                                        int light, int overlay, float soupHeight) {
        ISoupBaseRender.renderSurface(getSnowSprite(), COLOR_WHITE_OPAQUE, poseStack, buffer, light, soupHeight - Y_OFFSET);
    }

    /**
     * 渲染烹饪阶段的汤底表面。
     * <p>
     * 以细雪材质与完全不透明白色在传入的汤底高度处绘制表面。
     *
     * @param entity      汤锅方块实体
     * @param partialTick 部分 tick 插值
     * @param poseStack   姿态栈
     * @param buffer      多重缓冲区来源
     * @param light       光照值
     * @param overlay     覆盖层
     * @param recipeId    配方标识符
     * @param soupHeight  汤底表面高度
     */
    @Override
    public void renderWhenCooking(StockpotBlockEntity entity, float partialTick,
                                  PoseStack poseStack, MultiBufferSource buffer,
                                  int light, int overlay, ResourceLocation recipeId, float soupHeight) {
        ISoupBaseRender.renderSurface(getSnowSprite(), COLOR_WHITE_OPAQUE, poseStack, buffer, light, soupHeight - Y_OFFSET);
    }

    /**
     * 渲染完成阶段的汤底表面。
     * <p>
     * 以细雪材质与完全不透明白色在传入的汤底高度处绘制表面。
     *
     * @param entity      汤锅方块实体
     * @param partialTick 部分 tick 插值
     * @param poseStack   姿态栈
     * @param buffer      多重缓冲区来源
     * @param light       光照值
     * @param overlay     覆盖层
     * @param recipeId    配方标识符
     * @param soupHeight  汤底表面高度
     */
    @Override
    public void renderWhenFinished(StockpotBlockEntity entity, float partialTick,
                                   PoseStack poseStack, MultiBufferSource buffer,
                                   int light, int overlay, ResourceLocation recipeId, float soupHeight) {
        ISoupBaseRender.renderSurface(getSnowSprite(), COLOR_WHITE_OPAQUE, poseStack, buffer, light, soupHeight - Y_OFFSET);
    }
}