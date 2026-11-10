package com.tequilacat.kaleidoscope_startale.init;

import com.github.ysbbbbbb.kaleidoscopecookery.block.food.FoodBiteBlock;
import com.github.ysbbbbbb.kaleidoscopecookery.init.registry.FoodBiteAnimateTicks;
import com.github.ysbbbbbb.kaleidoscopetavern.block.brew.DrinkBlock;
import com.github.ysbbbbbb.kaleidoscopetavern.block.mixology.CocktailBlock;
import com.tequilacat.kaleidoscope_startale.KaleidoscopeStartale;
import com.tequilacat.kaleidoscope_startale.block.*;
import com.tequilacat.kaleidoscope_startale.block.cake.MatchaCake;
import com.tequilacat.kaleidoscope_startale.block.crop.ancientFruit.AncientFruitTrellisCropBlock;
import com.tequilacat.kaleidoscope_startale.block.crop.ancientFruit.AncientFruitVineBlock;
import com.tequilacat.kaleidoscope_startale.util.DrinkShapes;
import com.tequilacat.kaleidoscope_startale.util.KSCropBlock;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public interface KSBlocks {
    DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(KaleidoscopeStartale.MODID);

    // ==================== 作物 ====================
    /** 大黄 */
    DeferredBlock<KSCropBlock> RHUBARB_CROP = BLOCKS.register("rhubarb_crop",
            () -> new KSCropBlock(5, lazyItem(KSItems.RHUBARB_SEEDS), cropProperties()));
    /** 苋菜 */
    DeferredBlock<KSCropBlock> AMARANTH_CROP = BLOCKS.register("amaranth_crop",
            () -> new KSCropBlock(5, lazyItem(KSItems.AMARANTH_SEEDS), cropProperties()));
    /** 上古水果：需要长在上古水果藤（熟）下方才能存活，剪刀采集。 */
    DeferredBlock<AncientFruitTrellisCropBlock> ANCIENT_FRUIT_CROP = BLOCKS.register("ancient_fruit_crop",
            () -> new AncientFruitTrellisCropBlock(KSBlocks::ancientFruitVineBlock, KSItems.ANCIENT_FRUIT, 2, vineCropProperties()));
    /** 上古水果藤：可从藤架向上 / 向水平蔓延，并在下方结果。 */
    DeferredBlock<AncientFruitVineBlock> ANCIENT_FRUIT_VINE = BLOCKS.register("ancient_fruit_vine",
            () -> new AncientFruitVineBlock(0.05F, 0.1F, KSBlocks::ancientFruitCropBlock, KSItems.ANCIENT_FRUIT_VINE));


    // ==================== 酒水 ====================
    /** 上古水果酒 */
    DeferredBlock<Block> ANCIENT_FRUIT_WINE = BLOCKS.register("ancient_fruit_wine", () -> DrinkBlock.create().maxCount(2).shapes(DrinkShapes.ancientFruitWine()).build().get());
    /** 劲爽啤酒 */
    DeferredBlock<Block> REFRESHING_BEER = BLOCKS.register("refreshing_beer", DrinkBlock.create().maxCount(4).shapes(DrinkShapes.defaultShapes()).build());
    /** 燕京U7 */
    DeferredBlock<Block> YANJING_U7 = BLOCKS.register("yanjing_u7", () -> DrinkBlock.create().maxCount(4).shapes(DrinkShapes.yanjingU7()).build().get());
    /** 葡萄风味葡萄酒 */
    DeferredBlock<Block> GRAPE_FLAVORED_WINE = BLOCKS.register("grape_flavored_wine", DrinkBlock.create().maxCount(4).shapes(DrinkShapes.defaultShapes()).build());
    /** 赤眼珠宝 */
    DeferredBlock<Block> RED_EYE_WINE = BLOCKS.register("red_eye_wine", () -> DrinkBlock.create().maxCount(4).shapes(DrinkShapes.redEyeWine()).build().get());
    /** Joja可乐 */
    DeferredBlock<Block> JOJA_COLA = BLOCKS.register("joja_cola", () -> DrinkBlock.create().maxCount(4).shapes(DrinkShapes.jojaCola()).build().get());
    /** 大黄酱 */
    DeferredBlock<Block> RHUBARB_JAM = BLOCKS.register("rhubarb_jam", () -> DrinkBlock.create().maxCount(2).shapes(DrinkShapes.rhubarbJam()).build().get());


    // ==================== 鸡尾酒 ====================
    /** 冰之精灵 */
    DeferredBlock<CocktailBlock> ICE_ELF_COCKTAIL = BLOCKS.register("ice_elf_cocktail", () -> new GlowingCocktailBlock(5));
    /** 气泡回响 */
    DeferredBlock<CocktailBlock> BUBBLE_ECHO_COCKTAIL = BLOCKS.register("bubble_echo", () -> new GlowingCocktailBlock(5));
    /** 春日饮 */
    DeferredBlock<CocktailBlock> SPRING_DRINK_COCKTAIL = BLOCKS.register("spring_drink", () -> new GlowingCocktailBlock(5));
    /** 春日饮 */
    DeferredBlock<CocktailBlock> SPRING_DRINK_1_COCKTAIL = BLOCKS.register("spring_drink_1", () -> new GlowingCocktailBlock(5));


    // ==================== 果汁 ====================
    /** 浆果汁 */
    DeferredBlock<JuiceGlassBlock> BERRY_JUICE = BLOCKS.register("berry_juice", () -> new JuiceGlassBlock());
    /** 发光浆果汁 */
    DeferredBlock<JuiceGlassBlock> GLOW_BERRY_JUICE = BLOCKS.register("glow_berry_juice", () -> new JuiceGlassBlock());
    /** 葡萄汁 */
    DeferredBlock<JuiceGlassBlock> GRAPE_JUICE = BLOCKS.register("grape_juice", () -> new JuiceGlassBlock());
    /** 黄金葡萄汁 */
    DeferredBlock<JuiceGlassBlock> GOLD_GRAPE_JUICE = BLOCKS.register("gold_grape_juice", () -> new JuiceGlassBlock());
    /** 冰葡萄汁 */
    DeferredBlock<JuiceGlassBlock> ICE_GRAPE_JUICE = BLOCKS.register("ice_grape_juice", () -> new JuiceGlassBlock());
    /** 青提葡萄汁 */
    DeferredBlock<JuiceGlassBlock> GREEN_GRAPE_JUICE = BLOCKS.register("green_grape_juice", () -> new JuiceGlassBlock());


    // ==================== 工具 ====================
    /** 榨汁机 */
    DeferredBlock<JuicerBlock> JUICER = BLOCKS.register("juicer", () -> new JuicerBlock(JuicerBlock.createProperties()));
    /** 冷灶 */
    DeferredBlock<Block> COLD_STOVE = BLOCKS.register("cold_stove", ColdStoveBlock::new);
    /** 奶油油壶 */
    DeferredBlock<Block> CREAM_OIL = BLOCKS.register("cream_oil", CreamOilBlock::new);
    /** 汤吞 */
    DeferredBlock<Block> YUNOMI = BLOCKS.register("yunomi", YunomiBlock::new);


    // ==================== 食物 ====================
    /** 大黄软糖派 */
    DeferredBlock<FoodBiteBlock> RHUBARB_FONDANT_PIE = BLOCKS.register("rhubarb_fondant_pie",
            () -> new FoodBiteBlock(KSFoods.RHUBARB_FONDANT_PIE_BLOCK_FOOD, 4, (FoodBiteAnimateTicks.AnimateTick) null));
    /** 蛋包饭 */
    DeferredBlock<FoodBiteBlock> OMELETTE_RICE = BLOCKS.register("omelette_rice",
            () -> new FoodBiteBlock(KSFoods.OMELETTE_RICE_BLOCK_FOOD, 3, (FoodBiteAnimateTicks.AnimateTick) null));
    /** 鱼菇煲 */
    DeferredBlock<FoodBiteBlock> FISH_MUSHROOM_SOUP = BLOCKS.register("fish_mushroom_soup",
            () -> new FoodBiteBlock(KSFoods.FISH_MUSHROOM_SOUP_BLOCK_FOOD, 4, (FoodBiteAnimateTicks.AnimateTick) null));
    /** 闪亮猪儿虫浓汤 */
    DeferredBlock<FoodBiteBlock> GLOWING_CATERPILLAR_SOUP = BLOCKS.register("glowing_caterpillar_soup",
            () -> new FoodBiteBlock(KSFoods.GLOWING_CATERPILLAR_SOUP_BLOCK_FOOD, 4, (FoodBiteAnimateTicks.AnimateTick) null));
    /** 抹茶蛋糕 */
    DeferredBlock<Block> MATCHA_CAKE = BLOCKS.register("matcha_cake", MatchaCake::new);


    private static ItemLike lazyItem(Supplier<? extends Item> supplier) {
        return supplier::get;
    }

    private static Block ancientFruitCropBlock() {
        return ANCIENT_FRUIT_CROP.get();
    }

    private static Block ancientFruitVineBlock() {
        return ANCIENT_FRUIT_VINE.get();
    }

    private static BlockBehaviour.Properties cropProperties() {
        return BlockBehaviour.Properties.of()
                .noCollission()
                .noOcclusion()
                .randomTicks()
                .instabreak()
                .sound(SoundType.CROP);
    }

    private static BlockBehaviour.Properties vineCropProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.PLANT)
                .noCollission()
                .randomTicks()
                .instabreak()
                .sound(SoundType.CROP)
                .offsetType(BlockBehaviour.OffsetType.XYZ)
                .pushReaction(PushReaction.DESTROY);
    }

    static void register(IEventBus bus){
        BLOCKS.register(bus);
    }
}
