package com.tequilacat.kaleidoscope_startale.data.tags;

import com.tequilacat.kaleidoscope_startale.KaleidoscopeStartale;
import com.tequilacat.kaleidoscope_startale.init.KSEntityTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

public class ModEntityTagsProvider extends EntityTypeTagsProvider {
    public ModEntityTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, KaleidoscopeStartale.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {

        tag(KSEntityTags.LIVESTOCK_FEED_MOBS)
                .add(EntityType.COW)
                .add(EntityType.MOOSHROOM)
                .add(EntityType.PIG)
                .add(EntityType.SHEEP)
                .add(EntityType.GOAT)
                .add(EntityType.SNIFFER);

        tag(KSEntityTags.POULTRY_FEED_MOBS)
                .add(EntityType.ARMADILLO)
                .add(EntityType.RABBIT)
                .add(EntityType.CHICKEN);

        tag(KSEntityTags.UNIVERSAL_FEED_BLACKLIST);

    }
}
