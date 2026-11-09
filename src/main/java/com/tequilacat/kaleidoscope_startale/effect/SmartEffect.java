package com.tequilacat.kaleidoscope_startale.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** 聪明：持续期间实体身上物品不损耗耐久。 */
public class SmartEffect extends MobEffect {

    /** 效果颜色。 */
    public static final int COLOR = 0x0069735D;

    /**
     * 构造聪明效果。
     * <p>
     * 类别为有益，使用指定颜色。
     */
    public SmartEffect() {
        super(MobEffectCategory.BENEFICIAL, COLOR);
    }
}
