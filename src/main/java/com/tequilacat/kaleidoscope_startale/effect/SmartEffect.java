package com.tequilacat.kaleidoscope_startale.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * 聪明。
 * <p>
 * 持续期间实体身上物品不损耗耐久，
 * 附魔与铁砧修补所消耗的经验减半。
 */
public class SmartEffect extends MobEffect {

    /**
     * 构造聪明效果。
     * <p>
     * 类别为有益，使用指定颜色。
     */
    public SmartEffect(int color) {
        super(MobEffectCategory.BENEFICIAL, color);
    }
}
