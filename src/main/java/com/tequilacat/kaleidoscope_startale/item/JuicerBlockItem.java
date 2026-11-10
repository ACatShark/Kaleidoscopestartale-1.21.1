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

/**
 * 榨汁机方块物品。
 * <p>
 * 继承方块物品，支持携带方块实体数据放置（保留内容物），
 * 并在对空酒杯使用时将罐内果汁灌注为对应果汁方块。
 */
public class JuicerBlockItem extends BlockItem {

    /**
     * 构造榨汁机方块物品。
     *
     * @param block      关联方块
     * @param properties 物品属性
     */
    public JuicerBlockItem(Block block, Item.Properties properties) {
        super(block, properties);
    }

    /**
     * 对物品使用方块。
     * <p>
     * 若目标为空酒杯且物品罐内果汁不少于一杯，则将酒杯替换为对应果汁方块、
     * 扣除流体、回写物品数据并播放灌注音效；否则回退到默认放置逻辑。
     *
     * @param context 使用上下文
     * @return 交互结果
     */
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

    /**
     * 放置方块。
     * <p>
     * 若物品携带方块实体数据，则在创造模式下额外处理：
     * 手动扣减一个物品并同步物品栏，使带内容物的榨汁机也能正常消耗。
     *
     * @param context 放置上下文
     * @return 放置结果
     */
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

    /**
     * 从物品数据中读取流体罐。
     * <p>
     * 无方块实体数据或不含罐数据时返回 null。
     *
     * @param stack      物品堆叠
     * @param registries 注册表提供者
     * @return 流体罐，无法读取时返回 null
     */
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

    /**
     * 将流体罐写回物品数据。
     * <p>
     * 仅当物品已携带方块实体数据时执行，覆盖其中的罐数据。
     *
     * @param stack      物品堆叠
     * @param tank       流体罐
     * @param registries 注册表提供者
     */
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