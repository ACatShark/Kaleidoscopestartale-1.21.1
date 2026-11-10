package com.tequilacat.kaleidoscope_startale.data;

import com.tequilacat.kaleidoscope_startale.init.KSBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

public class ModBlockLootTablesProvider extends BlockLootSubProvider {
    public ModBlockLootTablesProvider(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    protected void generate() {
        dropSelf(KSBlocks.COLD_STOVE.get());
        dropSelf(KSBlocks.CREAM_OIL.get());
    }

    @Override
    protected @NotNull Iterable<Block> getKnownBlocks() {
        return List.of(
                KSBlocks.COLD_STOVE.get(),
                KSBlocks.CREAM_OIL.get()
        );
    }
}
