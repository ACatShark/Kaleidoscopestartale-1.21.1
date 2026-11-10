package com.tequilacat.kaleidoscope_startale.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * 牧场英雄。
 * <p>
 * 拍拍器产出提升、拍击幅度更大。
 */
public class RanchHeroEffect extends MobEffect {

    /**
     * 构造牧场英雄效果。
     * <p>
     * 类别为有益，使用指定颜色。
     */
    public RanchHeroEffect(int color) {
        super(MobEffectCategory.BENEFICIAL, color);
    }
}
