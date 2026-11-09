package tequila.cat.pigpig.init;

import com.github.ysbbbbbb.kaleidoscopecookery.item.BowlFoodBlockItem;
import com.github.ysbbbbbb.kaleidoscopetavern.item.CocktailBlockItem;
import com.github.ysbbbbbb.kaleidoscopetavern.item.JuiceBucketItem;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import tequila.cat.pigpig.KaleidoscopeStartale;
import tequila.cat.pigpig.fluid.ModFluids;
import tequila.cat.pigpig.item.BowlFoodBlockItemWithTooltip;
import tequila.cat.pigpig.item.EffectFoodItem;
import tequila.cat.pigpig.item.JuicerBlockItem;
import tequila.cat.pigpig.item.ModDrinkBlockItem;
import tequila.cat.pigpig.item.PetterItem;
import tequila.cat.pigpig.item.RhubarbJamItem;
import tequila.cat.pigpig.item.SeedItem;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(KaleidoscopeStartale.MODID);

    // ---- 作物 ----
    private static final FoodProperties RHUBARB_FOOD = food(1, 1.0F).build();
    public static final DeferredItem<SeedItem> RHUBARB_SEEDS = ITEMS.register("rhubarb_seeds",
            () -> new SeedItem(ModBlocks.RHUBARB_CROP.get(), new Item.Properties()));
    public static final DeferredItem<Item> RHUBARB = ITEMS.register("rhubarb",
            () -> new Item(new Item.Properties().food(RHUBARB_FOOD)));

    public static final DeferredItem<SeedItem> AMARANTH_SEEDS = ITEMS.register("amaranth_seeds",
            () -> new SeedItem(ModBlocks.AMARANTH_CROP.get(), new Item.Properties()));
    public static final DeferredItem<Item> AMARANTH = ITEMS.register("amaranth",
            () -> new Item(new Item.Properties()));

    private static final FoodProperties ANCIENT_FRUIT_FOOD = food(4, 1.0F).build();
    /** 上古水果藤的种植凭证物品（对藤架右键种下） */
    public static final DeferredItem<Item> ANCIENT_FRUIT_VINE = ITEMS.register("ancient_fruit_vine",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> ANCIENT_FRUIT = ITEMS.register("ancient_fruit",
            () -> new Item(new Item.Properties().food(ANCIENT_FRUIT_FOOD)));

    private static final FoodProperties GOLDFISH_FOOD = food(2, 0.5F).build();
    public static final DeferredItem<Item> GOLDFISH = ITEMS.register("goldfish",
            () -> new Item(new Item.Properties().food(GOLDFISH_FOOD)));

    // ---- 加工食品 ----
    private static final FoodProperties RHUBARB_JAM_FOOD = food(7, 1.0F)
            .usingConvertsTo(Items.GLASS_BOTTLE)
            .effect(() -> new MobEffectInstance(com.github.ysbbbbbb.kaleidoscopecookery.init.ModEffects.MUSTARD, 6000), 1.0F)
            .effect(() -> new MobEffectInstance(com.github.ysbbbbbb.kaleidoscopecookery.init.ModEffects.WARMTH, 3600), 1.0F)
            .build();
    public static final DeferredItem<RhubarbJamItem> RHUBARB_JAM = ITEMS.register("rhubarb_jam",
            () -> new RhubarbJamItem(ModBlocks.RHUBARB_JAM.get(), new Item.Properties().stacksTo(16).food(RHUBARB_JAM_FOOD)));

    private static final FoodProperties ANCIENT_FRUIT_PLATTER_FOOD = food(9, 1.0F)
            .usingConvertsTo(Items.BOWL)
            .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 3600), 1.0F)
            .effect(() -> new MobEffectInstance(com.github.ysbbbbbb.kaleidoscopecookery.init.ModEffects.VIGOR, 3600), 1.0F)
            .build();
    public static final DeferredItem<EffectFoodItem> ANCIENT_FRUIT_PLATTER = ITEMS.register("ancient_fruit_platter",
            () -> new EffectFoodItem(new Item.Properties().food(ANCIENT_FRUIT_PLATTER_FOOD)));

    private static final FoodProperties AMARANTH_SOUP_FOOD = food(10, 1.1F)
            .usingConvertsTo(Items.BOWL)
            .effect(() -> new MobEffectInstance(com.github.ysbbbbbb.kaleidoscopecookery.init.ModEffects.WARMTH, 3600), 1.0F)
            .build();
    public static final DeferredItem<EffectFoodItem> AMARANTH_SOUP = ITEMS.register("amaranth_soup",
            () -> new EffectFoodItem(new Item.Properties().stacksTo(1).food(AMARANTH_SOUP_FOOD)));

    private static final FoodProperties FISH_MUSHROOM_SOUP_ITEM_FOOD = food(22, 1.2F)
            .alwaysEdible()
            .effect(() -> new MobEffectInstance(ModEffects.MEAL, 6000), 1.0F)
            .build();
    public static final DeferredItem<BowlFoodBlockItemWithTooltip> FISH_MUSHROOM_SOUP = ITEMS.register("fish_mushroom_soup",
            () -> new BowlFoodBlockItemWithTooltip(ModBlocks.FISH_MUSHROOM_SOUP.get(), FISH_MUSHROOM_SOUP_ITEM_FOOD,
                    Items.BOWL, "tooltip.kaleidoscope_startale.fish_mushroom_soup.maxim"));

    /** 整份 22 点饱食度、1.2 倍饱和度；发光与弹射闪避各 5 分钟。 */
    private static final FoodProperties GLOWING_CATERPILLAR_SOUP_ITEM_FOOD = food(22, 1.2F)
            .alwaysEdible()
            .effect(() -> new MobEffectInstance(MobEffects.GLOWING, 6000), 1.0F)
            .effect(() -> new MobEffectInstance(com.github.ysbbbbbb.kaleidoscopecookery.init.ModEffects.PROJECTILE_DODGE, 6000), 1.0F)
            .build();
    public static final DeferredItem<BowlFoodBlockItem> GLOWING_CATERPILLAR_SOUP = ITEMS.register("glowing_caterpillar_soup",
            () -> new BowlFoodBlockItem(ModBlocks.GLOWING_CATERPILLAR_SOUP.get(), GLOWING_CATERPILLAR_SOUP_ITEM_FOOD,
                    Items.BOWL));

    private static final FoodProperties RHUBARB_FONDANT_PIE_ITEM_FOOD = food(20, 1.1F).alwaysEdible().build();
    public static final DeferredItem<BowlFoodBlockItem> RHUBARB_FONDANT_PIE = ITEMS.register("rhubarb_fondant_pie",
            () -> new BowlFoodBlockItem(ModBlocks.RHUBARB_FONDANT_PIE.get(), RHUBARB_FONDANT_PIE_ITEM_FOOD, Items.BOWL));

    private static final FoodProperties OMELETTE_RICE_ITEM_FOOD = food(17, 1.2F).alwaysEdible().build();
    public static final DeferredItem<BowlFoodBlockItem> OMELETTE_RICE = ITEMS.register("omelette_rice",
            () -> new BowlFoodBlockItem(ModBlocks.OMELETTE_RICE.get(), OMELETTE_RICE_ITEM_FOOD, Items.BOWL));

    private static final FoodProperties ANCIENT_FRUIT_SKEWER_FOOD = food(14, 1.1F).build();
    public static final DeferredItem<Item> ANCIENT_FRUIT_SKEWER = ITEMS.register("ancient_fruit_skewer",
            () -> new Item(new Item.Properties().food(ANCIENT_FRUIT_SKEWER_FOOD)));

    private static final FoodProperties EGG_BAKED_CAKE_FOOD = food(11, 1.0F)
            .effect(() -> new MobEffectInstance(com.github.ysbbbbbb.kaleidoscopecookery.init.ModEffects.WARMTH, 3600), 1.0F)
            .build();
    public static final DeferredItem<EffectFoodItem> EGG_BAKED_CAKE = ITEMS.register("egg_baked_cake",
            () -> new EffectFoodItem(new Item.Properties().food(EGG_BAKED_CAKE_FOOD)));

    private static final FoodProperties IDEA_COOKIE_FOOD = food(7, 0.8F)
            .effect(() -> new MobEffectInstance(ModEffects.INSPIRATION, 700), 1.0F)
            .build();
    public static final DeferredItem<EffectFoodItem> IDEA_COOKIE = ITEMS.register("idea_cookie",
            () -> new EffectFoodItem(new Item.Properties().food(IDEA_COOKIE_FOOD)));

    private static final FoodProperties INSPIRATION_MUSHROOM_FOOD = food(5, 0.7F)
            .effect(() -> new MobEffectInstance(ModEffects.INSPIRATION, 400), 1.0F)
            .build();
    public static final DeferredItem<EffectFoodItem> INSPIRATION_MUSHROOM = ITEMS.register("inspiration_mushroom",
            () -> new EffectFoodItem(new Item.Properties().food(INSPIRATION_MUSHROOM_FOOD)));

    /** 整份 28 点饱食度、1.2 倍饱和度。 */
    private static final FoodProperties SLIME_KING_FLAVORED_JELLY_FOOD = new FoodProperties.Builder()
            .nutrition(28)
            .saturationModifier(1.2F)
            .build();
    public static final DeferredItem<Item> SLIME_KING_FLAVORED_JELLY = ITEMS.register("slime_king_flavored_jelly",
            () -> new Item(new Item.Properties().food(SLIME_KING_FLAVORED_JELLY_FOOD)));

    // ---- 酒水 / 果汁 ----
    public static final DeferredItem<JuiceBucketItem> ANCIENT_FRUIT_JUICE_BUCKET = ITEMS.register("ancient_fruit_juice_bucket",
            () -> new JuiceBucketItem(ModFluids.ANCIENT_FRUIT_JUICE));
    public static final DeferredItem<ModDrinkBlockItem> ANCIENT_FRUIT_WINE = ITEMS.register("ancient_fruit_wine",
            () -> new ModDrinkBlockItem(ModBlocks.ANCIENT_FRUIT_WINE.get()));
    public static final DeferredItem<ModDrinkBlockItem> REFRESHING_BEER = ITEMS.register("refreshing_beer",
            () -> new ModDrinkBlockItem(ModBlocks.REFRESHING_BEER.get()));
    public static final DeferredItem<ModDrinkBlockItem> YANJING_U7 = ITEMS.register("yanjing_u7",
            () -> new ModDrinkBlockItem(ModBlocks.YANJING_U7.get()));
    public static final DeferredItem<ModDrinkBlockItem> GRAPE_FLAVORED_WINE = ITEMS.register("grape_flavored_wine",
            () -> new ModDrinkBlockItem(ModBlocks.GRAPE_FLAVORED_WINE.get()));
    public static final DeferredItem<ModDrinkBlockItem> RED_EYE_WINE = ITEMS.register("red_eye_wine",
            () -> new ModDrinkBlockItem(ModBlocks.RED_EYE_WINE.get()));
    public static final DeferredItem<ModDrinkBlockItem> JOJA_COLA = ITEMS.register("joja_cola",
            () -> new ModDrinkBlockItem(ModBlocks.JOJA_COLA.get()));
    public static final DeferredItem<CocktailBlockItem> ICE_ELF_COCKTAIL = ITEMS.register("ice_elf_cocktail",
            () -> new CocktailBlockItem(ModBlocks.ICE_ELF_COCKTAIL.get()));
    public static final DeferredItem<CocktailBlockItem> BUBBLE_ECHO_COCKTAIL = ITEMS.register("bubble_echo",
            () -> new CocktailBlockItem(ModBlocks.BUBBLE_ECHO_COCKTAIL.get()));
    public static final DeferredItem<CocktailBlockItem> SPRING_DRINK_COCKTAIL = ITEMS.register("spring_drink",
            () -> new CocktailBlockItem(ModBlocks.SPRING_DRINK_COCKTAIL.get()));
    public static final DeferredItem<CocktailBlockItem> SPRING_DRINK_1_COCKTAIL = ITEMS.register("spring_drink_1",
            () -> new CocktailBlockItem(ModBlocks.SPRING_DRINK_1_COCKTAIL.get()));
    public static final DeferredItem<CocktailBlockItem> BERRY_JUICE = ITEMS.register("berry_juice",
            () -> new CocktailBlockItem(ModBlocks.BERRY_JUICE.get()));
    public static final DeferredItem<CocktailBlockItem> GLOW_BERRY_JUICE = ITEMS.register("glow_berry_juice",
            () -> new CocktailBlockItem(ModBlocks.GLOW_BERRY_JUICE.get()));
    public static final DeferredItem<CocktailBlockItem> GRAPE_JUICE = ITEMS.register("grape_juice",
            () -> new CocktailBlockItem(ModBlocks.GRAPE_JUICE.get()));
    public static final DeferredItem<CocktailBlockItem> GOLD_GRAPE_JUICE = ITEMS.register("gold_grape_juice",
            () -> new CocktailBlockItem(ModBlocks.GOLD_GRAPE_JUICE.get()));
    public static final DeferredItem<CocktailBlockItem> ICE_GRAPE_JUICE = ITEMS.register("ice_grape_juice",
            () -> new CocktailBlockItem(ModBlocks.ICE_GRAPE_JUICE.get()));
    public static final DeferredItem<CocktailBlockItem> GREEN_GRAPE_JUICE = ITEMS.register("green_grape_juice",
            () -> new CocktailBlockItem(ModBlocks.GREEN_GRAPE_JUICE.get()));

    private static final FoodProperties ENCHANTED_NIGHTCRAWLER_FOOD = food(18, 0.4F)
            .alwaysEdible()
            .effect(() -> new MobEffectInstance(MobEffects.CONFUSION, 200), 1.0F)
            .build();
    public static final DeferredItem<EffectFoodItem> ENCHANTED_NIGHTCRAWLER = ITEMS.register("enchanted_nightcrawler",
            () -> new EffectFoodItem(new Item.Properties().food(ENCHANTED_NIGHTCRAWLER_FOOD)));

    // ---- 饲料 / 工具 ----
    public static final DeferredItem<Item> LIVESTOCK_FEED = ITEMS.register("livestock_feed",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POULTRY_FEED = ITEMS.register("poultry_feed",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> UNIVERSAL_FEED = ITEMS.register("universal_feed",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<JuicerBlockItem> JUICER = ITEMS.register("juicer",
            () -> new JuicerBlockItem(ModBlocks.JUICER.get(), new Item.Properties()));
    public static final DeferredItem<PetterItem> PETTER = ITEMS.register("petter",
            () -> new PetterItem(new Item.Properties().stacksTo(1).durability(700)));

    private static FoodProperties.Builder food(int nutrition, float saturationMultiplier) {
        return new FoodProperties.Builder().nutrition(nutrition).saturationModifier(saturationMultiplier / 2.0F);
    }

    private ModItems() {
    }
}
