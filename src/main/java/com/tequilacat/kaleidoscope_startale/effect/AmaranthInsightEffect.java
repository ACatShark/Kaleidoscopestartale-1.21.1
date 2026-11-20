package com.tequilacat.kaleidoscope_startale.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * 苋见之明。
 * <p>
 * 拍拍器收获上限提高，拍扁幅度增大。
 */
public class AmaranthInsightEffect extends MobEffect {

    /**
     * 构造苋见之明效果。
     * <p>
     * 类别为有益，使用指定颜色。
     */
    public AmaranthInsightEffect(int color) {
        super(MobEffectCategory.BENEFICIAL, color);
    }
}
