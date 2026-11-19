package com.tequilacat.kaleidoscope_startale.init;

import com.mojang.serialization.MapCodec;
import com.tequilacat.kaleidoscope_startale.KaleidoscopeStartale;
import com.tequilacat.kaleidoscope_startale.data.loot.ChestLootModifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public interface KSLootModifiers {
    DeferredRegister<MapCodec<? extends IGlobalLootModifier>> SERIALIZERS = DeferredRegister.create(
            NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, KaleidoscopeStartale.MODID);

    /** 古城箱子掉落上古水果藤。 */
    DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<ChestLootModifier>>
            ANCIENT_FRUIT_CHEST = SERIALIZERS.register("ancient_fruit_chest",
            () -> ChestLootModifier.CODEC);

    static void register(IEventBus bus) {
        SERIALIZERS.register(bus);
    }
}
