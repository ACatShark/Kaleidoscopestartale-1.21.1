package com.tequilacat.kaleidoscope_startale.block;

import com.github.ysbbbbbb.kaleidoscopetavern.block.mixology.CocktailBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 发光鸡尾酒方块。
 * <p>
 * 继承酒馆模组的鸡尾酒方块，按构造时指定的亮度发光。
 */
public class GlowingCocktailBlock extends CocktailBlock {

    /** 发光亮度。 */
    private final int lightLevel;

    /**
     * 构造发光鸡尾酒方块。
     *
     * @param lightLevel 发光亮度
     */
    public GlowingCocktailBlock(int lightLevel) {
        this.lightLevel = lightLevel;
    }

    /**
     * 获取方块发光亮度。
     *
     * @param state 方块状态
     * @param level 方块获取器
     * @param pos   方块位置
     * @return 发光亮度
     */
    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        return this.lightLevel;
    }
}