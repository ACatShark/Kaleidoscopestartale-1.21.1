package com.tequilacat.kaleidoscope_startale.mixin;

import com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.OilPotBlock;
import com.tequilacat.kaleidoscope_startale.init.KSBlocks;
import com.tequilacat.kaleidoscope_startale.init.KSItems;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 油锅方块的 Mixin。
 * <p>
 * 在油锅原交互未处理时，若玩家手持奶油且目标方块不是奶油油锅，
 * 则将油锅替换为奶油油锅，并消耗一份奶油。
 */
@Mixin(OilPotBlock.class)
public abstract class OilPotBlockMixin {

    /**
     * 在油锅物品交互返回后注入，处理放置奶油油锅的逻辑。
     * <p>
     * 仅在主手交互、原交互未处理、手持奶油且目标方块非奶油油锅时生效：
     * 以原方块朝向与含水状态创建奶油油锅方块并替换，非创造模式消耗奶油，
     * 播放放置音效并将交互结果设为成功。
     *
     * @param stack     使用的物品堆
     * @param state     方块状态
     * @param level     世界实例
     * @param pos       方块位置
     * @param player    交互的玩家
     * @param hand      交互手
     * @param hitResult 方块点击结果
     * @param cir       回调信息，用于读取与设置返回值
     */
    @Inject(method = "useItemOn", at = @At("RETURN"), cancellable = true, remap = false)
    private void KS$useCreamOnOilPot(
            ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hitResult,
            CallbackInfoReturnable<ItemInteractionResult> cir
    ) {
        if (hand != InteractionHand.MAIN_HAND) return;
        if (cir.getReturnValue() != ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION) return;
        ItemStack mainHand = player.getMainHandItem();
        if (!mainHand.is(KSItems.CREAM.get())) return;
        if (state.is(KSBlocks.CREAM_OIL.get())) return;

        BlockState creamState = KSBlocks.CREAM_OIL.get().defaultBlockState()
                .setValue(HorizontalDirectionalBlock.FACING,
                        state.getValue(HorizontalDirectionalBlock.FACING))
                .setValue(BlockStateProperties.WATERLOGGED,
                        state.getValue(BlockStateProperties.WATERLOGGED));
        level.setBlock(pos, creamState, 3);
        if (!player.isCreative()) {
            mainHand.shrink(1);
        }
        player.playSound(SoundEvents.LANTERN_HIT, 1.0F, player.getRandom().nextFloat() * 0.2F + 0.8F);
        cir.setReturnValue(ItemInteractionResult.SUCCESS);
    }
}