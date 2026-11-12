package com.tequilacat.kaleidoscope_startale.data.tags;

import com.github.ysbbbbbb.kaleidoscopecookery.init.ModItems;
import com.github.ysbbbbbb.kaleidoscopetavern.init.tag.TagMod;
import com.tequilacat.kaleidoscope_startale.KaleidoscopeStartale;
import com.tequilacat.kaleidoscope_startale.init.KSItemTags;
import com.tequilacat.kaleidoscope_startale.init.KSItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;
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

        // ==================== 模组本体 ====================
        tag(KSItemTags.LIVESTOCK_FEED).add(KSItems.LIVESTOCK_FEED.get());
        tag(KSItemTags.POULTRY_FEED).add(KSItems.POULTRY_FEED.get());
        tag(KSItemTags.UNIVERSAL_FEED).add(KSItems.UNIVERSAL_FEED.get());
        tag(KSItemTags.LOOTING_APPLICABLE)
                .add(KSItems.PETTER.get())
                .addOptionalTag(ResourceLocation.fromNamespaceAndPath("minecraft", "enchantable/sword"));
        tag(KSItemTags.TAMED_DATA_DISPLAY_HAT)
                .add(ModItems.STRAW_HAT.get())
                .add(ModItems.STRAW_HAT_FLOWER.get());


        // ==================== 原版 ====================
        tag(ItemTags.VILLAGER_PLANTABLE_SEEDS)
                .add(KSItems.RHUBARB_SEEDS.get())
                .add(KSItems.AMARANTH_SEEDS.get());
        tag(TagKey.create(
                Registries.ITEM,
                ResourceLocation.fromNamespaceAndPath("minecraft", "enchantable/durability")
        )).add(KSItems.PETTER.get());
        tag(TagKey.create(
                Registries.ITEM,
                ResourceLocation.fromNamespaceAndPath("minecraft", "enchantable/sharp_weapon")
        )).add(KSItems.PETTER.get());


        // ==================== neoforge ====================
        tag(Tags.Items.CROPS)
                .add(KSItems.AMARANTH.get())
                .add(KSItems.ANCIENT_FRUIT.get())
                .add(KSItems.RHUBARB.get());
        tag(KSItemTags.CROPS_AMARANTH).add(KSItems.AMARANTH.get());
        tag(KSItemTags.CROPS_ANCIENT_FRUIT).add(KSItems.ANCIENT_FRUIT.get());
        tag(KSItemTags.CROPS_RHUBARB).add(KSItems.RHUBARB.get());
        tag(Tags.Items.FOODS)
                .add(KSItems.RHUBARB.get())
                .add(KSItems.ANCIENT_FRUIT.get())
                .add(KSItems.RHUBARB_JAM.get())
                .add(KSItems.ANCIENT_FRUIT_PLATTER.get())
                .add(KSItems.AMARANTH_SOUP.get())
                .add(KSItems.ENCHANTED_NIGHTCRAWLER.get())
                .add(KSItems.ANCIENT_FRUIT_JUICE_BUCKET.get())
                .add(KSItems.ANCIENT_FRUIT_WINE.get())
                .add(KSItems.REFRESHING_BEER.get())
                .add(KSItems.YANJING_U7.get());
        tag(KSItemTags.FRUITS).add(KSItems.ANCIENT_FRUIT.get());
        tag(KSItemTags.GRAIN).add(KSItems.AMARANTH.get());
        tag(KSItemTags.VEGETABLES).add(KSItems.RHUBARB.get());
        tag(Tags.Items.SEEDS)
                .add(KSItems.RHUBARB_SEEDS.get())
                .add(KSItems.AMARANTH_SEEDS.get());
        tag(Tags.Items.FOODS_VEGETABLE).add(KSItems.RHUBARB.get());
        tag(Tags.Items.FOODS_SOUP).add(KSItems.AMARANTH_SOUP.get());
        tag(Tags.Items.FOODS_RAW_FISH).add(KSItems.GOLDFISH.get());


        // ==================== Diet ====================
        tag(KSItemTags.DIET_FRUITS).addTag(KSItemTags.FRUITS);
        tag(KSItemTags.DIET_GRAINS).add(KSItems.AMARANTH.get());
        tag(KSItemTags.DIET_PROTEINS).add(KSItems.ENCHANTED_NIGHTCRAWLER.get());
        tag(KSItemTags.DIET_VEGETABLES).addTag(KSItemTags.VEGETABLES);


        // ==================== 森罗物语：厨房 ====================
        tag(com.github.ysbbbbbb.kaleidoscopecookery.init.tag.TagMod.CATERPILLARS).addTag(KSItemTags.VEGETABLES);


        // ==================== 森罗物语：酒馆 ====================
        // 黑色
        tag(TagMod.COCKTAIL_INGREDIENT_BLACK);
        // 深蓝色
        tag(TagMod.COCKTAIL_INGREDIENT_DARK_BLUE);
        // 深绿色
        tag(TagMod.COCKTAIL_INGREDIENT_DARK_GREEN);
        // 深青色
        tag(TagMod.COCKTAIL_INGREDIENT_DARK_AQUA);
        // 深红色
        tag(TagMod.COCKTAIL_INGREDIENT_DARK_RED);
        // 深紫色
        tag(TagMod.COCKTAIL_INGREDIENT_DARK_PURPLE);
        // 金色
        tag(TagMod.COCKTAIL_INGREDIENT_GOLD)
                .add(KSItems.YANJING_U7.get());
        // 灰色
        tag(TagMod.COCKTAIL_INGREDIENT_GRAY);
        // 深灰色
        tag(TagMod.COCKTAIL_INGREDIENT_DARK_GRAY);
        // 蓝色
        tag(TagMod.COCKTAIL_INGREDIENT_BLUE)
                .add(KSItems.ANCIENT_FRUIT_VINE.get());
        // 绿色
        tag(TagMod.COCKTAIL_INGREDIENT_GREEN)
                .add(KSItems.REFRESHING_BEER.get());
        // 青色
        tag(TagMod.COCKTAIL_INGREDIENT_AQUA);
        // 红色
        tag(TagMod.COCKTAIL_INGREDIENT_RED)
                .add(KSItems.RED_EYE_WINE.get());
        // 浅紫色
        tag(TagMod.COCKTAIL_INGREDIENT_LIGHT_PURPLE)
                .add(KSItems.GRAPE_FLAVORED_WINE.get());
        // 黄色
        tag(TagMod.COCKTAIL_INGREDIENT_YELLOW);
        // 白色
        tag(TagMod.COCKTAIL_INGREDIENT_WHITE);











    }
}
