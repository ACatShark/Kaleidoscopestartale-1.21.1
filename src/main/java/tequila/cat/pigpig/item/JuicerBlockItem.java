package tequila.cat.pigpig.item;

import java.util.function.Consumer;

import com.github.ysbbbbbb.kaleidoscopetavern.block.mixology.GlasswareBlock;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;

import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

import tequila.cat.pigpig.block.entity.JuicerBlockEntity;
import tequila.cat.pigpig.client.JuicerItemStackRenderer;
import tequila.cat.pigpig.data.JuiceBlocks;

/**
 * 榨汁机方块物品：带 BEWLR，物品栏里也能显示内部液体与食材；
 * 创造模式放置带内容的榨汁机时手动扣数量，因为原版不会扣。
 * 另外手持带果汁的榨汁机对空酒杯右键，会扣掉 JuiceBlocks.AMOUNT_PER_GLASS mB，
 * 并把空酒杯换成对应口味的果汁杯。
 */
public class JuicerBlockItem extends BlockItem {
    public JuicerBlockItem(Block block, Item.Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState targetState = level.getBlockState(pos);

        if (!targetState.is(com.github.ysbbbbbb.kaleidoscopetavern.init.ModBlocks.EMPTY_GLASSWARE.get())) {
            return super.useOn(context);
        }

        ItemStack stack = context.getItemInHand();
        FluidTank tank = readTank(stack, level.registryAccess());
        if (tank == null || tank.getFluidAmount() < JuiceBlocks.AMOUNT_PER_GLASS) {
            return InteractionResult.PASS;
        }

        Block juiceBlock = JuiceBlocks.blockForFluid(tank.getFluid().getFluid());
        if (juiceBlock == null) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        BlockState juiceState = juiceBlock.defaultBlockState();
        if (juiceState.hasProperty(HorizontalDirectionalBlock.FACING)
                && targetState.hasProperty(HorizontalDirectionalBlock.FACING)) {
            juiceState = juiceState.setValue(HorizontalDirectionalBlock.FACING,
                    targetState.getValue(HorizontalDirectionalBlock.FACING));
        }
        if (juiceState.hasProperty(GlasswareBlock.WATERLOGGED)
                && targetState.hasProperty(GlasswareBlock.WATERLOGGED)) {
            juiceState = juiceState.setValue(GlasswareBlock.WATERLOGGED,
                    targetState.getValue(GlasswareBlock.WATERLOGGED));
        }

        level.setBlockAndUpdate(pos, juiceState);
        tank.drain(JuiceBlocks.AMOUNT_PER_GLASS, FluidAction.EXECUTE);
        writeTank(stack, tank, level.registryAccess());
        level.playSound(null, pos, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 0.8F, 1.4F);
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult place(BlockPlaceContext context) {
        ItemStack stack = context.getItemInHand();
        boolean withContents = stack.has(DataComponents.BLOCK_ENTITY_DATA);
        int originalCount = stack.getCount();

        InteractionResult result = super.place(context);
        if (!result.consumesAction() || !withContents) {
            return result;
        }

        Player player = context.getPlayer();
        if (player != null && player.isCreative() && !context.getLevel().isClientSide) {
            ItemStack remaining = stack.copy();
            remaining.setCount(originalCount - 1);
            player.setItemInHand(context.getHand(), remaining.isEmpty() ? ItemStack.EMPTY : remaining);
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.inventoryMenu.sendAllDataToRemote();
            }
        }
        return result;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private JuicerItemStackRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null) {
                    this.renderer = new JuicerItemStackRenderer();
                }
                return this.renderer;
            }
        });
    }

    /** 从物品的 BLOCK_ENTITY_DATA 里读出榨汁机液罐；没有内容物时返回 null。 */
    @Nullable
    private static FluidTank readTank(ItemStack stack, HolderLookup.Provider registries) {
        CustomData data = stack.get(DataComponents.BLOCK_ENTITY_DATA);
        if (data == null) {
            return null;
        }
        CompoundTag tag = data.copyTag();
        if (!tag.contains("tank")) {
            return null;
        }
        FluidTank tank = new FluidTank(JuicerBlockEntity.TANK_CAPACITY);
        tank.readFromNBT(registries, tag.getCompound("tank"));
        return tank;
    }

    private static void writeTank(ItemStack stack, FluidTank tank, HolderLookup.Provider registries) {
        CustomData data = stack.get(DataComponents.BLOCK_ENTITY_DATA);
        if (data == null) {
            return;
        }
        CompoundTag tag = data.copyTag();
        tag.put("tank", tank.writeToNBT(registries, new CompoundTag()));
        stack.set(DataComponents.BLOCK_ENTITY_DATA, CustomData.of(tag));
    }
}
