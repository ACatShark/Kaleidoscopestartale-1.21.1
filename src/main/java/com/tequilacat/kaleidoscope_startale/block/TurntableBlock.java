package com.tequilacat.kaleidoscope_startale.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 转台方块。
 * <p>
 * 由底座、中心轴与顶部圆盘组成的三段碰撞箱装饰方块：
 * 石质音效、需要正确工具采集，不遮挡且不阻碍视线，
 * 并允许天光向下传播。
 */
public class TurntableBlock extends Block {

    /** 转盘的组合碰撞箱。 */
    private static final VoxelShape SHAPE = Shapes.or(
            box(3.0D, 0.0D, 3.0D, 13.0D, 1.0D, 13.0D),
            box(6.0D, 1.0D, 6.0D, 10.0D, 4.0D, 10.0D),
            box(0.0D, 4.0D, 0.0D, 16.0D, 5.0D, 16.0D)
    );

    /**
     * 构造转台方块。
     */
    public TurntableBlock() {
        super(BlockBehaviour.Properties.of()
                .strength(1.0F, 3.0F)
                .mapColor(MapColor.STONE)
                .sound(SoundType.STONE)
                .requiresCorrectToolForDrops()
                .noOcclusion()
                .isSuffocating((state, level, pos) -> false)
                .isViewBlocking((state, level, pos) -> false));
    }

    /**
     * 获取方块碰撞箱。
     *
     * @param state   方块状态
     * @param level   方块获取器
     * @param pos     方块位置
     * @param context 碰撞上下文
     * @return 碰撞箱
     */
    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    /**
     * 判断是否允许天光向下传播。
     *
     * @param state 方块状态
     * @param level 方块获取器
     * @param pos   方块位置
     * @return 恒为 true
     */
    @Override
    protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    /**
     * 判断光照遮挡是否使用形状。
     *
     * @param state 方块状态
     * @return 恒为 true
     */
    @Override
    protected boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }
}