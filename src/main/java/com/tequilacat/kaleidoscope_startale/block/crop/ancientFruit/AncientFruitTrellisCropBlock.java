package com.tequilacat.kaleidoscope_startale.block.crop.ancientFruit;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.ItemAbilities;

/**
 * 上古水果棚架作物方块。
 * <p>
 * 依附成熟藤蔓生长，可催熟，成熟后可用剪刀采收产物。
 */
public class AncientFruitTrellisCropBlock extends Block implements BonemealableBlock {

    /** 生长阶段属性。 */
    public static final IntegerProperty AGE = BlockStateProperties.AGE_1;

    /** 最大生长阶段。 */
    public static final int MAX_AGE = 1;

    /** 方块形状。 */
    public static final VoxelShape SHAPE = Block.box(2.0D, 6.0D, 2.0D, 14.0D, 16.0D, 14.0D);

    /** 每随机刻生长概率。 */
    private static final float GROW_PER_TICK_PROBABILITY = 0.25F;

    /** 上方藤蔓方块供应器。 */
    private final Supplier<Block> vineTrellis;

    /** 产物物品供应器。 */
    private final Supplier<Item> produce;

    /** 采集产物数量。 */
    private final int harvestCount;

    /**
     * 构造藤架作物方块。
     * <p>
     * 默认阶段为成熟（1）。
     *
     * @param vineTrellis 上方藤蔓方块供应器
     * @param produce     产物物品供应器
     * @param harvestCount 采集产物数量
     * @param properties  方块属性
     */
    public AncientFruitTrellisCropBlock(Supplier<Block> vineTrellis, Supplier<Item> produce, int harvestCount,
                                        BlockBehaviour.Properties properties) {
        super(properties);
        this.vineTrellis = vineTrellis;
        this.produce = produce;
        this.harvestCount = harvestCount;
        this.registerDefaultState(this.stateDefinition.any().setValue(AGE, 1));
    }

    /**
     * 处理玩家对物品使用方块。
     * <p>
     * 若手持可剪切物品且作物已成熟，则移除方块、掉落产物、
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
        ItemStack heldItem = player.getItemInHand(hand);
        if (heldItem.canPerformAction(ItemAbilities.SHEARS_HARVEST) && this.isMaxAge(state)) {
            level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
            Block.popResource(level, pos, new ItemStack(this.produce.get(), this.harvestCount));
            heldItem.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
            player.playSound(SoundEvents.BEEHIVE_SHEAR);
            return ItemInteractionResult.SUCCESS;
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    /**
     * 判断是否随机刻更新。
     * <p>
     * 仅在未成熟时随机刻更新。
     *
     * @param state 方块状态
     * @return 是否随机刻更新
     */
    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return super.isRandomlyTicking(state) && state.getValue(AGE) < MAX_AGE;
    }

    /**
     * 随机刻更新。
     * <p>
     * 按生长概率判断后提升一个生长阶段。
     *
     * @param state  方块状态
     * @param level  服务端世界
     * @param pos    方块位置
     * @param random 随机源
     */
    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (CommonHooks.canCropGrow(level, pos, state, random.nextDouble() < (double) GROW_PER_TICK_PROBABILITY)) {
            level.setBlockAndUpdate(pos, state.cycle(AGE));
            CommonHooks.fireCropGrowPost(level, pos, state);
        }
    }

    /**
     * 更新方块状态。
     * <p>
     * 无法存活时变为空气，否则按默认逻辑更新。
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
        return state.canSurvive(level, pos)
                ? super.updateShape(state, direction, neighborState, level, pos, neighborPos)
                : Blocks.AIR.defaultBlockState();
    }

    /**
     * 判断方块能否存活。
     * <p>
     * 要求上方为成熟的上古水果藤方块。
     *
     * @param state 方块状态
     * @param level 世界读取器
     * @param pos   方块位置
     * @return 能否存活
     */
    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockState above = level.getBlockState(pos.above());
        if (above.getBlock() == this.vineTrellis.get() && above.getBlock() instanceof AncientFruitVineBlock vine) {
            return vine.isMaxAge(above);
        }
        return false;
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
     * 判断是否为有效的骨粉使用目标。
     *
     * @param level 世界读取器
     * @param pos   方块位置
     * @param state 方块状态
     * @return 是否为有效目标
     */
    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return !this.isMaxAge(state);
    }

    /**
     * 判断骨粉是否催熟成功。
     *
     * @param level  世界
     * @param random 随机源
     * @param pos    方块位置
     * @param state  方块状态
     * @return 恒为 true
     */
    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    /**
     * 执行骨粉催熟。
     * <p>
     * 随机提升 1 到 2 个阶段，且不超过最大阶段。
     *
     * @param level  服务端世界
     * @param random 随机源
     * @param pos    方块位置
     * @param state  方块状态
     */
    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        int newAge = Math.min(state.getValue(AGE) + random.nextInt(1, 3), MAX_AGE);
        level.setBlockAndUpdate(pos, state.setValue(AGE, newAge));
    }

    /**
     * 创建方块状态定义。
     *
     * @param builder 状态定义构建器
     */
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
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
        return SHAPE;
    }

    /**
     * 获取方块掉落物。
     * <p>
     * 仅在成熟时返回默认掉落，未成熟时无掉落。
     *
     * @param state             方块状态
     * @param lootParamsBuilder 掉落参数构建器
     * @return 掉落物列表
     */
    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder lootParamsBuilder) {
        return this.isMaxAge(state) ? super.getDrops(state, lootParamsBuilder) : Collections.emptyList();
    }
}