package com.tequilacat.kaleidoscope_startale.data.tags;

import com.tequilacat.kaleidoscope_startale.KaleidoscopeStartale;
import com.tequilacat.kaleidoscope_startale.init.KSBlockTags;
import com.tequilacat.kaleidoscope_startale.init.KSBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

public class ModBlockTagsProvider extends BlockTagsProvider {

    public ModBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, KaleidoscopeStartale.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider provider) {

        // ==================== 原版 ====================
        tag(BlockTags.MINEABLE_WITH_AXE)
                .add(KSBlocks.ANCIENT_FRUIT_VINE.get());
        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(KSBlocks.COLD_STOVE.get());
        tag(Tags.Blocks.NEEDS_WOOD_TOOL)
                .add(KSBlocks.COLD_STOVE.get())
                .add(KSBlocks.ANCIENT_FRUIT_VINE.get());
        tag(BlockTags.CROPS)
                .add(KSBlocks.RHUBARB_CROP.get())
                .add(KSBlocks.AMARANTH_CROP.get())
                .add(KSBlocks.ANCIENT_FRUIT_CROP.get());


        // ==================== Carry On ====================
        tag(KSBlockTags.CARRY_ON_BLACKLIST)
                .add(KSBlocks.RHUBARB_CROP.get())
                .add(KSBlocks.AMARANTH_CROP.get())
                .add(KSBlocks.ANCIENT_FRUIT_CROP.get())
                .add(KSBlocks.ANCIENT_FRUIT_VINE.get());


        // ==================== 节气 ====================
        tag(KSBlockTags.CROPS_AVERAGE_MOIST)
                .add(KSBlocks.RHUBARB_CROP.get())
                .add(KSBlocks.AMARANTH_CROP.get())
                .add(KSBlocks.ANCIENT_FRUIT_CROP.get())
                .add(KSBlocks.ANCIENT_FRUIT_VINE.get());


        // ==================== 静谧四季 ====================
        tag(KSBlockTags.AUTUMN_CROPS_BLOCK)
                .add(KSBlocks.AMARANTH_CROP.get())
                .add(KSBlocks.ANCIENT_FRUIT_CROP.get())
                .add(KSBlocks.ANCIENT_FRUIT_VINE.get());
        tag(KSBlockTags.SPRING_CROPS_BLOCK)
                .add(KSBlocks.RHUBARB_CROP.get())
                .add(KSBlocks.ANCIENT_FRUIT_VINE.get());
        tag(KSBlockTags.SUMMER_CROPS_BLOCK)
                .add(KSBlocks.AMARANTH_CROP.get())
                .add(KSBlocks.ANCIENT_FRUIT_CROP.get())
                .add(KSBlocks.ANCIENT_FRUIT_VINE.get());

    }
}
