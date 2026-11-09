package com.tequilacat.kaleidoscope_startale.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** 蓝调时刻：之后获得的其它状态效果持续时间 ×1.5（逻辑在 LivingEntityMixin）。 */
public class BlueHourEffect extends MobEffect {

    /** 效果颜色。 */
     public static final int COLOR = 0x00324B8C;

    /**
     * 构造蓝调时刻效果。
     * <p>
     * 类别为有益，使用指定颜色。
     */
    public BlueHourEffect() {
        super(MobEffectCategory.BENEFICIAL, COLOR);
    }
}
