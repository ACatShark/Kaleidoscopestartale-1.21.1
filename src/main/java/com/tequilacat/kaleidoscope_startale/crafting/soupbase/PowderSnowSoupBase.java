package com.tequilacat.kaleidoscope_startale.crafting.soupbase;

import com.github.ysbbbbbb.kaleidoscopecookery.api.client.render.ISoupBaseRender;
import com.github.ysbbbbbb.kaleidoscopecookery.api.recipe.soupbase.ISoupBase;
import com.tequilacat.kaleidoscope_startale.KaleidoscopeStartale;
import com.tequilacat.kaleidoscope_startale.client.renderer.powder.PowderSnowSoupBaseRender;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * 细雪汤底。
 * <p>
 * 实现汤底接口，以细雪桶作为汤底物品、以空桶作为回收容器，
 * 用于冷炖等配方的汤底匹配与容器回收。
 */
public class PowderSnowSoupBase implements ISoupBase {

    /** 细雪汤底 */
    public static final PowderSnowSoupBase INSTANCE = new PowderSnowSoupBase();

    /**
     * 私有构造函数，防止外部实例化。
     */
    private PowderSnowSoupBase() {}

    /**
     * 获取汤底标识符。
     *
     * @return 汤底标识符
     */
    @Override
    public ResourceLocation getName() {
        return KaleidoscopeStartale.id("powder_snow");
    }

    /**
     * 获取气泡颜色。
     *
     * @return 气泡颜色（白色）
     */
    @Override
    public int getBubbleColor() {
        return 0xFFFFFF;
    }

    /**
     * 获取用于展示的汤底物品堆。
     *
     * @return 细雪桶物品堆
     */
    @Override
    public ItemStack getDisplayStack() {
        return new ItemStack(Items.POWDER_SNOW_BUCKET);
    }

    /**
     * 获取汤底渲染器，返回细雪专用渲染器。
     */
    @Override
    @OnlyIn(Dist.CLIENT)
    public ISoupBaseRender getRender() {
        return PowderSnowSoupBaseRender.INSTANCE;
    }

    /**
     * 判断物品是否为该汤底。
     *
     * @param stack 待判断的物品堆
     * @return 是细雪桶返回 true
     */
    @Override
    public boolean isSoupBase(ItemStack stack) {
        return stack.is(Items.POWDER_SNOW_BUCKET);
    }

    /**
     * 判断物品是否为该汤底对应的容器。
     *
     * @param stack 待判断的物品堆
     * @return 是空桶返回 true
     */
    @Override
    public boolean isContainer(ItemStack stack) {
        return stack.is(Items.BUCKET);
    }

    /**
     * 获取使用汤底后返还的容器。
     *
     * @param level 世界实例
     * @param user  使用汤底的实体
     * @param used  被使用的物品堆
     * @return 空桶物品堆
     */
    @Override
    public ItemStack getReturnContainer(Level level, LivingEntity user, ItemStack used) {
        return new ItemStack(Items.BUCKET);
    }

    /**
     * 获取使用容器后返还的汤底。
     *
     * @param level        世界实例
     * @param user         使用容器的实体
     * @param usedContainer 被使用的容器物品堆
     * @return 细雪桶物品堆
     */
    @Override
    public ItemStack getReturnSoupBase(Level level, LivingEntity user, ItemStack usedContainer) {
        return new ItemStack(Items.POWDER_SNOW_BUCKET);
    }
}