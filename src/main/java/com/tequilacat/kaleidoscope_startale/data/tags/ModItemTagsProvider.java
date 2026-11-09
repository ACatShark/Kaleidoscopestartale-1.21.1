package com.tequilacat.kaleidoscope_startale.data.tags;

import com.tequilacat.kaleidoscope_startale.KaleidoscopeStartale;
import com.tequilacat.kaleidoscope_startale.init.KSItemTags;
import com.tequilacat.kaleidoscope_startale.init.KSItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModItemTagsProvider extends ItemTagsProvider {
    public ModItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider,
                               CompletableFuture<TagLookup<Block>> blockTags, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, blockTags, KaleidoscopeStartale.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider provider) {

        tag(KSItemTags.LIVESTOCK_FEED).add(KSItems.LIVESTOCK_FEED.get());
        tag(KSItemTags.POULTRY_FEED).add(KSItems.POULTRY_FEED.get());
        tag(KSItemTags.UNIVERSAL_FEED).add(KSItems.UNIVERSAL_FEED.get());
    }
}
