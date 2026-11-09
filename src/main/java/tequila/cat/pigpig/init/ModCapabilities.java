package tequila.cat.pigpig.init;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.capability.wrappers.FluidBucketWrapper;

import tequila.cat.pigpig.KaleidoscopeStartale;

@EventBusSubscriber(modid = KaleidoscopeStartale.MODID)
public final class ModCapabilities {
    @SubscribeEvent
    public static void registerItemHandlers(RegisterCapabilitiesEvent event) {
        // 上古水果汁桶：可被任意流体容器抽取 / 灌入
        event.registerItem(Capabilities.FluidHandler.ITEM,
                (stack, ctx) -> new FluidBucketWrapper(stack),
                ModItems.ANCIENT_FRUIT_JUICE_BUCKET.get());

        // 榨汁机：物品槽 + 内部储罐
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.JUICER.get(), (juicer, side) -> juicer.getItems());
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK,
                ModBlockEntities.JUICER.get(), (juicer, side) -> juicer.getTank());
    }

    private ModCapabilities() {
    }
}
