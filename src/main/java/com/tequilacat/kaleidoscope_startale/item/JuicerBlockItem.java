package com.tequilacat.kaleidoscope_startale.item;

import com.github.ysbbbbbb.kaleidoscopetavern.block.mixology.GlasswareBlock;
import com.tequilacat.kaleidoscope_startale.blockentity.JuicerBlockEntity;
import com.tequilacat.kaleidoscope_startale.util.JuiceBlocks;
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
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

import javax.annotation.Nullable;

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
        tank.drain(JuiceBlocks.AMOUNT_PER_GLASS, IFluidHandler.FluidAction.EXECUTE);
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
