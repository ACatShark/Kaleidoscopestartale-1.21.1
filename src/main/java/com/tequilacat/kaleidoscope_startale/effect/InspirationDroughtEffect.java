package com.tequilacat.kaleidoscope_startale.effect;

import java.util.Set;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;

import net.neoforged.neoforge.common.EffectCure;
import net.neoforged.neoforge.common.EffectCures;

/** 灵感枯竭：只能用不死图腾解除，期间无法再获得灵感充沛。 */
public class InspirationDroughtEffect extends MobEffect {

    /** 效果颜色。 */
     public static final int COLOR = 0x006B5F3B;

    /**
     * 允许的解除方式集合，仅限不死图腾保护。
     */
    private static final Set<EffectCure> ALLOWED_CURES = Set.of(EffectCures.PROTECTED_BY_TOTEM);

    /**
     * 构造灵感枯竭效果。
     * <p>
     * 类别为有害，使用指定颜色。
     */
    public InspirationDroughtEffect() {
        super(MobEffectCategory.HARMFUL, COLOR);
    }

    /**
     * 填充效果的解除方式。
     * <p>
     * 清空默认解除方式，仅保留不死图腾保护这一种。
     *
     * @param cures         解除方式集合
     * @param effectInstance 效果实例
     */
    @Override
    public void fillEffectCures(Set<EffectCure> cures, MobEffectInstance effectInstance) {
        cures.clear();
        cures.addAll(ALLOWED_CURES);
    }
}
