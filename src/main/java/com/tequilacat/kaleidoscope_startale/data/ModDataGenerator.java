package com.tequilacat.kaleidoscope_startale.data;

import com.tequilacat.kaleidoscope_startale.KaleidoscopeStartale;
import com.tequilacat.kaleidoscope_startale.data.recipe.KSRecipeProvider;
import com.tequilacat.kaleidoscope_startale.data.tags.ModEntityTagsProvider;
import com.tequilacat.kaleidoscope_startale.data.tags.ModItemTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

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
//        generator.addProvider(event.includeServer(), new ModBlockTagsProvider(packOutput, lookupProvider, existingFileHelper));
        generator.addProvider(event.includeClient(), new ModItemModelsProvider(packOutput, event.getExistingFileHelper()));
//        generator.addProvider(event.includeServer(), new LootTableProvider(packOutput, Collections.emptySet(),
//                List.of(new LootTableProvider.SubProviderEntry(ModBlockLootTablesProvider::new, LootContextParamSets.BLOCK)), lookupProvider));
        generator.addProvider(event.includeServer(), new KSRecipeProvider(packOutput, lookupProvider));
    }
}
