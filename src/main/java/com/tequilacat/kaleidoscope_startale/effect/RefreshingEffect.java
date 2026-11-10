package com.tequilacat.kaleidoscope_startale.effect;

import com.tequilacat.kaleidoscope_startale.KaleidoscopeStartale;
import com.tequilacat.kaleidoscope_startale.init.KSEffects;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * 清爽。
 * <p>
 * 提升移动速度与挖掘速度。
 * 效果结束时自动为实体衔接回甘效果。
 */
public class RefreshingEffect extends MobEffect {

    /**
     * 构造清爽效果。
     * <p>
     * 注册移动速度与挖掘速度两项属性修饰符，
     * 均采用基于基础值的乘算加成方式，数值为 0.15。
     *
     * @param color 效果的颜色值
     */
    public RefreshingEffect(int color) {
        super(MobEffectCategory.BENEFICIAL, color);
        addAttributeModifier(Attributes.MOVEMENT_SPEED, KaleidoscopeStartale.id("refreshing_movement"),
                0.15, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        addAttributeModifier(Attributes.BLOCK_BREAK_SPEED, KaleidoscopeStartale.id("refreshing_mining"),
                0.15, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
    }

    /**
     * 判断当前刻是否应当执行效果逻辑。
     * <p>
     * 仅在剩余时长为 1 刻或每满 20 刻（即每秒）时执行一次，
     * 以减少不必要的调用。
     *
     * @param duration  剩余持续时间（刻）
     * @param amplifier 效果等级
     * @return 应当执行返回 true
     */
    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration <= 1 || duration % 20 == 0;
    }

    /**
     * 执行效果逻辑。
     * <p>
     * 当效果剩余时长不足 1 刻时，为实体附加一段回甘效果，
     * 实现从清爽到回甘的衔接。
     *
     * @param entity    受影响的生物实体
     * @param amplifier 效果等级
     * @return 始终返回 true
     */
    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        MobEffectInstance instance = entity.getEffect(KSEffects.REFRESHING);
        if (instance != null && instance.getDuration() <= 1) {
            entity.addEffect(new MobEffectInstance(KSEffects.AFTER_TASTE, 2 * 60 * 20, 0));
        }
        return true;
    }
}