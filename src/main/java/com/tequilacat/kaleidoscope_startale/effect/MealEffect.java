package com.tequilacat.kaleidoscope_startale.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * 膳食。
 * <p>
 * 进食速度翻倍。
 */
public class MealEffect extends MobEffect {

    /**
     * 构造膳食效果。
     * <p>
     * 类别为有益，使用指定颜色。
     */
    public MealEffect(int color) {
        super(MobEffectCategory.BENEFICIAL, color);
    }
}
