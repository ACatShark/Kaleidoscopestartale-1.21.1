package com.tequilacat.kaleidoscope_startale.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** 灵感充沛：受伤时用经验抵扣伤害。 */
public class InspirationEffect extends MobEffect {

    /** 效果颜色。 */
     public static final int COLOR = 0x00F2C9CE;

    public InspirationEffect() {
        super(MobEffectCategory.BENEFICIAL, COLOR);
    }
}
