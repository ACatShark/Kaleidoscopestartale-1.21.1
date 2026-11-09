package com.tequilacat.kaleidoscope_startale.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** 牧场英雄：拍拍器产出提升、拍击幅度更大。 */
public class RanchHeroEffect extends MobEffect {

    /** 效果颜色。 */
    public static final int COLOR = 0x00E0A63C;

    /**
     * 构造牧场英雄效果。
     * <p>
     * 类别为有益，使用指定颜色。
     */
    public RanchHeroEffect() {
        super(MobEffectCategory.BENEFICIAL, COLOR);
    }
}
