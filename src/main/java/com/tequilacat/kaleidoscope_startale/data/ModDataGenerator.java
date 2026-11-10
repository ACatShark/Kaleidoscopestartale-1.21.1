package com.tequilacat.kaleidoscope_startale.data;

import com.tequilacat.kaleidoscope_startale.KaleidoscopeStartale;
import com.tequilacat.kaleidoscope_startale.data.loot.ModFishingLootTablesProvider;
import com.tequilacat.kaleidoscope_startale.data.loot.ModGlobalLootModifiersProvider;
import com.tequilacat.kaleidoscope_startale.data.loot.ModLootTablesProvider;
import com.tequilacat.kaleidoscope_startale.data.registries.ModDatapackRegistriesProvider;
import com.tequilacat.kaleidoscope_startale.data.tags.ModBlockTagsProvider;
import com.tequilacat.kaleidoscope_startale.data.tags.ModEntityTagsProvider;
import com.tequilacat.kaleidoscope_startale.data.tags.ModItemTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = KaleidoscopeStartale.MODID)
public class ModDataGenerator {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        generator.addProvider(event.includeServer(), new ModItemTagsProvider(packOutput, lookupProvider,
                CompletableFuture.completedFuture(TagsProvider.TagLookup.empty()), existingFileHelper));
        generator.addProvider(event.includeServer(), new ModEntityTagsProvider(packOutput, lookupProvider, existingFileHelper));
        generator.addProvider(event.includeServer(), new ModBlockTagsProvider(packOutput, lookupProvider, existingFileHelper));
        generator.addProvider(event.includeClient(), new ModItemModelsProvider(packOutput, event.getExistingFileHelper()));
        generator.addProvider(event.includeServer(), new LootTableProvider(packOutput, Collections.emptySet(), List.of(
                new LootTableProvider.SubProviderEntry(ModBlockLootTablesProvider::new, LootContextParamSets.BLOCK),
                new LootTableProvider.SubProviderEntry(ModLootTablesProvider::new, LootContextParamSets.BLOCK),
                new LootTableProvider.SubProviderEntry(ignored -> new ModFishingLootTablesProvider(), LootContextParamSets.FISHING)
        ), lookupProvider));
        generator.addProvider(event.includeServer(), new ModDataMapProvider(packOutput, lookupProvider));
        generator.addProvider(event.includeServer(), new ModGlobalLootModifiersProvider(packOutput, lookupProvider));
        generator.addProvider(event.includeServer(), new ModRecipesProvider(packOutput, lookupProvider));
        generator.addProvider(event.includeServer(), new ModDatapackRegistriesProvider(packOutput, lookupProvider));
    }
}
