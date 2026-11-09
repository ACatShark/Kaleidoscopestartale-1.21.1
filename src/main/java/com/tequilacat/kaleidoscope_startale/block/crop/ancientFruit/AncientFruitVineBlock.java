package com.tequilacat.kaleidoscope_startale.block.crop.ancientFruit;

import com.github.ysbbbbbb.kaleidoscopetavern.block.plant.ITrellis;
import com.github.ysbbbbbb.kaleidoscopetavern.block.properties.TrellisType;
import com.github.ysbbbbbb.kaleidoscopetavern.init.ModBlocks;
import com.tequilacat.kaleidoscope_startale.init.KSBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.ItemAbilities;

import java.util.function.Supplier;

/**
 * 上古水果藤蔓方块。
 * <p>
 * 依附于棚架生长：可随随机刻成长、向周围棚架蔓延，
 * 成熟后可在下方结出果实。支持骨粉催熟、剪刀剪取藤蔓、
 * 含水状态以及随棚架连接变化更新类型。
 */
public class AncientFruitVineBlock extends Block implements SimpleWaterloggedBlock, ITrellis, BonemealableBlock {

    /** 含水状态属性。 */
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    /** 生长阶段属性。 */
    public static final IntegerProperty AGE = BlockStateProperties.AGE_1;

    /** 最大生长阶段。 */
    public static final int MAX_AGE = 1;

    /**
     * 生长时检查相邻棚架的方向顺序。
     */
    public static final Direction[] CHECK_DIRECTION = {
            Direction.UP, Direction.EAST, Direction.WEST, Direction.SOUTH, Direction.NORTH
    };

    /** 每随机刻生长概率。 */
    private final float growPerTickProbability;

    /** 骨粉催熟成功概率。 */
    private final float boneMealChance;

    /** 果实方块供应器。 */
    private final Supplier<Block> fruitBlock;

    /** 藤蔓物品供应器。 */
    private final Supplier<Item> vineItem;

