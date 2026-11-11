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
                .add(EntityType.COW) // 牛
                .add(EntityType.MOOSHROOM) // 哞菇
                .add(EntityType.PIG) // 猪
                .add(EntityType.SHEEP) // 羊
                .add(EntityType.GOAT) // 山羊
                .add(EntityType.SNIFFER); // 嗅探兽

        tag(KSEntityTags.POULTRY_FEED_MOBS)
                .add(EntityType.ARMADILLO) // 犰狳
                .add(EntityType.RABBIT) // 兔子
                .add(EntityType.CHICKEN) // 鸡
                .add(EntityType.PARROT); // 鹦鹉

        tag(KSEntityTags.UNIVERSAL_FEED_MOBS)
                .add(EntityType.COW) // 牛
                .add(EntityType.MOOSHROOM) // 哞菇
                .add(EntityType.PIG) // 猪
                .add(EntityType.SHEEP) // 羊
                .add(EntityType.GOAT) // 山羊
                .add(EntityType.SNIFFER) // 嗅探兽

                .add(EntityType.ARMADILLO) // 犰狳
                .add(EntityType.RABBIT) // 兔子
                .add(EntityType.CHICKEN) // 鸡
                .add(EntityType.PARROT); // 鹦鹉

    }
}
