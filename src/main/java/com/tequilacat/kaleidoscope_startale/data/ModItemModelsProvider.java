package com.tequilacat.kaleidoscope_startale.data;

import com.github.ysbbbbbb.kaleidoscopecookery.init.registry.TeacupRegistry;
import com.tequilacat.kaleidoscope_startale.KaleidoscopeStartale;
import com.tequilacat.kaleidoscope_startale.init.KSBlocks;
import com.tequilacat.kaleidoscope_startale.init.KSItems;
import com.tequilacat.kaleidoscope_startale.init.KSTeacups;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModItemModelsProvider extends ItemModelProvider {
    public ModItemModelsProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, KaleidoscopeStartale.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        // ==================== 作物 ====================
        cropItem(KSItems.AMARANTH.get());
        cropItem(KSItems.AMARANTH_SEEDS.get());
        cropItem(KSItems.RHUBARB.get());
        cropItem(KSItems.RHUBARB_SEEDS.get());
        cropItem(KSItems.ANCIENT_FRUIT_VINE.get());
        cropItem(KSItems.ANCIENT_FRUIT.get());

        // ==================== 食物 ====================
        foodItem(KSItems.GOLDFISH.get());
        foodItem(KSItems.MATCHA_BB.get());
        foodItem(KSItems.MATCHA_PARFAIT.get());
        foodItem(KSItems.MATCHA_CAKE_ROLL.get());
        foodItem(KSItems.MATCHA_CAKE_SLICE.get());
        foodItem(KSItems.MATCHA_CAKE.get());
        foodItem(KSItems.ENCHANTED_NIGHTCRAWLER.get());

        // ==================== 加工食品 ====================
        brewItem(KSItems.RHUBARB_JAM.get());
        foodItem(KSItems.ANCIENT_FRUIT_PLATTER.get());
        foodItem(KSItems.AMARANTH_SOUP.get());
        foodItem(KSItems.FISH_MUSHROOM_SOUP.get());
        foodItem(KSItems.GLOWING_CATERPILLAR_SOUP.get());
        foodItem(KSItems.RHUBARB_FONDANT_PIE.get());
        foodItem(KSItems.OMELETTE_RICE.get());
//        foodItem(KSItems.ANCIENT_FRUIT_SKEWER.get());
        foodItem(KSItems.EGG_BAKED_CAKE.get());
        foodItem(KSItems.IDEA_COOKIE.get());
        foodItem(KSItems.INSPIRATION_MUSHROOM.get());
        foodItem(KSItems.SLIME_KING_FLAVORED_JELLY.get());

        // ==================== 酒水 ====================
        brewItem(KSItems.ANCIENT_FRUIT_WINE.get());
        brewItem(KSItems.REFRESHING_BEER.get());
        brewItem(KSItems.YANJING_U7.get());
        brewItem(KSItems.GRAPE_FLAVORED_WINE.get());
        brewItem(KSItems.RED_EYE_WINE.get());
        brewItem(KSItems.JOJA_COLA.get());

        // ==================== 鸡尾酒 ====================
        brewItem(KSItems.ICE_ELF_COCKTAIL.get());
        brewItem(KSItems.BUBBLE_ECHO_COCKTAIL.get());
        brewItem(KSItems.SPRING_DRINK_COCKTAIL.get());
        brewItem(KSItems.SPRING_DRINK_1_COCKTAIL.get());

        // ==================== 果汁 ====================
        juiceItem(KSItems.BERRY_JUICE.get());
        juiceItem(KSItems.GLOW_BERRY_JUICE.get());
        juiceItem(KSItems.GRAPE_JUICE.get());
        juiceItem(KSItems.GOLD_GRAPE_JUICE.get());
        juiceItem(KSItems.ICE_GRAPE_JUICE.get());
        juiceItem(KSItems.GREEN_GRAPE_JUICE.get());
        juiceItem(KSItems.ANCIENT_FRUIT_JUICE_BUCKET.get());

        // ==================== 材料 ====================
        basicItem(KSItems.LIVESTOCK_FEED.get());
        basicItem(KSItems.POULTRY_FEED.get());
        basicItem(KSItems.UNIVERSAL_FEED.get());
        basicItem(KSItems.MATCHA_POWDER.get());
        basicItem(KSItems.CREAM.get());

        // ====================  工具 ====================
//        basicItem(KSItems.JUICER.get());
//        basicItem(KSItems.PETTER.get());
        simpleBlockItem(KSBlocks.COLD_STOVE.get());
        basicItem(KSItems.YUNOMI.get());
        basicItem(KSItems.CREAM_OIL.get());

        // ==================== 茶叶 ====================
        basicItem(KSItems.GREEN_TEA_LEAVES.get());

        // ==================== 茶包 ====================
        bagItem(KSItems.ROSE_TEA_BAG.get());

        // ==================== 茶 ====================
        teaItem(TeacupRegistry.getItem(KSTeacups.MATCHA_TEA));
        teaItem(TeacupRegistry.getItem(KSTeacups.ROSE_TEA));

    }

    private void cropItem(Item item) {
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(item);
        String itemName = key.getPath();
        ItemModelBuilder builder = withExistingParent(itemName, ResourceLocation.withDefaultNamespace("item/generated"));
        builder.texture("layer0", KaleidoscopeStartale.id("item/crop/" + itemName));
    }

    private void brewItem(Item item) {
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(item);
        String itemName = key.getPath();
        ItemModelBuilder builder = withExistingParent(itemName, ResourceLocation.withDefaultNamespace("item/generated"));
        builder.texture("layer0", KaleidoscopeStartale.id("item/brew/" + itemName));
    }

    private void juiceItem(Item item) {
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(item);
        String itemName = key.getPath();
        ItemModelBuilder builder = withExistingParent(itemName, ResourceLocation.withDefaultNamespace("item/generated"));
        builder.texture("layer0", KaleidoscopeStartale.id("item/juice/" + itemName));
    }

    private void teaItem(Item item) {
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(item);
        String itemName = key.getPath();
        ItemModelBuilder builder = withExistingParent(itemName, ResourceLocation.withDefaultNamespace("item/generated"));
        builder.texture("layer0", KaleidoscopeStartale.id("item/tea/" + itemName));
    }

    private void bagItem(Item item) {
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(item);
        String itemName = key.getPath();
        ItemModelBuilder builder = withExistingParent(itemName, ResourceLocation.withDefaultNamespace("item/generated"));
        builder.texture("layer0", KaleidoscopeStartale.id("item/tea_bag/" + itemName));
    }

    private void foodItem(Item item) {
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(item);
        String itemName = key.getPath();
        ItemModelBuilder builder = withExistingParent(itemName, ResourceLocation.withDefaultNamespace("item/generated"));
        builder.texture("layer0", KaleidoscopeStartale.id("item/food/" + itemName));
    }
}
