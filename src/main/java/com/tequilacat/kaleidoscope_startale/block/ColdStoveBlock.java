package com.tequilacat.kaleidoscope_startale.block;

import com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.StoveBlock;
import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.List;

/**
 * 冷灶方块。
 * <p>
 * 仅替换点燃时的粒子效果与物品 tooltip。
 */
public class ColdStoveBlock extends StoveBlock {

    /** 方块编解码器 */
    public static final MapCodec<ColdStoveBlock> CODEC = simpleCodec(p -> new ColdStoveBlock());

    /**
     * 构造冷灶方块。
     */
    public ColdStoveBlock() {
        super();
    }

    /**
     * 获取方块编解码器。
     *
     * @return 方块编解码器
     */
    @Override
    public MapCodec<? extends ColdStoveBlock> codec() {
        return CODEC;
    }

    /**
     * 客户端每刻动画效果。
     * <p>
     * 仅在方块处于点燃状态时生效：随机播放营火噼啪声，
     * 并生成灵魂火粒子与灵魂火火焰粒子作为视觉表现。
     *
     * @param state  方块状态
     * @param level  世界实例
     * @param pos    方块位置
     * @param random 随机源
     */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(BlockStateProperties.LIT)) {
            return;
        }
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.5;
        double z = pos.getZ() + 0.5;

        if (random.nextInt(10) == 0) {
            level.playLocalSound(x, y, z,
                    SoundEvents.CAMPFIRE_CRACKLE,
                    SoundSource.BLOCKS,
                    0.5F + random.nextFloat(),
                    random.nextFloat() * 0.7F + 0.6F, false);
        }

        level.addParticle(ParticleTypes.SOUL,
                x + random.nextDouble() / 3 * (random.nextBoolean() ? 1 : -1),
                y + 0.5 + random.nextDouble() / 3,
                z + random.nextDouble() / 3 * (random.nextBoolean() ? 1 : -1),
                0, 0.02, 0);

        Direction direction = state.getValue(FACING);
        Direction.Axis axis = direction.getAxis();
        double offsetRandom = random.nextDouble() * 0.6 - 0.3;
        double xOffset = axis == Direction.Axis.X ? (double) direction.getStepX() * 0.52 : offsetRandom;
        double yOffset = 0.25 + random.nextDouble() * 6.0 / 16.0;
        double zOffset = axis == Direction.Axis.Z ? (double) direction.getStepZ() * 0.52 : offsetRandom;
        level.addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                x + xOffset,
                pos.getY() + yOffset,
                z + zOffset,
                0, 0, 0);
    }

    /**
     * 追加物品提示信息。
     * <p>
     * 在提示中追加一行灰色文本，用于说明冷灶的用途。
     *
     * @param stack   物品堆
     * @param context 提示上下文
     * @param tooltip 提示信息列表
     * @param flag    提示标志
     */
    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.kaleidoscope_startale.cold_stove").withStyle(ChatFormatting.GRAY));
    }
}