package com.tequilacat.kaleidoscope_startale.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * 祝尼魔的祝福。
 * <p>
 * 破坏作物时额外收获、手持镰刀范围收割、拾取范围扩大。
 */
public class JunimoBlessingEffect extends MobEffect {

    /**
     * 构造祝尼魔的祝福效果。
     * <p>
     * 类别为有益，使用指定颜色。
     */
    public JunimoBlessingEffect(int color) {
        super(MobEffectCategory.BENEFICIAL, color);
    }
}
