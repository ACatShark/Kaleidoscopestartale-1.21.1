package com.tequilacat.kaleidoscope_startale.block;

import com.mojang.serialization.MapCodec;
import com.tequilacat.kaleidoscope_startale.blockentity.JuicerBlockEntity;
import com.tequilacat.kaleidoscope_startale.init.KSBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.fluids.FluidUtil;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 榨汁机方块。
 * <p>
 * 拥有水平朝向与开合状态，绑定榨汁机方块实体并每刻更新。
 * 交互上区分点击盖子与瓶身：点击盖子可开关或潜行搬走，
 * 点击瓶身可投入物品、取出物品或用手持流体容器舀出果汁。
 */
public class JuicerBlock extends BaseEntityBlock {

    /** 方块编解码器。 */
    public static final MapCodec<JuicerBlock> CODEC = simpleCodec(JuicerBlock::new);

    /** 水平朝向属性。 */
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    /** 开合状态属性。 */
    public static final BooleanProperty OPEN = BooleanProperty.create("open");

    /** 方块碰撞箱。 */
    private static final VoxelShape SHAPE = Block.box(4.0D, 0.0D, 4.0D, 12.0D, 16.0D, 12.0D);

    /** 判定是否点击盖子的高度阈值（以格计）。 */
    private static final double LID_MIN_Y = 14.0D / 16.0D;

    /**
     * 构造榨汁机方块。
     * <p>
     * 默认朝向北方且处于合盖状态。
     *
     * @param properties 方块属性
     */
    public JuicerBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(OPEN, false));
    }

    /**
     * 创建榨汁机方块的属性。
     *
     * @return 方块属性
     */
    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.WOOD)
                .instrument(NoteBlockInstrument.GUITAR)
                .strength(1.0F)
                .sound(SoundType.WOOD)
                .noOcclusion()
                .ignitedByLava();
    }

    /**
     * 处理玩家对物品使用方块。
     * <p>
     * 点击盖子时开关或潜行搬走；点击瓶身时空手取物、
     * 手持流体容器舀出果汁，或开盖状态下投入物品。
     *
     * @param stack     手持物品堆叠
     * @param state     方块状态
     * @param level     世界
     * @param pos       方块位置
     * @param player    玩家
     * @param hand      交互手
     * @param hitResult 命中结果
     * @return 物品交互结果
     */
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof JuicerBlockEntity juicer)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        boolean open = state.getValue(OPEN);
        boolean onLid = hitResult.getLocation().y - pos.getY() >= LID_MIN_Y;

        // 点在盖子上：普通右键开关盖，潜行右键整体搬走
        if (onLid) {
            if (level.isClientSide()) {
                return ItemInteractionResult.SUCCESS;
            }
            if (player.isSecondaryUseActive()) {
                juicer.pickup(level, pos, player);
            } else {
                juicer.toggleOpen();
            }
            return ItemInteractionResult.SUCCESS;
        }

        // 点在瓶身上：空手右键取 1 个，潜行取全部
        if (stack.isEmpty()) {
            if (level.isClientSide()) {
                return ItemInteractionResult.SUCCESS;
            }
            if (!open) {
                if (player.isSecondaryUseActive()) {
                    juicer.pickup(level, pos, player);
                }
                return ItemInteractionResult.SUCCESS;
            }
            if (player.isSecondaryUseActive()) {
                juicer.emptyInput(player);
            } else {
                juicer.takeOne(player);
            }
            return ItemInteractionResult.SUCCESS;
        }

        // 瓶身 + 手持流体容器：舀出果汁
        if (FluidUtil.getFluidHandler(stack).isPresent()) {
            if (!level.isClientSide() && open) {
                juicer.drainInto(player, stack);
            }
            return ItemInteractionResult.SUCCESS;
        }

        if (!open) {
            return ItemInteractionResult.SUCCESS;
        }

        if (level.isClientSide()) {
            return ItemInteractionResult.SUCCESS;
        }
        return juicer.addItem(stack)
                ? ItemInteractionResult.SUCCESS
                : ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    /**
     * 获取方块实体每刻更新器。
     * <p>
     * 客户端使用客户端更新器，服务端使用服务端更新器。
     *
     * @param level 世界
     * @param state 方块状态
     * @param type  方块实体类型
     * @return 每刻更新器，无对应类型时返回 null
     */
    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide()
                ? createTickerHelper(type, KSBlockEntities.JUICER.get(), JuicerBlockEntity::clientTick)
                : createTickerHelper(type, KSBlockEntities.JUICER.get(), JuicerBlockEntity::serverTick);
    }

    /**
     * 创建方块实体。
     *
     * @param pos   方块位置
     * @param state 方块状态
     * @return 榨汁机方块实体
     */
    @Override
    @Nullable
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new JuicerBlockEntity(pos, state);
    }

    /**
     * 获取放置时的方块状态。
     * <p>
     * 朝向设为玩家水平朝向的相反方向。
     *
     * @param context 放置上下文
     * @return 放置后的方块状态
     */
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    /**
     * 创建方块状态定义。
     *
     * @param builder 状态定义构建器
     */
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, OPEN);
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
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    /**
     * 获取渲染形状。
     * <p>
     * 使用不可见渲染形状，模型交由方块实体渲染器绘制。
     *
     * @param state 方块状态
     * @return 渲染形状
     */
    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    /**
     * 判断是否具有比较器输出。
     *
     * @param state 方块状态
     * @return 恒为 true
     */
    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    /**
     * 获取比较器输出信号。
     * <p>
     * 按流体量占罐容量的比例映射到 0~15。
     *
     * @param state 方块状态
     * @param level 世界
     * @param pos   方块位置
     * @return 比较器信号强度
     */
    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof JuicerBlockEntity juicer) {
            return Mth.floor(15.0F * (float) juicer.getTank().getFluidAmount() / (float) JuicerBlockEntity.TANK_CAPACITY);
        }
        return 0;
    }

    /**
     * 获取方块掉落物。
     * <p>
     * 若存在榨汁机方块实体，则用携带方块实体数据的物品替换默认掉落。
     *
     * @param state  方块状态
     * @param params 掉落参数构建器
     * @return 掉落物列表
     */
    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> drops = super.getDrops(state, params);
        if (!drops.isEmpty() && params.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof JuicerBlockEntity juicer) {
            drops.set(0, juicer.createDropStack(params.getLevel().registryAccess()));
        }
        return drops;
    }

    /**
     * 旋转方块状态。
     *
     * @param state    方块状态
     * @param rotation 旋转
     * @return 旋转后的方块状态
     */
    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    /**
     * 镜像方块状态。
     *
     * @param state 方块状态
     * @param mirror 镜像
     * @return 镜像后的方块状态
     */
    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    /**
     * 获取方块编解码器。
     *
     * @return 方块编解码器
     */
    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }
}