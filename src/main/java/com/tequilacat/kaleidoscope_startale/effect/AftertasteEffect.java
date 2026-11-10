package com.tequilacat.kaleidoscope_startale.effect;

import com.tequilacat.kaleidoscope_startale.init.KSEffects;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;

/**
 * 回甘。
 * <p>
 * 降低饱食度消耗。
 * 效果结束时自动为实体衔接提神效果。
 */
public class AftertasteEffect extends MobEffect {

    /**
     * 构造回甘效果。
     *
     * @param color 效果的颜色值
     */
    public AftertasteEffect(int color) {
        super(MobEffectCategory.BENEFICIAL, color);
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
     * 当效果剩余时长不足 1 刻时，为实体附加一段提神效果并提前返回，
     * 实现从回甘到提神的衔接；
     * 否则若受影响实体为玩家，则将其饱食度乘以 0.85。
     *
     * @param entity    受影响的生物实体
     * @param amplifier 效果等级
     * @return 始终返回 true
     */
    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        MobEffectInstance instance = entity.getEffect(KSEffects.AFTER_TASTE);
        if (instance != null && instance.getDuration() <= 1) {
            entity.addEffect(new MobEffectInstance(KSEffects.INVIGORATING, 20 * 60, 0));
            return true;
        }
        if (entity instanceof Player player) {
            FoodData foodData = player.getFoodData();
            foodData.setExhaustion(foodData.getExhaustionLevel() * 0.85f);
        }
        return true;
    }
}