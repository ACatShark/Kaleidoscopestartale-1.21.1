package com.tequilacat.kaleidoscope_startale.init;

import com.github.ysbbbbbb.kaleidoscopecookery.item.BowlFoodBlockItem;
import com.github.ysbbbbbb.kaleidoscopetavern.item.CocktailBlockItem;
import com.github.ysbbbbbb.kaleidoscopetavern.item.JuiceBucketItem;
import com.tequilacat.kaleidoscope_startale.KaleidoscopeStartale;
import com.tequilacat.kaleidoscope_startale.item.*;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public interface KSItems {
    DeferredRegister.Items ITEMS = DeferredRegister.createItems(KaleidoscopeStartale.MODID);

    // ==================== 作物 ====================
    /** 苋菜种子 */
    DeferredItem<SeedItem> AMARANTH_SEEDS = ITEMS.register("amaranth_seeds", () -> new SeedItem(KSBlocks.AMARANTH_CROP.get(), new Item.Properties()));
    /** 大黄种子 */
    DeferredItem<SeedItem> RHUBARB_SEEDS = ITEMS.register("rhubarb_seeds", () -> new SeedItem(KSBlocks.RHUBARB_CROP.get(), new Item.Properties()));
    /** 大黄 */
    DeferredItem<Item> RHUBARB = ITEMS.register("rhubarb", () -> new Item(new Item.Properties().food(KSFoods.RHUBARB)));
    /** 苋菜 */
    DeferredItem<Item> AMARANTH = ITEMS.register("amaranth", () -> new Item(new Item.Properties()));
    /** 上古水果藤 */
    DeferredItem<Item> ANCIENT_FRUIT_VINE = ITEMS.register("ancient_fruit_vine", () -> new Item(new Item.Properties()));
    /** 上古水果 */
    DeferredItem<Item> ANCIENT_FRUIT = ITEMS.register("ancient_fruit", () -> new Item(new Item.Properties().food(KSFoods.ANCIENT_FRUIT)));


    // ==================== 食物 ====================
    /** 金鱼 */
    DeferredItem<Item> GOLDFISH = ITEMS.register("goldfish", () -> new Item(new Item.Properties().food(KSFoods.GOLDFISH)));
    /** 抹茶棒冰 */
    DeferredItem<Item> MATCHA_BB = ITEMS.register("matcha_bb", () -> new Item(new Item.Properties().food(KSFoods.POPSICLE)));
    /** 抹茶芭菲 */
    DeferredItem<Item> MATCHA_PARFAIT = ITEMS.register("matcha_parfait", () -> new Item(new Item.Properties().food(KSFoods.PARFAIT)));
    /** 抹茶蛋糕卷 */
    DeferredItem<Item> MATCHA_CAKE_ROLL = ITEMS.register("matcha_cake_roll", () -> new Item(new Item.Properties().food(KSFoods.CAKE_ROLL)));
    /** 抹茶蛋糕切片 */
    DeferredItem<Item> MATCHA_CAKE_SLICE = ITEMS.register("matcha_cake_slice", () -> new Item(new Item.Properties().food(KSFoods.CAKE_SLICE)));
    /** 抹茶蛋糕 */
    DeferredItem<BlockItem> MATCHA_CAKE = ITEMS.register("matcha_cake", () -> new BlockItem(KSBlocks.MATCHA_CAKE.get(), new Item.Properties().stacksTo(1)));
    /** 附魔夜行者 */
    DeferredItem<EffectFoodItem> ENCHANTED_NIGHTCRAWLER = ITEMS.register("enchanted_nightcrawler",
            () -> new EffectFoodItem(new Item.Properties().food(KSFoods.ENCHANTED_NIGHTCRAWLER_FOOD)));


    // ==================== 加工食品 ====================
    /** 大黄酱 */
    DeferredItem<RhubarbJamItem> RHUBARB_JAM = ITEMS.register("rhubarb_jam",
            () -> new RhubarbJamItem(KSBlocks.RHUBARB_JAM.get(), new Item.Properties().stacksTo(16).food(KSFoods.RHUBARB_JAM_FOOD)));
    /** 上古水果拼盘 */
    DeferredItem<EffectFoodItem> ANCIENT_FRUIT_PLATTER = ITEMS.register("ancient_fruit_platter",
            () -> new EffectFoodItem(new Item.Properties().food(KSFoods.ANCIENT_FRUIT_PLATTER_FOOD)));
    /** 苋菜汤 */
    DeferredItem<EffectFoodItem> AMARANTH_SOUP = ITEMS.register("amaranth_soup",
            () -> new EffectFoodItem(new Item.Properties().stacksTo(1).food(KSFoods.AMARANTH_SOUP_FOOD)));
    /** 鱼菇煲 */
    DeferredItem<BowlFoodBlockItemWithTooltip> FISH_MUSHROOM_SOUP = registerBowlFoodWithTooltip("fish_mushroom_soup",
            KSBlocks.FISH_MUSHROOM_SOUP, KSFoods.FISH_MUSHROOM_SOUP_ITEM_FOOD, "tooltip.kaleidoscope_startale.fish_mushroom_soup.maxim");
    /** 闪亮猪儿虫浓汤 */
    DeferredItem<BowlFoodBlockItem> GLOWING_CATERPILLAR_SOUP = registerBowlFood("glowing_caterpillar_soup",
            KSBlocks.GLOWING_CATERPILLAR_SOUP, KSFoods.GLOWING_CATERPILLAR_SOUP_ITEM_FOOD);
    /** 大黄翻糖派 */
    DeferredItem<BowlFoodBlockItem> RHUBARB_FONDANT_PIE = registerBowlFood("rhubarb_fondant_pie",
            KSBlocks.RHUBARB_FONDANT_PIE, KSFoods.RHUBARB_FONDANT_PIE_ITEM_FOOD);
    /** 蛋包饭 */
    DeferredItem<BowlFoodBlockItem> OMELETTE_RICE = registerBowlFood("omelette_rice",
            KSBlocks.OMELETTE_RICE, KSFoods.OMELETTE_RICE_ITEM_FOOD);
    /** 上古水果串 */
    DeferredItem<Item> ANCIENT_FRUIT_SKEWER = ITEMS.register("ancient_fruit_skewer",
            () -> new Item(new Item.Properties().food(KSFoods.ANCIENT_FRUIT_SKEWER_FOOD)));
    /** 蛋烘糕 */
    DeferredItem<EffectFoodItem> EGG_BAKED_CAKE = ITEMS.register("egg_baked_cake",
            () -> new EffectFoodItem(new Item.Properties().food(KSFoods.EGG_BAKED_CAKE_FOOD)));
    /** IntelliJ IDEA 饼干 */
    DeferredItem<EffectFoodItem> IDEA_COOKIE = ITEMS.register("idea_cookie",
            () -> new EffectFoodItem(new Item.Properties().food(KSFoods.IDEA_COOKIE_FOOD)));
    /** 灵感菇 */
    DeferredItem<EffectFoodItem> INSPIRATION_MUSHROOM = ITEMS.register("inspiration_mushroom",
            () -> new EffectFoodItem(new Item.Properties().food(KSFoods.INSPIRATION_MUSHROOM_FOOD)));
    /** 史莱姆王风味果冻 */
    DeferredItem<Item> SLIME_KING_FLAVORED_JELLY = ITEMS.register("slime_king_flavored_jelly",
            () -> new Item(new Item.Properties().food(KSFoods.SLIME_KING_FLAVORED_JELLY_FOOD)));


    // ==================== 酒水 ====================
    /** 上古水果酒 */
    DeferredItem<ModDrinkBlockItem> ANCIENT_FRUIT_WINE = ITEMS.register("ancient_fruit_wine", () -> new ModDrinkBlockItem(KSBlocks.ANCIENT_FRUIT_WINE.get()));
    /** 劲爽啤酒 */
    DeferredItem<ModDrinkBlockItem> REFRESHING_BEER = ITEMS.register("refreshing_beer", () -> new ModDrinkBlockItem(KSBlocks.REFRESHING_BEER.get()));
    /** 燕京U7 */
    DeferredItem<ModDrinkBlockItem> YANJING_U7 = ITEMS.register("yanjing_u7", () -> new ModDrinkBlockItem(KSBlocks.YANJING_U7.get()));
    /** 葡萄风味葡萄酒 */
    DeferredItem<ModDrinkBlockItem> GRAPE_FLAVORED_WINE = ITEMS.register("grape_flavored_wine", () -> new ModDrinkBlockItem(KSBlocks.GRAPE_FLAVORED_WINE.get()));
    /** 赤眼珠宝 */
    DeferredItem<ModDrinkBlockItem> RED_EYE_WINE = ITEMS.register("red_eye_wine", () -> new ModDrinkBlockItem(KSBlocks.RED_EYE_WINE.get()));
    /** Joja可乐 */
    DeferredItem<ModDrinkBlockItem> JOJA_COLA = ITEMS.register("joja_cola", () -> new ModDrinkBlockItem(KSBlocks.JOJA_COLA.get()));


    // ==================== 鸡尾酒 ====================
    /** 冰之精灵 */
    DeferredItem<CocktailBlockItem> ICE_ELF_COCKTAIL = ITEMS.register("ice_elf_cocktail", () -> new CocktailBlockItem(KSBlocks.ICE_ELF_COCKTAIL.get()));
    /** 气泡回响 */
    DeferredItem<CocktailBlockItem> BUBBLE_ECHO_COCKTAIL = ITEMS.register("bubble_echo", () -> new CocktailBlockItem(KSBlocks.BUBBLE_ECHO_COCKTAIL.get()));
    /** 春日饮 */
    DeferredItem<CocktailBlockItem> SPRING_DRINK_COCKTAIL = ITEMS.register("spring_drink", () -> new CocktailBlockItem(KSBlocks.SPRING_DRINK_COCKTAIL.get()));
    /** 春日饮 */
    DeferredItem<CocktailBlockItem> SPRING_DRINK_1_COCKTAIL = ITEMS.register("spring_drink_1", () -> new CocktailBlockItem(KSBlocks.SPRING_DRINK_1_COCKTAIL.get()));


    // ==================== 流体 ====================
    /** 上古水果汁桶 */
    DeferredItem<JuiceBucketItem> ANCIENT_FRUIT_JUICE_BUCKET = ITEMS.register("ancient_fruit_juice_bucket", () -> new JuiceBucketItem(KSFluids.ANCIENT_FRUIT_JUICE));


    // ==================== 材料 ====================
    /** 杂蔬饲料 */
    DeferredItem<Item> LIVESTOCK_FEED = ITEMS.register("livestock_feed", () -> new Item(new Item.Properties()));
    /** 八宝肥虫饲料 */
    DeferredItem<Item> POULTRY_FEED = ITEMS.register("poultry_feed", () -> new Item(new Item.Properties()));
    /** 通用饲料 */
    DeferredItem<Item> UNIVERSAL_FEED = ITEMS.register("universal_feed", () -> new Item(new Item.Properties()));
    /** 抹茶粉 */
    DeferredItem<Item> MATCHA_POWDER = ITEMS.register("matcha_powder", () -> new Item(new Item.Properties()));
    /** 奶油 */
    DeferredItem<Item> CREAM = ITEMS.register("cream", () -> new CreamItem(new Item.Properties()));


    // ==================== 工具 ====================
    /** 拍拍器 */
    DeferredItem<PetterItem> PETTER = ITEMS.register("petter", () -> new PetterItem(new Item.Properties().stacksTo(1).durability(700)));
    /** 冷灶 */
    DeferredItem<Item> COLD_STOVE = ITEMS.register("cold_stove", () -> new BlockItem(KSBlocks.COLD_STOVE.get(), new Item.Properties()));
    /** 云吞 */
    DeferredItem<Item> YUNOMI = ITEMS.register("yunomi", YunomiItem::new);
    /** 奶油油壶 */
    DeferredItem<BlockItem> CREAM_OIL = ITEMS.register("cream_oil", () -> new CreamOilBlockItem(KSBlocks.CREAM_OIL.get(), new Item.Properties().stacksTo(1)));


    // ==================== 茶叶 ====================
    /** 绿茶茶叶 */
    DeferredItem<Item> GREEN_TEA_LEAVES = ITEMS.register("green_tea_leaves", () -> new Item(new Item.Properties()));


    // ==================== 茶包 ====================
    /** 玫瑰花茶茶包 */
    DeferredItem<Item> ROSE_TEA_BAG = ITEMS.register("rose_tea_bag", () -> new Item(new Item.Properties()));



    private static DeferredItem<BowlFoodBlockItem> registerBowlFood(String name, DeferredBlock<? extends Block> block, FoodProperties food) {
        return ITEMS.register(name, () -> new BowlFoodBlockItem(block.get(), food, Items.BOWL));
    }

    private static DeferredItem<BowlFoodBlockItemWithTooltip> registerBowlFoodWithTooltip(
            String name, DeferredBlock<? extends Block> block, FoodProperties food, String tooltipKey) {
        return ITEMS.register(name, () -> new BowlFoodBlockItemWithTooltip(block.get(), food, Items.BOWL, tooltipKey));
    }

    static void register(IEventBus bus){
        ITEMS.register(bus);
    }
}
