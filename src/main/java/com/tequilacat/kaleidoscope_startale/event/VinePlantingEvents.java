package com.tequilacat.kaleidoscope_startale.event;

import com.github.ysbbbbbb.kaleidoscopetavern.block.plant.ITrellis;
import com.github.ysbbbbbb.kaleidoscopetavern.block.properties.TrellisType;
import com.github.ysbbbbbb.kaleidoscopetavern.init.ModBlocks;
import com.tequilacat.kaleidoscope_startale.block.crop.ancientFruit.AncientFruitVineBlock;
import com.tequilacat.kaleidoscope_startale.init.KSBlocks;
import com.tequilacat.kaleidoscope_startale.init.KSItems;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * 藤蔓种植事件类。
 * <p>
 * 监听玩家右键方块事件，当手持远古果实藤蔓对单格酒馆棚架使用，
 * 且棚架下方为泥土类方块时，将棚架替换为可种植的藤蔓方块。
 */
public class VinePlantingEvents {

    /**
     * 处理右键方块事件。
     * <p>
     * 仅在主手、服务端、目标为单格棚架、手持远古果实藤蔓且下方为泥土类方块时生效；
     * 成功时取消原事件，替换方块为藤蔓、播放种植音效、消耗物品并挥动手臂。
     *
     * @param event 右键方块事件
     */
    @SubscribeEvent
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }

        Level level = event.getLevel();
        if (level.isClientSide) {
            return;
        }

        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);
        if (!state.is(tavernTrellis()) || state.getValue(ITrellis.TYPE) != TrellisType.SINGLE) {
            return;
        }

        BlockState below = level.getBlockState(pos.below());
        ItemStack stack = event.getItemStack();
        if (!stack.is(KSItems.ANCIENT_FRUIT_VINE.get()) || !below.is(BlockTags.DIRT)) {
            return;
        }

        event.setCanceled(true);

        Block planted = KSBlocks.ANCIENT_FRUIT_VINE.get();
        BlockState newState = planted.defaultBlockState()
                .setValue(ITrellis.TYPE, state.getValue(ITrellis.TYPE))
                .setValue(AncientFruitVineBlock.WATERLOGGED, state.getOptionalValue(BlockStateProperties.WATERLOGGED).orElse(false));
        level.setBlockAndUpdate(pos, newState);
        level.playSound(null, pos, SoundEvents.CROP_PLANTED, SoundSource.BLOCKS);

        if (!event.getEntity().isCreative()) {
            stack.shrink(1);
        }
        event.getEntity().swing(InteractionHand.MAIN_HAND);
    }

    /**
     * 获取酒馆棚架方块。
     *
     * @return 酒馆棚架方块
     */
    private static Block tavernTrellis() {
        return ModBlocks.TRELLIS.get();
    }
}