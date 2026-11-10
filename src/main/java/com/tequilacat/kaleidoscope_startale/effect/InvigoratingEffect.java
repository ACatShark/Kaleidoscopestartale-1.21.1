package com.tequilacat.kaleidoscope_startale.effect;

import com.tequilacat.kaleidoscope_startale.init.KSEffects;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

/**
 * 提神。
 * <p>
 * 免疫部分负面效果。
 * 效果结束时自动为实体衔接余韵效果。
 */
public class InvigoratingEffect extends MobEffect {

    /**
     * 构造提神效果。
     *
     * @param color 效果的颜色值
     */
    public InvigoratingEffect(int color) {
        super(MobEffectCategory.BENEFICIAL, color);
    }

    /**
     * 判断当前刻是否应当执行效果逻辑。
     * <p>
     * 提神效果需要每个游戏刻都执行，以持续清除负面效果。
     *
     * @param duration  剩余持续时间（刻）
     * @param amplifier 效果等级
     * @return 始终返回 true
     */
    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    /**
     * 执行效果逻辑。
     * <p>
     * 每刻移除缓慢、挖掘疲劳、反胃、失明与虚弱等负面效果；
     * 当效果剩余时长不足 1 刻时，为实体附加一段余韵效果，
     * 实现从提神到余韵的衔接。
     *
     * @param entity    受影响的生物实体
     * @param amplifier 效果等级
     * @return 始终返回 true
     */
    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        entity.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
        entity.removeEffect(MobEffects.DIG_SLOWDOWN);
        entity.removeEffect(MobEffects.CONFUSION);
        entity.removeEffect(MobEffects.BLINDNESS);
        entity.removeEffect(MobEffects.WEAKNESS);

        MobEffectInstance instance = entity.getEffect(KSEffects.INVIGORATING);
        if (instance != null && instance.getDuration() <= 1) {
            entity.addEffect(new MobEffectInstance(KSEffects.LINGERING, 20 * 30, 0));
        }
        return true;
    }
}