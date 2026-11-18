package com.tequilacat.kaleidoscope_startale.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import com.mojang.serialization.MapCodec;
import org.jetbrains.annotations.NotNull;

/**
 * 纸盒方块。
 * <p>
 * 一种带水平朝向的小型纸盒装饰方块：瞬间破坏、使用羊毛音效且不遮挡，
 * 碰撞箱为底座上方的小型盒体，并随朝向切换为对应的偏置形状。
 */
public class CartonBlock extends HorizontalDirectionalBlock {

    /** 方块编解码器。 */
    public static final MapCodec<CartonBlock> CODEC = simpleCodec(CartonBlock::new);

    /** 朝北时的碰撞箱。 */
    private static final VoxelShape SHAPE_NORTH = Shapes.box(
            6.0D / 16.0D, 0.0D, 6.0D / 16.0D,
            10.0D / 16.0D, 7.0D / 16.0D, 11.0D / 16.0D);

    /** 朝东时的碰撞箱。 */
    private static final VoxelShape SHAPE_EAST = Shapes.box(
            5.0D / 16.0D, 0.0D, 6.0D / 16.0D,
            10.0D / 16.0D, 7.0D / 16.0D, 10.0D / 16.0D);

    /** 朝南时的碰撞箱。 */
    private static final VoxelShape SHAPE_SOUTH = Shapes.box(
            6.0D / 16.0D, 0.0D, 5.0D / 16.0D,
            10.0D / 16.0D, 7.0D / 16.0D, 10.0D / 16.0D);

    /** 朝西时的碰撞箱。 */
    private static final VoxelShape SHAPE_WEST = Shapes.box(
            6.0D / 16.0D, 0.0D, 6.0D / 16.0D,
            11.0D / 16.0D, 7.0D / 16.0D, 10.0D / 16.0D);

    /**
     * 创建纸盒方块。
     * <p>
     * 无碰撞体积、瞬间破坏、声音按羊毛处理，并带水平朝向。
     */
    public CartonBlock() {
        this(BlockBehaviour.Properties.of()
                .instabreak()
                .sound(SoundType.WOOL)
                .noOcclusion());
    }

    /**
     * 创建纸盒方块。
     *
     * @param properties 方块属性
     */
    public CartonBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    /**
     * 获取方块编解码器。
     *
     * @return 方块编解码器
     */
    @Override
    protected @NotNull MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    /**
     * 创建方块状态定义。
     *
     * @param builder 状态定义构建器
     */
    @Override
    protected void createBlockStateDefinition(@NotNull StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    /**
     * 放置时朝向玩家所在的水平方向。
     *
     * @param context 放置上下文
     * @return 初始方块状态
     */
    @Override
    public BlockState getStateForPlacement(@NotNull BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    /**
     * 获取方块碰撞箱。
     * <p>
     * 根据朝向返回对应的偏置盒体形状。
     *
     * @param state   方块状态
     * @param level   方块获取器
     * @param pos     方块位置
     * @param context 碰撞上下文
     * @return 碰撞箱
     */
    @Override
    protected @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level,
                                           @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case EAST -> SHAPE_EAST;
            case SOUTH -> SHAPE_SOUTH;
            case WEST -> SHAPE_WEST;
            default -> SHAPE_NORTH;
        };
    }

    /**
     * 旋转方块状态。
     *
     * @param state    方块状态
     * @param rotation 旋转
     * @return 旋转后的方块状态
     */
    @Override
    protected @NotNull BlockState rotate(@NotNull BlockState state, @NotNull Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    /**
     * 镜像方块状态。
     *
     * @param state  方块状态
     * @param mirror 镜像
     * @return 镜像后的方块状态
     */
    @Override
    protected @NotNull BlockState mirror(@NotNull BlockState state, @NotNull Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }
}