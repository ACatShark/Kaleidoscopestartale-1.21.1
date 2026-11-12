package com.tequilacat.kaleidoscope_startale.init;

import com.tequilacat.kaleidoscope_startale.KaleidoscopeStartale;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public interface KSCreativeModeTabs {
    DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, KaleidoscopeStartale.MODID);

    DeferredHolder<CreativeModeTab, CreativeModeTab> STARTALE_TAB =
            CREATIVE_MODE_TABS.register("startale_tab", () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(KSItems.ANCIENT_FRUIT.get()))
                    .title(Component.translatable("itemGroup." + KaleidoscopeStartale.MODID))
                    .displayItems((parameters, output) -> KSCreativeSections.addItems(output))
                    .build());

    static void register(IEventBus bus) {
        CREATIVE_MODE_TABS.register(bus);
    }
}
