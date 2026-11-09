package com.tequilacat.kaleidoscope_startale.init;

import com.github.ysbbbbbb.kaleidoscopetavern.init.ModBlocks;
import com.tequilacat.kaleidoscope_startale.KaleidoscopeStartale;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BlockEntityTypeAddBlocksEvent;

@EventBusSubscriber(modid = KaleidoscopeStartale.MODID)
public class KSBlockEntityTypes {

    @SubscribeEvent
    public static void addValidBlocks(BlockEntityTypeAddBlocksEvent event) {
        event.modify(ModBlocks.DRINK_BE.get(),
                KSBlocks.ANCIENT_FRUIT_WINE.get(),
                KSBlocks.REFRESHING_BEER.get(),
                KSBlocks.YANJING_U7.get(),
                KSBlocks.GRAPE_FLAVORED_WINE.get(),
                KSBlocks.JOJA_COLA.get(),
                KSBlocks.RED_EYE_WINE.get(),
                KSBlocks.RHUBARB_JAM.get());
    }
}
