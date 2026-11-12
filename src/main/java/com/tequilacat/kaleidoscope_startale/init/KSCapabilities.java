package com.tequilacat.kaleidoscope_startale.init;

import com.tequilacat.kaleidoscope_startale.KaleidoscopeStartale;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.capability.wrappers.FluidBucketWrapper;

@EventBusSubscriber(modid = KaleidoscopeStartale.MODID)
public interface KSCapabilities {

    @SubscribeEvent
    static void registerItemHandlers(RegisterCapabilitiesEvent event) {
        // 上古水果汁桶：可被任意流体容器抽取 / 灌入
        event.registerItem(Capabilities.FluidHandler.ITEM,
                (stack, ctx) -> new FluidBucketWrapper(stack),
                KSItems.ANCIENT_FRUIT_JUICE_BUCKET.get());
    }
}
