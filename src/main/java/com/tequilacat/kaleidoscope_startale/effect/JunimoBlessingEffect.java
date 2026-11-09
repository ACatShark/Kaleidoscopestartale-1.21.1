package com.tequilacat.kaleidoscope_startale.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * 祝尼魔的祝福：破坏作物时额外收获、手持镰刀范围收割、拾取范围扩大
 */
public class JunimoBlessingEffect extends MobEffect {

    /** 效果颜色。 */
     public static final int COLOR = 0x0077C043;

    /**
     * 构造祝尼魔的祝福效果。
     * <p>
     * 类别为有益，使用指定颜色。
     */
    public JunimoBlessingEffect() {
        super(MobEffectCategory.BENEFICIAL, COLOR);
    }
}
