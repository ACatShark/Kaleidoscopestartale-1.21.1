package com.tequilacat.kaleidoscope_startale.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/**
 * 余韵。
 * <p>
 * 每 3 秒为受影响的实体恢复 1 点饱食度与 1 点生命值；
 * 饱食度恢复仅对玩家生效，生命值恢复对所有生物生效。
 */
public class LingeringEffect extends MobEffect {

    /**
     * 构造余韵效果。
     *
     * @param color 效果的颜色值
     */
    public LingeringEffect(int color) {
        super(MobEffectCategory.BENEFICIAL, color);
    }

    /**
     * 判断当前刻是否应当执行效果逻辑。
     * <p>
     * 仅在剩余时长能被 60 整除时执行，即每 3 秒触发一次。
     *
     * @param duration  剩余持续时间（刻）
     * @param amplifier 效果等级
     * @return 应当执行返回 true
     */
    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration % 60 == 0;
    }

    /**
     * 执行效果逻辑。
     * <p>
     * 若受影响实体为玩家，则恢复 1 点饱食度；
     * 随后为所有生物恢复 1 点生命值。
     *
     * @param entity    受影响的生物实体
     * @param amplifier 效果等级
     * @return 始终返回 true
     */
    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity instanceof Player player) {
            player.getFoodData().eat(1, 0);
        }
        entity.heal(1);
        return true;
    }
}