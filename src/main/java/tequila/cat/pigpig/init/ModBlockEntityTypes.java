package tequila.cat.pigpig.init;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BlockEntityTypeAddBlocksEvent;

import tequila.cat.pigpig.KaleidoscopeStartale;

/**
 * 让 tavern 的 DrinkBlockEntity 也接受本模组的酒瓶方块，
 * 否则放置后无法保存「已倒出层数」等状态。
 */
@EventBusSubscriber(modid = KaleidoscopeStartale.MODID)
public final class ModBlockEntityTypes {
    @SubscribeEvent
    public static void addValidBlocks(BlockEntityTypeAddBlocksEvent event) {
        event.modify(com.github.ysbbbbbb.kaleidoscopetavern.init.ModBlocks.DRINK_BE.get(),
                ModBlocks.ANCIENT_FRUIT_WINE.get(),
                ModBlocks.REFRESHING_BEER.get(),
                ModBlocks.YANJING_U7.get(),
                ModBlocks.GRAPE_FLAVORED_WINE.get(),
                ModBlocks.JOJA_COLA.get(),
                ModBlocks.RED_EYE_WINE.get(),
                ModBlocks.RHUBARB_JAM.get());
    }

    private ModBlockEntityTypes() {
    }
}
