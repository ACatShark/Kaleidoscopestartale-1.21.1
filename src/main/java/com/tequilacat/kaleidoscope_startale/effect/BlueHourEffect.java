package com.tequilacat.kaleidoscope_startale.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * 蓝调时刻。
 * <p>
 * 之后获得的其它状态效果持续时间 ×1.5。
 */
public class BlueHourEffect extends MobEffect {

    /**
     * 构造蓝调时刻效果。
     * <p>
     * 类别为有益，使用指定颜色。
     */
    public BlueHourEffect(int color) {
        super(MobEffectCategory.BENEFICIAL, color);
    }
}
