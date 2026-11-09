package com.tequilacat.kaleidoscope_startale.util;

import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;

/**
 * 食物属性构建工具类。
 * <p>
 * 提供便捷方法创建食物属性，统一处理饱和度倍率换算，
 * 并支持附带效果、始终可食用与使用后返还容器等配置。
 */
public class FoodBuilderHelper {

    /**
     * 创建食物属性构建器。
     * <p>
     * 饱和度修饰符按传入倍率的一半计算。
     *
     * @param nutrition           营养值
     * @param saturationMultiplier 饱和度倍率
     * @return 食物属性构建器
     */
    public static FoodProperties.Builder food(int nutrition, float saturationMultiplier) {
        return new FoodProperties.Builder()
                .nutrition(nutrition)
                .saturationModifier(saturationMultiplier / 2.0F);
    }

    /**
     * 创建基础食物属性。
     *
     * @param nutrition           营养值
     * @param saturationMultiplier 饱和度倍率
     * @return 食物属性
     */
    public static FoodProperties simple(int nutrition, float saturationMultiplier) {
        return food(nutrition, saturationMultiplier).build();
    }

    /**
     * 创建附带效果的食物属性。
     *
     * @param nutrition           营养值
     * @param saturationMultiplier 饱和度倍率
     * @param effects             附带效果对
     * @return 食物属性
     */
    public static FoodProperties simple(int nutrition, float saturationMultiplier, EffectPair... effects) {
        FoodProperties.Builder builder = food(nutrition, saturationMultiplier);
        for (EffectPair effect : effects) {
            builder.effect(effect.getEffect(), effect.getProbability());
        }
        return builder.build();
    }

    /**
     * 创建始终可食用的食物属性。
     *
     * @param nutrition           营养值
     * @param saturationMultiplier 饱和度倍率
     * @return 食物属性
     */
    public static FoodProperties simpleAlwaysEdible(int nutrition, float saturationMultiplier) {
        return food(nutrition, saturationMultiplier)
                .alwaysEdible()
                .build();
    }

    /**
     * 创建始终可食用且附带效果的食物属性。
     *
     * @param nutrition           营养值
     * @param saturationMultiplier 饱和度倍率
     * @param effects             附带效果对
     * @return 食物属性
     */
    public static FoodProperties simpleAlwaysEdible(int nutrition, float saturationMultiplier, EffectPair... effects) {
        FoodProperties.Builder builder = food(nutrition, saturationMultiplier)
                .alwaysEdible();
        for (EffectPair effect : effects) {
            builder.effect(effect.getEffect(), effect.getProbability());
        }
        return builder.build();
    }

    /**
     * 创建使用后返还容器且附带效果的食物属性。
     *
     * @param nutrition           营养值
     * @param saturationMultiplier 饱和度倍率
     * @param container           使用后返还的容器物品
     * @param effects             附带效果对
     * @return 食物属性
     */
    public static FoodProperties simpleWithContainer(int nutrition, float saturationMultiplier,
                                                     Item container, EffectPair... effects) {
        FoodProperties.Builder builder = food(nutrition, saturationMultiplier).usingConvertsTo(container);
        for (EffectPair effect : effects) {
            builder.effect(effect.getEffect(), effect.getProbability());
        }
        return builder.build();
    }
}