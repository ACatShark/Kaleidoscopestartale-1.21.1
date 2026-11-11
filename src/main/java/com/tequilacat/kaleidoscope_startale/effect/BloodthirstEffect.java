package com.tequilacat.kaleidoscope_startale.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import com.tequilacat.kaleidoscope_startale.KaleidoscopeStartale;

/**
 * 嗜血
 * <p>
 * 造成伤害后回复相当于该伤害 70% 的生命，
 * 期间无法自然回血，攻击速度 +50%，饥饿值消耗显著加快。
 */
public class BloodthirstEffect extends MobEffect {

    /** 攻击速度加成（按总攻速的百分比叠加）。 */
    public static final double ATTACK_SPEED_BONUS = 0.5D;

    /** 饥饿消耗倍率：拥有嗜血时，所有饱食度消耗按此倍率放大。 */
    public static final float EXHAUSTION_MULTIPLIER = 2.0F;

    /**
     * 构造嗜血效果。
     * <p>
     * 类别为有益，使用指定颜色，并添加攻击速度属性修饰符。
     */
    public BloodthirstEffect(int color) {
        super(MobEffectCategory.BENEFICIAL, color);
        this.addAttributeModifier(Attributes.ATTACK_SPEED, KaleidoscopeStartale.id("bloodthirst_attack_speed"),
                ATTACK_SPEED_BONUS, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    }
}
