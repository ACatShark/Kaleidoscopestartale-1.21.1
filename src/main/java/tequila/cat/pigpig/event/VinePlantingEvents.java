package tequila.cat.pigpig.event;

import com.github.ysbbbbbb.kaleidoscopetavern.block.plant.ITrellis;
import com.github.ysbbbbbb.kaleidoscopetavern.block.properties.TrellisType;

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

import tequila.cat.pigpig.block.ModVineBlock;
import tequila.cat.pigpig.init.ModBlocks;
import tequila.cat.pigpig.init.ModItems;

/**
 * 手持「上古水果藤」右键 tavern 的空藤架（单格藤架且下方是泥土）→ 就地种下。
 */
public class VinePlantingEvents {
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
        if (!stack.is(ModItems.ANCIENT_FRUIT_VINE.get()) || !below.is(BlockTags.DIRT)) {
            return;
        }

        event.setCanceled(true);

        Block planted = ModBlocks.ANCIENT_FRUIT_VINE.get();
        BlockState newState = planted.defaultBlockState()
                .setValue(ITrellis.TYPE, state.getValue(ITrellis.TYPE))
                .setValue(ModVineBlock.WATERLOGGED,
                        state.getOptionalValue(BlockStateProperties.WATERLOGGED).orElse(false));
        level.setBlockAndUpdate(pos, newState);
        level.playSound(null, pos, SoundEvents.CROP_PLANTED, SoundSource.BLOCKS);

        if (!event.getEntity().isCreative()) {
            stack.shrink(1);
        }
        event.getEntity().swing(InteractionHand.MAIN_HAND);
    }

    private static Block tavernTrellis() {
        return com.github.ysbbbbbb.kaleidoscopetavern.init.ModBlocks.TRELLIS.get();
    }
}
