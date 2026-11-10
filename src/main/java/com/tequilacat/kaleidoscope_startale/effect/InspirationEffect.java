package com.tequilacat.kaleidoscope_startale.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** 灵感充沛：受伤时用经验抵扣伤害。 */
/**
 * 灵感充沛。
 * <p>
 * 受伤时用经验抵扣伤害。
 */
public class InspirationEffect extends MobEffect {

    /**
     * 构造灵感充沛效果。
     * <p>
     * 类别为益益，使用指定颜色。
     */
    public InspirationEffect(int color) {
        super(MobEffectCategory.BENEFICIAL, color);
    }
}
