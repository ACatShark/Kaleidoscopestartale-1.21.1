package com.tequilacat.kaleidoscope_startale.init;

import com.tequilacat.kaleidoscope_startale.util.EffectPair;
import com.tequilacat.kaleidoscope_startale.util.FoodBuilderHelper;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Items;

public interface KSFoods {

    /** 大黄 */
    FoodProperties RHUBARB = FoodBuilderHelper.simpleAlwaysEdible(2, 1.0f);
    /** 上古水果 */
    FoodProperties ANCIENT_FRUIT = FoodBuilderHelper.simpleAlwaysEdible(4, 1.0f);
    /** 金鱼 */
    FoodProperties GOLDFISH = FoodBuilderHelper.simpleAlwaysEdible(2, 1.0f);
    /** 大黄酱 */
    FoodProperties RHUBARB_JAM_FOOD = FoodBuilderHelper.simpleWithContainer(7, 1.0F, Items.GLASS_BOTTLE,
            EffectPair.of(() -> new MobEffectInstance(com.github.ysbbbbbb.kaleidoscopecookery.init.ModEffects.MUSTARD, 6000), 1.0F),
            EffectPair.of(() -> new MobEffectInstance(com.github.ysbbbbbb.kaleidoscopecookery.init.ModEffects.WARMTH, 3600), 1.0F));
    /** 上古水果拼盘 */
    FoodProperties ANCIENT_FRUIT_PLATTER_FOOD = FoodBuilderHelper.simpleWithContainer(9, 1.0F, Items.BOWL,
            EffectPair.of(() -> new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 3600), 1.0F),
            EffectPair.of(() -> new MobEffectInstance(com.github.ysbbbbbb.kaleidoscopecookery.init.ModEffects.VIGOR, 3600), 1.0F));
    /** 苋菜汤 */
    FoodProperties AMARANTH_SOUP_FOOD = FoodBuilderHelper.simpleWithContainer(9, 1.0F, Items.BOWL,
            EffectPair.of(() -> new MobEffectInstance(com.github.ysbbbbbb.kaleidoscopecookery.init.ModEffects.WARMTH, 3600), 1.0F));
    /** 鱼菇煲 */
    FoodProperties FISH_MUSHROOM_SOUP_ITEM_FOOD = FoodBuilderHelper.simpleAlwaysEdible(22, 1.2F,
            EffectPair.of(() -> new MobEffectInstance(KSEffects.MEAL, 6000), 1.0F));
    FoodProperties FISH_MUSHROOM_SOUP_BLOCK_FOOD = FoodBuilderHelper.simpleAlwaysEdible(6, 1.2F,
            EffectPair.of(() -> new MobEffectInstance(KSEffects.MEAL, 6000), 1.0F));
    /** 闪亮猪儿虫浓汤 */
    FoodProperties GLOWING_CATERPILLAR_SOUP_ITEM_FOOD = FoodBuilderHelper.simpleAlwaysEdible(22, 1.2F,
            EffectPair.of(() -> new MobEffectInstance(MobEffects.GLOWING, 6000), 1.0F),
            EffectPair.of(() -> new MobEffectInstance(com.github.ysbbbbbb.kaleidoscopecookery.init.ModEffects.PROJECTILE_DODGE, 6000), 1.0F));
    FoodProperties GLOWING_CATERPILLAR_SOUP_BLOCK_FOOD = FoodBuilderHelper.simpleAlwaysEdible(6, 1.2F,
            EffectPair.of(() -> new MobEffectInstance(MobEffects.GLOWING, 6000), 1.0F),
            EffectPair.of(() -> new MobEffectInstance(com.github.ysbbbbbb.kaleidoscopecookery.init.ModEffects.PROJECTILE_DODGE, 6000), 1.0F));
    /** 大黄软糖派 */
    FoodProperties RHUBARB_FONDANT_PIE_ITEM_FOOD = FoodBuilderHelper.simpleAlwaysEdible(20, 1.1F);
    FoodProperties RHUBARB_FONDANT_PIE_BLOCK_FOOD = FoodBuilderHelper.simpleAlwaysEdible(5, 1.1F);
    /** 蛋包饭 */
    FoodProperties OMELETTE_RICE_ITEM_FOOD = FoodBuilderHelper.simpleAlwaysEdible(17, 1.2F);
    FoodProperties OMELETTE_RICE_BLOCK_FOOD = FoodBuilderHelper.simpleAlwaysEdible(6, 1.2F);
    /** 上古水果串 */
    FoodProperties ANCIENT_FRUIT_SKEWER_FOOD = FoodBuilderHelper.simple(14, 1.1F);
    /** 烤蛋饼 */
    FoodProperties EGG_BAKED_CAKE_FOOD = FoodBuilderHelper.simple(11, 1.0F,
            EffectPair.of(() -> new MobEffectInstance(com.github.ysbbbbbb.kaleidoscopecookery.init.ModEffects.WARMTH, 3600), 1.0F));
    /** IntelliJ IDEA 饼干 */
    FoodProperties IDEA_COOKIE_FOOD = FoodBuilderHelper.simple(7, 0.8F,
            EffectPair.of(() -> new MobEffectInstance(KSEffects.INSPIRATION, 700), 1.0F));
    /** 灵感菇 */
    FoodProperties INSPIRATION_MUSHROOM_FOOD = FoodBuilderHelper.simple(5, 0.7F,
            EffectPair.of(() -> new MobEffectInstance(KSEffects.INSPIRATION, 400), 1.0F));
    /** 史莱姆王风味果冻 */
    FoodProperties SLIME_KING_FLAVORED_JELLY_FOOD = FoodBuilderHelper.simple(28, 1.2F);
    /** 附魔夜行者 */
    FoodProperties ENCHANTED_NIGHTCRAWLER_FOOD = FoodBuilderHelper.simpleAlwaysEdible(18, 0.4F,
            EffectPair.of(() -> new MobEffectInstance(MobEffects.CONFUSION, 200), 1.0F));

}