    /**
     * 构造上古水果藤蔓方块。
     *
     * @param growPerTickProbability 每随机刻生长概率
     * @param boneMealChance         骨粉催熟成功概率
     * @param fruitBlock             果实方块供应器
     * @param vineItem               藤蔓物品供应器
     */
    public AncientFruitVineBlock(float growPerTickProbability, float boneMealChance,
                                 Supplier<Block> fruitBlock, Supplier<Item> vineItem) {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.WOOD)
                .instrument(NoteBlockInstrument.GUITAR)
                .strength(0.8F)
                .sound(SoundType.WOOD)
                .randomTicks()
                .noOcclusion()
                .pushReaction(PushReaction.DESTROY)
                .ignitedByLava());
        this.fruitBlock = fruitBlock;
        this.vineItem = vineItem;
        this.growPerTickProbability = growPerTickProbability;
        this.boneMealChance = boneMealChance;
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(TYPE, TrellisType.SINGLE)
                .setValue(AGE, 0)
                .setValue(WATERLOGGED, false));
    }

    /**
     * 获取空棚架方块。
     *
     * @return 空棚架方块
     */
    private static Block emptyTrellis() {
        return ModBlocks.TRELLIS.get();
    }

    /**
     * 获取葡萄藤棚架方块。
     *
     * @return 葡萄藤棚架方块
     */
    private static Block grapevineTrellis() {
        return ModBlocks.GRAPEVINE_TRELLIS.get();
    }

    /**
     * 判断方块状态是否与本方块同属可连接类型。
     * <p>
     * 本方块、上古水果藤蔓、空棚架与葡萄藤棚架均视为同类型。
     *
     * @param state 方块状态
     * @return 是否同类型
     */
    @Override
    public boolean sameType(BlockState state) {
        return state.is(this)
                || state.is(KSBlocks.ANCIENT_FRUIT_VINE.get())
                || state.is(emptyTrellis())
                || state.is(grapevineTrellis());
    }

    /**
     * 处理玩家对物品使用方块。
     * <p>
     * 若手持可剪切物品，则将方块替换为空棚架、掉落藤蔓、
     * 消耗工具耐久并播放剪取音效。
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
    public ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                           Player player, InteractionHand hand, BlockHitResult hitResult) {
        ItemStack itemInHand = player.getItemInHand(hand);
        if (!itemInHand.canPerformAction(ItemAbilities.SHEARS_HARVEST)) {
            return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
        }

        BlockState newState = emptyTrellis().defaultBlockState()
                .setValue(TYPE, state.getValue(TYPE))
                .setValue(BlockStateProperties.WATERLOGGED, state.getValue(WATERLOGGED));
        level.setBlockAndUpdate(pos, newState);
        Block.popResource(level, pos, this.vineItem.get().getDefaultInstance());
        itemInHand.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
        player.playSound(SoundEvents.BEEHIVE_SHEAR);
        return ItemInteractionResult.SUCCESS;
    }

    /**
     * 更新方块状态。
     * <p>
     * 含水时调度水流刻，并根据三个轴向上的棚架连接情况更新类型。
     *
     * @param state         方块状态
     * @param direction     变化方向
     * @param neighborState 相邻方块状态
     * @param level         世界访问器
     * @param pos           方块位置
     * @param neighborPos   相邻方块位置
     * @return 更新后的方块状态
     */
    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                  LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }

        boolean xHas = ITrellis.axisHasTrellis(level, pos, Axis.X);
        boolean yHas = ITrellis.axisHasTrellis(level, pos, Axis.Y);
        boolean zHas = ITrellis.axisHasTrellis(level, pos, Axis.Z);
        state = state.setValue(TYPE, ITrellis.updateType(state.getValue(TYPE), xHas, yHas, zHas));
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    /**
     * 随机刻更新。
     * <p>
     * 按生长概率判断后执行一次生长。
     *
     * @param state  方块状态
     * @param level  服务端世界
     * @param pos    方块位置
     * @param random 随机源
     */
    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (CommonHooks.canCropGrow(level, pos, state, random.nextDouble() < (double) this.growPerTickProbability)) {
            this.doGrow(level, pos, state);
        }
    }

    /**
     * 判断是否达到最大生长阶段。
     *
     * @param state 方块状态
     * @return 是否成熟
     */
    public boolean isMaxAge(BlockState state) {
        return state.getValue(AGE) >= MAX_AGE;
    }

    /**
     * 判断下方方块是否支持生长。
     * <p>
     * 下方为本方块时要求其已成熟，否则要求为泥土类方块。
     *
     * @param belowState 下方方块状态
     * @return 是否支持生长
     */
    public boolean belowSupportGrow(BlockState belowState) {
        return belowState.is(this) ? this.isMaxAge(belowState) : belowState.is(BlockTags.DIRT);
    }

    /**
     * 判断方块状态是否可被蔓延占用。
     *
     * @param checkState 待检查的方块状态
     * @return 是否可生长进入
     */
    public boolean canGrowInto(BlockState checkState) {
        return checkState.is(emptyTrellis());
    }

    /**
     * 获取蔓延后生成的新方块状态。
     * <p>
     * 沿用目标棚架的类型与含水状态；向上蔓延时阶段为 0，否则为 1。
     *
     * @param direction  蔓延方向
     * @param checkState 目标方块状态
     * @return 新的方块状态
     */
    public BlockState getGrowIntoState(Direction direction, BlockState checkState) {
        TrellisType type = checkState.getOptionalValue(TYPE).orElse(TrellisType.SINGLE);
        boolean waterlogged = checkState.getOptionalValue(WATERLOGGED).orElse(false);
        int age = direction == Direction.UP ? 0 : 1;
        return this.defaultBlockState()
                .setValue(TYPE, type)
                .setValue(AGE, age)
                .setValue(WATERLOGGED, waterlogged);
    }

    /**
     * 判断当前位置能否结出果实。
     * <p>
     * 要求位置不低于世界最低高度加 1，且下方为空气。
     *
     * @param level 世界读取器
     * @param pos   方块位置
     * @return 能否结果
     */
    public boolean canGrowFruit(LevelReader level, BlockPos pos) {
        return pos.getY() >= level.getMinBuildHeight() + 1 && level.getBlockState(pos.below()).isAir();
    }

    /**
     * 判断当前状态下能否生长。
     * <p>
     * 单格类型需检查下方支撑并在未成熟时允许生长；
     * 已成熟时检查相邻是否有可蔓延棚架或能否结果。
     *
     * @param level 世界读取器
     * @param pos   方块位置
     * @param state 方块状态
     * @return 能否生长
     */
    public boolean canGrow(LevelReader level, BlockPos pos, BlockState state) {
        if (state.getValue(TYPE) == TrellisType.SINGLE) {
            BlockState belowState = level.getBlockState(pos.below());
            if (!this.belowSupportGrow(belowState)) {
                return false;
            }
            if (!this.isMaxAge(state)) {
                return true;
            }
        }

        if (this.isMaxAge(state)) {
            for (Direction direction : CHECK_DIRECTION) {
                if (this.canGrowInto(level.getBlockState(pos.relative(direction)))) {
                    return true;
                }
            }
            return this.canGrowFruit(level, pos);
        }
        return true;
    }

    /**
     * 执行一次生长。
     * <p>
     * 单格未成熟时提升阶段；成熟后优先向相邻空棚架蔓延，
     * 否则尝试在下方结出果实；非最大阶段时直接提升到最大阶段。
     *
     * @param level 世界
     * @param pos   方块位置
     * @param state 方块状态
     */
    public void doGrow(Level level, BlockPos pos, BlockState state) {
        if (state.getValue(TYPE) == TrellisType.SINGLE) {
            BlockState belowState = level.getBlockState(pos.below());
            if (!this.belowSupportGrow(belowState)) {
                return;
            }
            if (!this.isMaxAge(state)) {
                level.setBlockAndUpdate(pos, state.cycle(AGE));
                CommonHooks.fireCropGrowPost(level, pos, state);
                return;
            }
        }

        if (this.isMaxAge(state)) {
            for (Direction direction : CHECK_DIRECTION) {
                BlockPos checkPos = pos.relative(direction);
                BlockState checkState = level.getBlockState(checkPos);
                if (this.canGrowInto(checkState)) {
                    level.setBlockAndUpdate(checkPos, this.getGrowIntoState(direction, checkState));
                    CommonHooks.fireCropGrowPost(level, checkPos, checkState);
                    return;
                }
            }

            if (this.canGrowFruit(level, pos)) {
                level.setBlockAndUpdate(pos.below(), this.fruitBlock.get().defaultBlockState());
                CommonHooks.fireCropGrowPost(level, pos.below(), state);
            }
        } else {
            level.setBlockAndUpdate(pos, state.setValue(AGE, MAX_AGE));
            CommonHooks.fireCropGrowPost(level, pos, state);
        }
    }

    /**
     * 判断是否为有效的骨粉使用目标。
     *
     * @param level 世界读取器
     * @param pos   方块位置
     * @param state 方块状态
     * @return 是否为有效目标
     */
    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return this.boneMealChance > 0.0F && this.canGrow(level, pos, state);
    }

    /**
     * 判断骨粉是否催熟成功。
     *
     * @param level  世界
     * @param random 随机源
     * @param pos    方块位置
     * @param state  方块状态
     * @return 是否成功
     */
    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return random.nextFloat() < this.boneMealChance;
    }

    /**
     * 执行骨粉催熟。
     *
     * @param level  服务端世界
     * @param random 随机源
     * @param pos    方块位置
     * @param state  方块状态
     */
    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        this.doGrow(level, pos, state);
    }

    /**
     * 创建方块状态定义。
     *
     * @param builder 状态定义构建器
     */
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(TYPE, AGE, WATERLOGGED);
    }

    /**
     * 获取碰撞形状。
     *
     * @param state   方块状态
     * @param level   方块获取器
     * @param pos     方块位置
     * @param context 碰撞上下文
     * @return 碰撞形状
     */
    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return this.collisionShape(state.getValue(TYPE));
    }

    /**
     * 获取方块形状。
     *
     * @param state   方块状态
     * @param level   方块获取器
     * @param pos     方块位置
     * @param context 碰撞上下文
     * @return 方块形状
     */
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return this.selectShape(state.getValue(TYPE));
    }

    /**
     * 获取创造模式中键拾取的物品。
     *
     * @param state  方块状态
     * @param target 命中结果
     * @param level  世界读取器
     * @param pos    方块位置
     * @param player 玩家
     * @return 藤蔓物品
     */
    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader level, BlockPos pos, Player player) {
        return this.vineItem.get().getDefaultInstance();
    }

    /**
     * 获取流体状态。
     * <p>
     * 含水时返回水源流体状态。
     *
     * @param state 方块状态
     * @return 流体状态
     */
    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }
}