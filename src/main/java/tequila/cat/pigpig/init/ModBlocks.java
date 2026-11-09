package tequila.cat.pigpig.init;

import java.util.function.Supplier;

import com.github.ysbbbbbb.kaleidoscopecookery.block.food.FoodBiteBlock;
import com.github.ysbbbbbb.kaleidoscopecookery.init.registry.FoodBiteAnimateTicks;
import com.github.ysbbbbbb.kaleidoscopetavern.block.brew.DrinkBlock;
import com.github.ysbbbbbb.kaleidoscopetavern.block.mixology.CocktailBlock;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import tequila.cat.pigpig.KaleidoscopeStartale;
import tequila.cat.pigpig.block.GlowingCocktailBlock;
import tequila.cat.pigpig.block.JuiceGlassBlock;
import tequila.cat.pigpig.block.JuicerBlock;
import tequila.cat.pigpig.block.ModCropBlock;
import tequila.cat.pigpig.block.ModTrellisCropBlock;
import tequila.cat.pigpig.block.ModVineBlock;

public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(KaleidoscopeStartale.MODID);

    /** 上古水果藤每 tick 生长概率 */
    private static final float ANCIENT_FRUIT_VINE_GROW_CHANCE = 0.05F;
    /** 上古水果藤骨粉成功率 */
    private static final float ANCIENT_FRUIT_VINE_BONEMEAL_CHANCE = 0.1F;

    // ---- 作物 ----
    public static final DeferredBlock<ModCropBlock> RHUBARB_CROP =
            BLOCKS.register("rhubarb_crop", () -> new ModCropBlock(5, lazyItem(ModItems.RHUBARB_SEEDS::get), cropProperties()));
    public static final DeferredBlock<ModCropBlock> AMARANTH_CROP =
            BLOCKS.register("amaranth_crop", () -> new ModCropBlock(5, lazyItem(ModItems.AMARANTH_SEEDS::get), cropProperties()));

    /** 上古水果：需要长在上古水果藤（熟）下方才能存活，剪刀采集。 */
    public static final DeferredBlock<ModTrellisCropBlock> ANCIENT_FRUIT_CROP =
            BLOCKS.register("ancient_fruit_crop", () -> new ModTrellisCropBlock(
                    ModBlocks::ancientFruitVineBlock, ModItems.ANCIENT_FRUIT::get, 2, vineCropProperties()));
    /** 上古水果藤：可从藤架向上 / 向水平蔓延，并在下方结果。 */
    public static final DeferredBlock<ModVineBlock> ANCIENT_FRUIT_VINE =
            BLOCKS.register("ancient_fruit_vine", () -> new ModVineBlock(
                    ANCIENT_FRUIT_VINE_GROW_CHANCE, ANCIENT_FRUIT_VINE_BONEMEAL_CHANCE,
                    ModBlocks::ancientFruitCropBlock, ModItems.ANCIENT_FRUIT_VINE::get));

    // ---- 酒水（tavern 的 DrinkBlock，最多 4 层，喝干后剩空瓶） ----
    public static final DeferredBlock<Block> ANCIENT_FRUIT_WINE = BLOCKS.register("ancient_fruit_wine",
            () -> DrinkBlock.create().maxCount(2).shapes(
                    Block.box(5.0D, 0.0D, 5.0D, 11.0D, 16.0D, 11.0D),
                    Block.box(2.0D, 0.0D, 2.0D, 15.0D, 16.0D, 14.0D)).build().get());
    public static final DeferredBlock<Block> REFRESHING_BEER = BLOCKS.register("refreshing_beer",
            ModBlocks::createDrinkBlock);
    public static final DeferredBlock<Block> YANJING_U7 = BLOCKS.register("yanjing_u7",
            () -> DrinkBlock.create().maxCount(4).shapes(
                    Block.box(6.0D, 0.0D, 6.0D, 10.0D, 16.0D, 10.0D),
                    Block.box(3.0D, 0.0D, 3.0D, 13.0D, 16.0D, 12.0D),
                    Block.box(3.0D, 0.0D, 3.0D, 13.0D, 16.0D, 15.0D),
                    Block.box(3.0D, 0.0D, 2.0D, 13.0D, 16.0D, 13.0D)).build().get());
    public static final DeferredBlock<Block> GRAPE_FLAVORED_WINE = BLOCKS.register("grape_flavored_wine",
            ModBlocks::createDrinkBlock);
    public static final DeferredBlock<Block> RED_EYE_WINE = BLOCKS.register("red_eye_wine",
            () -> DrinkBlock.create().maxCount(4).shapes(
                    Block.box(3.05D, 0.0D, 6.0D, 12.95D, 15.0D, 10.0D),
                    Block.box(0.0D, 0.0D, 6.0D, 16.0D, 15.0D, 10.0D),
                    Block.box(0.0D, 0.0D, 4.0D, 16.0D, 15.0D, 14.0D),
                    Block.box(0.0D, 0.0D, 4.0D, 16.0D, 15.0D, 14.0D)).build().get());
    public static final DeferredBlock<Block> JOJA_COLA = BLOCKS.register("joja_cola",
            () -> DrinkBlock.create().maxCount(4).shapes(
                    // count=1：单罐，模型 y 0.5~7.5（罐底半像素底环）
                    Block.box(5.5D, 0.0D, 5.5D, 10.5D, 7.5D, 10.5D),
                    // count=2：两罐上下叠放，y 0~14
                    Block.box(5.5D, 0.0D, 5.5D, 10.5D, 14.0D, 10.5D),
                    // count=3：底部前后两罐（z 2.5~7.5 / 8.5~13.5）+ 顶上一罐
                    Shapes.or(
                            Block.box(5.5D, 0.0D, 2.5D, 10.5D, 7.0D, 7.5D),
                            Block.box(5.5D, 0.0D, 8.5D, 10.5D, 7.0D, 13.5D),
                            Block.box(5.5D, 7.0D, 5.5D, 10.5D, 14.0D, 10.5D)),
                    // count=4：2×2 四罐，y 0~7
                    Shapes.or(
                            Block.box(1.5D, 0.0D, 1.5D, 6.5D, 7.0D, 6.5D),
                            Block.box(9.5D, 0.0D, 1.5D, 14.5D, 7.0D, 6.5D),
                            Block.box(1.5D, 0.0D, 9.5D, 6.5D, 7.0D, 14.5D),
                            Block.box(9.5D, 0.0D, 9.5D, 14.5D, 7.0D, 14.5D)))
                    .build().get());
    public static final DeferredBlock<Block> RHUBARB_JAM = BLOCKS.register("rhubarb_jam",
            () -> DrinkBlock.create().maxCount(2).shapes(
                    Block.box(4.0D, 0.0D, 6.0D, 12.0D, 13.0D, 10.0D),
                    Block.box(0.0D, 0.0D, 3.0D, 16.0D, 13.0D, 14.0D)).build().get());

    // ---- 鸡尾酒（tavern 的 CocktailBlock，由调酒器 shake 出来，喝干后剩空玻璃器皿） ----
    /** 亮度 5：桌面上一小圈微光，又不会把方块本身照得发白。 */
    private static final int COCKTAIL_LIGHT = 5;
    public static final DeferredBlock<CocktailBlock> ICE_ELF_COCKTAIL =
            BLOCKS.register("ice_elf_cocktail", () -> new GlowingCocktailBlock(COCKTAIL_LIGHT));
    public static final DeferredBlock<CocktailBlock> BUBBLE_ECHO_COCKTAIL =
            BLOCKS.register("bubble_echo", () -> new GlowingCocktailBlock(COCKTAIL_LIGHT));
    public static final DeferredBlock<CocktailBlock> SPRING_DRINK_COCKTAIL =
            BLOCKS.register("spring_drink", () -> new GlowingCocktailBlock(COCKTAIL_LIGHT));
    public static final DeferredBlock<CocktailBlock> SPRING_DRINK_1_COCKTAIL =
            BLOCKS.register("spring_drink_1", () -> new GlowingCocktailBlock(COCKTAIL_LIGHT));

    // ---- 果汁（酒杯形态，碰撞箱按模型收紧，喝干后剩空玻璃器皿） ----
    public static final DeferredBlock<JuiceGlassBlock> BERRY_JUICE =
            BLOCKS.register("berry_juice", () -> new JuiceGlassBlock());
    public static final DeferredBlock<JuiceGlassBlock> GLOW_BERRY_JUICE =
            BLOCKS.register("glow_berry_juice", () -> new JuiceGlassBlock());
    public static final DeferredBlock<JuiceGlassBlock> GRAPE_JUICE =
            BLOCKS.register("grape_juice", () -> new JuiceGlassBlock());
    public static final DeferredBlock<JuiceGlassBlock> GOLD_GRAPE_JUICE =
            BLOCKS.register("gold_grape_juice", () -> new JuiceGlassBlock());
    public static final DeferredBlock<JuiceGlassBlock> ICE_GRAPE_JUICE =
            BLOCKS.register("ice_grape_juice", () -> new JuiceGlassBlock());
    public static final DeferredBlock<JuiceGlassBlock> GREEN_GRAPE_JUICE =
            BLOCKS.register("green_grape_juice", () -> new JuiceGlassBlock());

    // ---- 榨汁机 ----
    public static final DeferredBlock<JuicerBlock> JUICER =
            BLOCKS.register("juicer", () -> new JuicerBlock(JuicerBlock.createProperties()));

    // ---- 可分段食用的锅物 ----
    private static final FoodProperties RHUBARB_FONDANT_PIE_BLOCK_FOOD = food(5, 1.1F).alwaysEdible().build();
    private static final FoodProperties OMELETTE_RICE_BLOCK_FOOD = food(6, 1.2F).alwaysEdible().build();
    private static final FoodProperties FISH_MUSHROOM_SOUP_BLOCK_FOOD = food(6, 1.2F).alwaysEdible()
            .effect(() -> new MobEffectInstance(ModEffects.MEAL, 6000), 1.0F)
            .build();
    /** 每口 6 点饱食度（4 层共 24），发光与弹射闪避各 5 分钟。 */
    private static final FoodProperties GLOWING_CATERPILLAR_SOUP_BLOCK_FOOD = food(6, 1.2F).alwaysEdible()
            .effect(() -> new MobEffectInstance(MobEffects.GLOWING, 6000), 1.0F)
            .effect(() -> new MobEffectInstance(com.github.ysbbbbbb.kaleidoscopecookery.init.ModEffects.PROJECTILE_DODGE, 6000), 1.0F)
            .build();

    public static final DeferredBlock<FoodBiteBlock> RHUBARB_FONDANT_PIE = BLOCKS.register("rhubarb_fondant_pie",
            () -> new FoodBiteBlock(RHUBARB_FONDANT_PIE_BLOCK_FOOD, 4, (FoodBiteAnimateTicks.AnimateTick) null));
    public static final DeferredBlock<FoodBiteBlock> OMELETTE_RICE = BLOCKS.register("omelette_rice",
            () -> new FoodBiteBlock(OMELETTE_RICE_BLOCK_FOOD, 3, (FoodBiteAnimateTicks.AnimateTick) null));
    public static final DeferredBlock<FoodBiteBlock> FISH_MUSHROOM_SOUP = BLOCKS.register("fish_mushroom_soup",
            () -> new FoodBiteBlock(FISH_MUSHROOM_SOUP_BLOCK_FOOD, 4, (FoodBiteAnimateTicks.AnimateTick) null));
    public static final DeferredBlock<FoodBiteBlock> GLOWING_CATERPILLAR_SOUP = BLOCKS.register("glowing_caterpillar_soup",
            () -> new FoodBiteBlock(GLOWING_CATERPILLAR_SOUP_BLOCK_FOOD, 4, (FoodBiteAnimateTicks.AnimateTick) null));

    /**
     * 惰性 ItemLike：注册阶段不触碰物品 DeferredHolder（避免不同注册表事件
     * 顺序不确定时触发 "Trying to access unbound value"），仅在运行时按需解析。
     */
    private static ItemLike lazyItem(Supplier<? extends Item> supplier) {
        return supplier::get;
    }

    private static Block ancientFruitCropBlock() {
        return ANCIENT_FRUIT_CROP.get();
    }

    private static Block ancientFruitVineBlock() {
        return ANCIENT_FRUIT_VINE.get();
    }

    private static FoodProperties.Builder food(int nutrition, float saturationMultiplier) {
        return new FoodProperties.Builder().nutrition(nutrition).saturationModifier(saturationMultiplier / 2.0F);
    }

    /** 4 层高的通用酒瓶碰撞箱 */
    private static Block createDrinkBlock() {
        return DrinkBlock.create().maxCount(4).shapes(
                Block.box(6.0D, 0.0D, 6.0D, 10.0D, 16.0D, 10.0D),
                Block.box(4.0D, 0.0D, 3.0D, 12.0D, 16.0D, 13.0D),
                Block.box(2.0D, 0.0D, 3.0D, 13.0D, 16.0D, 14.0D),
                Block.box(2.0D, 0.0D, 3.0D, 14.0D, 16.0D, 14.0D)).build().get();
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

    private ModBlocks() {
    }
}
