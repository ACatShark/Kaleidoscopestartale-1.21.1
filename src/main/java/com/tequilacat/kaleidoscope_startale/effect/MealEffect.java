package com.tequilacat.kaleidoscope_startale.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** 膳食：进食速度翻倍。 */
public class MealEffect extends MobEffect {

    /** 效果颜色。 */
     public static final int COLOR = 0x00D9A441;

    /**
     * 构造膳食效果。
     * <p>
     * 类别为有益，使用指定颜色。
     */
    public MealEffect() {
        super(MobEffectCategory.BENEFICIAL, COLOR);
    }
}
