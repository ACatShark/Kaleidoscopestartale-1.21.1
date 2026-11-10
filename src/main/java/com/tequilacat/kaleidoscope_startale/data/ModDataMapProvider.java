package com.tequilacat.kaleidoscope_startale.data;

import com.tequilacat.kaleidoscope_startale.init.KSItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.DataMapProvider;
import net.neoforged.neoforge.registries.datamaps.builtin.Compostable;
import net.neoforged.neoforge.registries.datamaps.builtin.FurnaceFuel;
import net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps;

import java.util.concurrent.CompletableFuture;

@SuppressWarnings("unused")
public class ModDataMapProvider extends DataMapProvider {

    protected ModDataMapProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(packOutput, lookupProvider);
    }

    @Override
    @SuppressWarnings({"deprecation", "removal"})
    protected void gather() {

        // 堆肥
        builder(NeoForgeDataMaps.COMPOSTABLES)
                .add(KSItems.RHUBARB_SEEDS.get().builtInRegistryHolder(), new Compostable(0.3F), false)
                .add(KSItems.AMARANTH_SEEDS.get().builtInRegistryHolder(), new Compostable(0.3F), false)
                .add(KSItems.ANCIENT_FRUIT_VINE.get().builtInRegistryHolder(), new Compostable(0.3F), false)
                .add(KSItems.RHUBARB.get().builtInRegistryHolder(), new Compostable(0.65F), false)
                .add(KSItems.ANCIENT_FRUIT.get().builtInRegistryHolder(), new Compostable(0.65F), false)
                .add(KSItems.AMARANTH.get().builtInRegistryHolder(), new Compostable(0.65F), false)
                .add(KSItems.ENCHANTED_NIGHTCRAWLER.get().builtInRegistryHolder(), new Compostable(1.0F), false);

        // 熔炉燃料
        builder(NeoForgeDataMaps.FURNACE_FUELS)
                .add(KSItems.ANCIENT_FRUIT_VINE.get().builtInRegistryHolder(), new FurnaceFuel(200), false);

    }
}
