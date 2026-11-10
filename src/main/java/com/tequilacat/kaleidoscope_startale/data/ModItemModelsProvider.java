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
//        // ==================== 作物 ====================
//        basicItem(KSItems.AMARANTH_SEEDS.get());
//        basicItem(KSItems.RHUBARB_SEEDS.get());
//        basicItem(KSItems.RHUBARB.get());
//        basicItem(KSItems.AMARANTH.get());
//        basicItem(KSItems.ANCIENT_FRUIT_VINE.get());
//        basicItem(KSItems.ANCIENT_FRUIT.get());
//
//        // ==================== 食物 ====================
//        basicItem(KSItems.GOLDFISH.get());
//        basicItem(KSItems.ANCIENT_FRUIT_PLATTER.get());
//        basicItem(KSItems.ENCHANTED_NIGHTCRAWLER.get());
//        basicItem(KSItems.AMARANTH_SOUP.get());
//        basicItem(KSItems.FISH_MUSHROOM_SOUP.get());
//        basicItem(KSItems.GLOWING_CATERPILLAR_SOUP.get());
//        basicItem(KSItems.RHUBARB_FONDANT_PIE.get());
//        basicItem(KSItems.OMELETTE_RICE.get());
//        basicItem(KSItems.EGG_BAKED_CAKE.get());
//        basicItem(KSItems.IDEA_COOKIE.get());
//        basicItem(KSItems.INSPIRATION_MUSHROOM.get());
//        basicItem(KSItems.SLIME_KING_FLAVORED_JELLY.get());
//
//        // ==================== 加工食品 ====================
//        basicItem(KSItems.RHUBARB_JAM.get());
//
//        // ==================== 酒水 ====================
//        basicItem(KSItems.ANCIENT_FRUIT_WINE.get());
//        basicItem(KSItems.REFRESHING_BEER.get());
//        basicItem(KSItems.YANJING_U7.get());
//        basicItem(KSItems.GRAPE_FLAVORED_WINE.get());
//        basicItem(KSItems.RED_EYE_WINE.get());
//        basicItem(KSItems.JOJA_COLA.get());
//
//        // ==================== 鸡尾酒 ====================
//        basicItem(KSItems.ICE_ELF_COCKTAIL.get());
//        basicItem(KSItems.BUBBLE_ECHO_COCKTAIL.get());
//        basicItem(KSItems.SPRING_DRINK_COCKTAIL.get());
//        basicItem(KSItems.SPRING_DRINK_1_COCKTAIL.get());
//
//        // ==================== 果汁 ====================
//        basicItem(KSItems.BERRY_JUICE.get());
//        basicItem(KSItems.GLOW_BERRY_JUICE.get());
//        basicItem(KSItems.GRAPE_JUICE.get());
//        basicItem(KSItems.GOLD_GRAPE_JUICE.get());
//        basicItem(KSItems.ICE_GRAPE_JUICE.get());
//        basicItem(KSItems.GREEN_GRAPE_JUICE.get());
//        basicItem(KSItems.ANCIENT_FRUIT_JUICE_BUCKET.get());
//
//        // ==================== 材料 / 工具 ====================
//        basicItem(KSItems.LIVESTOCK_FEED.get());
//        basicItem(KSItems.POULTRY_FEED.get());
//        basicItem(KSItems.UNIVERSAL_FEED.get());

        // ==================== 厨具 ====================
        basicItem(KSItems.CREAM_OIL.get());
        basicItem(KSItems.YUNOMI.get());
        simpleBlockItem(KSBlocks.COLD_STOVE.get());

        // ==================== 材料 ====================
        basicItem(KSItems.MATCHA_POWDER.get());
        basicItem(KSItems.CREAM.get());

        // ==================== 食物 ====================
        foodItem(KSItems.MATCHA_BB.get());
        foodItem(KSItems.MATCHA_PARFAIT.get());
        foodItem(KSItems.MATCHA_CAKE_ROLL.get());
        foodItem(KSItems.MATCHA_CAKE_SLICE.get());
        foodItem(KSItems.MATCHA_CAKE.get());

        // ==================== 茶叶 ====================
        basicItem(KSItems.GREEN_TEA_LEAVES.get());

        // ==================== 茶包 ====================
        bagItem(KSItems.ROSE_TEA_BAG.get());

        // ==================== 茶 ====================
        teaItem(TeacupRegistry.getItem(KSTeacups.MATCHA_TEA));
        teaItem(TeacupRegistry.getItem(KSTeacups.ROSE_TEA));
    }

    /**
     * 茶（路径：item/tea）
     */
    private void teaItem(Item item) {
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(item);
        String itemName = key.getPath();
        ItemModelBuilder builder = withExistingParent(itemName, ResourceLocation.withDefaultNamespace("item/generated"));
        builder.texture("layer0", KaleidoscopeStartale.id("item/tea/" + itemName));
    }

    /**
     * 茶包（路径：item/teacup_bag/）
     */
    private void bagItem(Item item) {
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(item);
        String itemName = key.getPath();
        ItemModelBuilder builder = withExistingParent(itemName, ResourceLocation.withDefaultNamespace("item/generated"));
        builder.texture("layer0", KaleidoscopeStartale.id("item/tea_bag/" + itemName));
    }

    /**
     * 食物（路径：item/food）
     */
    private void foodItem(Item item) {
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(item);
        String itemName = key.getPath();
        ItemModelBuilder builder = withExistingParent(itemName, ResourceLocation.withDefaultNamespace("item/generated"));
        builder.texture("layer0", KaleidoscopeStartale.id("item/food/" + itemName));
    }
}
