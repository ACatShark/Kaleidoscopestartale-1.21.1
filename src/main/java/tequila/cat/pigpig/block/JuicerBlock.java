package tequila.cat.pigpig.block;

import java.util.List;

import com.mojang.serialization.MapCodec;

import org.jetbrains.annotations.Nullable;

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

import tequila.cat.pigpig.block.entity.JuicerBlockEntity;
import tequila.cat.pigpig.init.ModBlockEntities;

/**
 * 榨汁机：OPEN 状态表示「盖子打开」（打开时不工作，可投料 / 取料）。
 * 渲染交给 JuicerRenderer（方块模型 + 液体 + 悬浮食材）。
 */
public class JuicerBlock extends BaseEntityBlock {
    public static final MapCodec<JuicerBlock> CODEC = simpleCodec(JuicerBlock::new);

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty OPEN = BooleanProperty.create("open");

    private static final VoxelShape SHAPE = Block.box(4.0D, 0.0D, 4.0D, 12.0D, 16.0D, 12.0D);

    /** 盖子起始高度（14/16），命中点在这以上按“点在盖子上”处理。 */
    private static final double LID_MIN_Y = 14.0D / 16.0D;

    public JuicerBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(OPEN, false));
    }

    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.WOOD)
                .instrument(NoteBlockInstrument.GUITAR)
                .strength(1.0F)
                .sound(SoundType.WOOD)
                .noOcclusion()
                .ignitedByLava();
    }

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

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide()
                ? createTickerHelper(type, ModBlockEntities.JUICER.get(), JuicerBlockEntity::clientTick)
                : createTickerHelper(type, ModBlockEntities.JUICER.get(), JuicerBlockEntity::serverTick);
    }

    @Override
    @Nullable
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new JuicerBlockEntity(pos, state);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, OPEN);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof JuicerBlockEntity juicer) {
            return Mth.floor(15.0F * (float) juicer.getTank().getFluidAmount() / (float) JuicerBlockEntity.TANK_CAPACITY);
        }
        return 0;
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> drops = super.getDrops(state, params);
        if (!drops.isEmpty() && params.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof JuicerBlockEntity juicer) {
            drops.set(0, juicer.createDropStack(params.getLevel().registryAccess()));
        }
        return drops;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }
}
