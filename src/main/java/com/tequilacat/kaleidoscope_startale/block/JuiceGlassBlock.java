package com.tequilacat.kaleidoscope_startale.block;

import com.github.ysbbbbbb.kaleidoscopetavern.block.mixology.CocktailBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 果汁杯方块。
 * <p>
 * 继承酒馆模组的鸡尾酒方块，使用较小的杯状碰撞箱，
 * 并提供默认与自定义属性两种构造方式。
 */
public class JuiceGlassBlock extends CocktailBlock {

    /** 杯状碰撞箱。 */
    private static final VoxelShape SHAPE =
            Shapes.box(5.0D / 16.0D, 0.0D, 5.0D / 16.0D, 11.0D / 16.0D, 12.0D / 16.0D, 11.0D / 16.0D);

    /**
     * 使用默认属性构造果汁杯方块。
     */
    public JuiceGlassBlock() {
        super();
    }

    /**
     * 使用自定义属性构造果汁杯方块。
     *
     * @param properties 方块属性
     */
    public JuiceGlassBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    /**
     * 获取方块碰撞箱。
     *
     * @param state   方块状态
     * @param level   方块获取器
     * @param pos     方块位置
     * @param context 碰撞上下文
     * @return 杯状碰撞箱
     */
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
}