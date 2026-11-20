package com.tequilacat.kaleidoscope_startale.init;

import com.tequilacat.kaleidoscope_startale.KaleidoscopeStartale;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public interface KSDataComponents {
    DeferredRegister<DataComponentType<?>> DATA_COMPONENTS = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, KaleidoscopeStartale.MODID);

    /** 附魔升级次数。 */
    DeferredHolder<DataComponentType<?>, DataComponentType<ItemEnchantments>> ENCHANT_UPGRADES =
            DATA_COMPONENTS.register("enchant_upgrades", () -> DataComponentType.<ItemEnchantments>builder()
                    .persistent(ItemEnchantments.CODEC)
                    .networkSynchronized(ItemEnchantments.STREAM_CODEC)
                    .build());

    static void register(IEventBus bus) {
        DATA_COMPONENTS.register(bus);
    }
}
