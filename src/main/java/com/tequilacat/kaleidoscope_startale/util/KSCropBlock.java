package com.tequilacat.kaleidoscope_startale.util;

import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * 模组作物方块。
 * <p>
 * 继承原版作物方块，允许自定义最大生长阶段与基础种子物品，
 * 便于以不同参数复用同一套作物逻辑。
 */
public class KSCropBlock extends CropBlock {

    /** 最大生长阶段。 */
    private final int maxAge;

    /** 基础种子物品。 */
    private final ItemLike seed;

    /**
     * 构造模组作物方块。
     *
     * @param maxAge     最大生长阶段
     * @param seed       基础种子物品
     * @param properties 方块属性
     */
    public KSCropBlock(int maxAge, ItemLike seed, BlockBehaviour.Properties properties) {
        super(properties);
        this.maxAge = maxAge;
        this.seed = seed;
    }

    /**
     * 获取最大生长阶段。
     *
     * @return 最大生长阶段
     */
    @Override
    public int getMaxAge() {
        return this.maxAge;
    }

    /**
     * 获取基础种子物品。
     *
     * @return 基础种子物品
     */
    @Override
    protected ItemLike getBaseSeedId() {
        return this.seed;
    }
}