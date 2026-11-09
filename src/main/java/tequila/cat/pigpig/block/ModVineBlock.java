package tequila.cat.pigpig.block;

import java.util.function.Supplier;

import com.github.ysbbbbbb.kaleidoscopetavern.block.plant.ITrellis;
import com.github.ysbbbbbb.kaleidoscopetavern.block.properties.TrellisType;
import com.github.ysbbbbbb.kaleidoscopetavern.init.ModBlocks;

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

/**
 * 上古水果藤：可从藤架向上 / 向水平蔓延到相邻藤架，
 * 成熟后会在自身下方结出上古水果（ModTrellisCropBlock）。
 * 剪刀右键可把藤收回成物品。
 */
public class ModVineBlock extends Block implements SimpleWaterloggedBlock, ITrellis, BonemealableBlock {
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final IntegerProperty AGE = BlockStateProperties.AGE_1;
    public static final int MAX_AGE = 1;

    /** 允许蔓延的方向（不含向下：向下是结果） */
    public static final Direction[] CHECK_DIRECTION = {
            Direction.UP, Direction.EAST, Direction.WEST, Direction.SOUTH, Direction.NORTH
    };

    private final float growPerTickProbability;
    private final float boneMealChance;
    private final Supplier<Block> fruitBlock;
    private final Supplier<Item> vineItem;

    public ModVineBlock(float growPerTickProbability, float boneMealChance,
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

    private static Block emptyTrellis() {
        return ModBlocks.TRELLIS.get();
    }

    private static Block grapevineTrellis() {
        return ModBlocks.GRAPEVINE_TRELLIS.get();
    }

    @Override
    public boolean sameType(BlockState state) {
        return state.is(this)
                || state.is(tequila.cat.pigpig.init.ModBlocks.ANCIENT_FRUIT_VINE.get())
                || state.is(emptyTrellis())
                || state.is(grapevineTrellis());
    }

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

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (CommonHooks.canCropGrow(level, pos, state, random.nextDouble() < (double) this.growPerTickProbability)) {
            this.doGrow(level, pos, state);
        }
    }

    public boolean isMaxAge(BlockState state) {
        return state.getValue(AGE) >= MAX_AGE;
    }

    public boolean belowSupportGrow(BlockState belowState) {
        return belowState.is(this) ? this.isMaxAge(belowState) : belowState.is(BlockTags.DIRT);
    }

    public boolean canGrowInto(BlockState checkState) {
        return checkState.is(emptyTrellis());
    }

    public BlockState getGrowIntoState(Direction direction, BlockState checkState) {
        TrellisType type = checkState.getOptionalValue(TYPE).orElse(TrellisType.SINGLE);
        boolean waterlogged = checkState.getOptionalValue(WATERLOGGED).orElse(false);
        int age = direction == Direction.UP ? 0 : 1;
        return this.defaultBlockState()
                .setValue(TYPE, type)
                .setValue(AGE, age)
                .setValue(WATERLOGGED, waterlogged);
    }

    public boolean canGrowFruit(LevelReader level, BlockPos pos) {
        return pos.getY() >= level.getMinBuildHeight() + 1 && level.getBlockState(pos.below()).isAir();
    }

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

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return this.boneMealChance > 0.0F && this.canGrow(level, pos, state);
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return random.nextFloat() < this.boneMealChance;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        this.doGrow(level, pos, state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(TYPE, AGE, WATERLOGGED);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return this.collisionShape(state.getValue(TYPE));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return this.selectShape(state.getValue(TYPE));
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader level, BlockPos pos, Player player) {
        return this.vineItem.get().getDefaultInstance();
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }
}
