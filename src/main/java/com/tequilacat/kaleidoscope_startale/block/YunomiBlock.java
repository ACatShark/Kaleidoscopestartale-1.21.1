package com.tequilacat.kaleidoscope_startale.block;

import com.github.ysbbbbbb.kaleidoscopecookery.block.drink.EmptyCupBlock;
import com.github.ysbbbbbb.kaleidoscopecookery.block.drink.TeacupBlock;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModParticles;
import com.github.ysbbbbbb.kaleidoscopecookery.item.TeapotItem;
import com.github.ysbbbbbb.kaleidoscopecookery.util.ItemUtils;
import com.mojang.serialization.MapCodec;
import com.tequilacat.kaleidoscope_startale.init.KSItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 云吞方块。
 * <p>
 * 继承自空杯方块，支持三种交互：
 * - 使用茶壶倒茶：将云吞转换为对应的茶杯方块；
 * - 使用云吞物品：在未达上限时堆叠杯数；
 * - 空手右键：取下一只云吞。
 * 方块被破坏时按其堆叠数量掉落对应数量的云吞物品。
 */
public class YunomiBlock extends EmptyCupBlock {

    /** 方块编解码器 */
    public static final MapCodec<YunomiBlock> CODEC = simpleCodec(p -> new YunomiBlock());

    /**
     * 构造云吞方块。
     */
    public YunomiBlock() {
        super();
    }

    /**
     * 获取方块编解码器。
     *
     * @return 方块编解码器
     */
    @Override
    public MapCodec<? extends EmptyCupBlock> codec() {
        return CODEC;
    }

    /**
     * 获取方块的掉落物。
     * <p>
     * 若当前堆叠数量大于 0，则按其数量掉落云吞物品；
     * 否则交由父类处理。
     *
     * @param state  方块状态
     * @param params 战利品参数构建器
     * @return 掉落物列表
     */
    @Override
    public @NotNull List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        int cupCount = state.getValue(CUP_COUNT);
        return cupCount > 0
                ? List.of(new ItemStack(KSItems.YUNOMI.get(), cupCount))
                : super.getDrops(state, params);
    }

    /**
     * 处理在方块上使用物品的交互。
     * <p>
     * 仅响应主手交互：
     * - 手持茶壶时，将倒出的茶按茶杯方块转换，超出上限的部分退回为云吞物品；
     * - 手持云吞时，未达堆叠上限则增加杯数并消耗物品；
     * - 空手时取下一只云吞，数量归零则移除方块。
     *
     * @param stack  使用的物品堆
     * @param state  方块状态
     * @param level  世界实例
     * @param pos    方块位置
     * @param player 交互的玩家
     * @param hand   交互手
     * @param hit    方块点击结果
     * @return 物品交互结果
     */
    @Override
    public ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        ItemStack itemInHand = player.getItemInHand(hand);

        // 如果是茶壶
        if (itemInHand.is(com.github.ysbbbbbb.kaleidoscopecookery.init.ModItems.TEAPOT.get())) {
            ItemStack pourOut = TeapotItem.getPourOut(itemInHand, level);
            if (pourOut.isEmpty()) {
                return ItemInteractionResult.CONSUME;
            }
            // 必须是茶
            if (!(pourOut.getItem() instanceof com.github.ysbbbbbb.kaleidoscopecookery.item.TeacupItem teacupItem && teacupItem.getBlock() instanceof TeacupBlock teacupBlock)) {
                return ItemInteractionResult.CONSUME;
            }
            // 检查当前茶杯数量，是否超过转换的茶的上限，否则需要退回
            int currentCount = state.getValue(CUP_COUNT);
            if (currentCount > teacupBlock.getMaxCount()) {
                ItemStack returnStack = new ItemStack(KSItems.YUNOMI.get(), currentCount - teacupBlock.getMaxCount());
                ItemUtils.getItemToLivingEntity(player, returnStack);
            }
            // 将茶转换
            level.playSound(player, pos, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 1.0F, 1.0F);
            TeapotItem.pourOut(itemInHand, level);
            spawnPourParticles(level, pos);
            level.setBlockAndUpdate(pos, teacupBlock.defaultBlockState()
                    .setValue(teacupBlock.getCupCountProperty(), Math.min(currentCount, teacupBlock.getMaxCount()))
                    .setValue(teacupBlock.getTeaCountProperty(), 1)
                    .setValue(FACING, state.getValue(FACING)));
            return ItemInteractionResult.SUCCESS;
        }

        // 如果是云吞杯，堆叠
        if (itemInHand.is(KSItems.YUNOMI.get())) {
            int count = state.getValue(CUP_COUNT);
            if (count < MAX_COUNT) {
                level.setBlockAndUpdate(pos, state.setValue(CUP_COUNT, count + 1));
                level.playSound(player, pos, this.soundType.getPlaceSound(), SoundSource.BLOCKS, 1.0F, 1.0F);
                itemInHand.shrink(1);
                return ItemInteractionResult.SUCCESS;
            }
            return ItemInteractionResult.CONSUME;
        }

        // 如果是空手，取下云吞杯
        if (itemInHand.isEmpty()) {
            int cupCountNum = state.getValue(CUP_COUNT);
            if (cupCountNum > 0) {
                ItemStack cupStack = new ItemStack(KSItems.YUNOMI.get());
                ItemUtils.getItemToLivingEntity(player, cupStack);
                if (cupCountNum == 1) {
                    level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                } else {
                    level.setBlockAndUpdate(pos, state.setValue(CUP_COUNT, cupCountNum - 1));
                }
                level.playSound(player, pos, this.soundType.getBreakSound(), SoundSource.BLOCKS, 1.0F, 1.0F);
                return ItemInteractionResult.SUCCESS;
            }
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    /**
     * 在方块位置生成倒茶粒子效果。
     * <p>
     * 仅在服务端执行，围绕方块顶部生成少量烹饪粒子。
     *
     * @param level 世界实例
     * @param pos   方块位置
     */
    private static void spawnPourParticles(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        RandomSource random = level.random;
        serverLevel.sendParticles(ModParticles.COOKING.get(),
                pos.getX() + 0.5,
                pos.getY() + 0.35,
                pos.getZ() + 0.5,
                4,
                0.12 + random.nextDouble() * 0.04,
                0.08,
                0.12 + random.nextDouble() * 0.04,
                0.02);
    }
}