package tequila.cat.pigpig.block;

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
import net.minecraft.world.level.ItemLike;
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
 * 藤架作物：必须长在「成熟的上古水果藤」正下方才能存活，
 * 成熟后用剪刀右键采集产物。
 */
public class ModTrellisCropBlock extends Block implements BonemealableBlock {
    public static final IntegerProperty AGE = BlockStateProperties.AGE_1;
    public static final int MAX_AGE = 1;
    public static final VoxelShape SHAPE = Block.box(2.0D, 6.0D, 2.0D, 14.0D, 16.0D, 14.0D);

    private static final float GROW_PER_TICK_PROBABILITY = 0.25F;

    private final Supplier<Block> vineTrellis;
    private final Supplier<Item> produce;
    private final int harvestCount;

    public ModTrellisCropBlock(Supplier<Block> vineTrellis, Supplier<Item> produce, int harvestCount,
                               BlockBehaviour.Properties properties) {
        super(properties);
        this.vineTrellis = vineTrellis;
        this.produce = produce;
        this.harvestCount = harvestCount;
        this.registerDefaultState(this.stateDefinition.any().setValue(AGE, 1));
    }

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

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return super.isRandomlyTicking(state) && state.getValue(AGE) < MAX_AGE;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (CommonHooks.canCropGrow(level, pos, state, random.nextDouble() < (double) GROW_PER_TICK_PROBABILITY)) {
            level.setBlockAndUpdate(pos, state.cycle(AGE));
            CommonHooks.fireCropGrowPost(level, pos, state);
        }
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                  LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return state.canSurvive(level, pos)
                ? super.updateShape(state, direction, neighborState, level, pos, neighborPos)
                : Blocks.AIR.defaultBlockState();
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockState above = level.getBlockState(pos.above());
        if (above.getBlock() == this.vineTrellis.get() && above.getBlock() instanceof ModVineBlock vine) {
            return vine.isMaxAge(above);
        }
        return false;
    }

    public boolean isMaxAge(BlockState state) {
        return state.getValue(AGE) >= MAX_AGE;
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return !this.isMaxAge(state);
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        int newAge = Math.min(state.getValue(AGE) + random.nextInt(1, 3), MAX_AGE);
        level.setBlockAndUpdate(pos, state.setValue(AGE, newAge));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder lootParamsBuilder) {
        return this.isMaxAge(state) ? super.getDrops(state, lootParamsBuilder) : Collections.emptyList();
    }
}
