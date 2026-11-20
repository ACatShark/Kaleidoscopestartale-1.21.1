package com.tequilacat.kaleidoscope_startale.mixin;

import com.tequilacat.kaleidoscope_startale.init.KSEffects;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * 生物实体混入类。
 * <p>
 * 在生物获得状态效果时进行干预：若生物已拥有蓝调时刻效果，
 * 则让新获得的增益效果持续时间翻倍、负面效果持续时间减半。
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    /**
     * 蓝调时刻期间，增益效果的持续时间倍率。
     */
    @Unique
    private static final float BLUE_HOUR_BENEFICIAL_MULTIPLIER = 2.0F;

    /**
     * 蓝调时刻期间，负面效果的持续时间倍率。
     */
    @Unique
    private static final float BLUE_HOUR_HARMFUL_MULTIPLIER = 0.5F;

    /**
     * 调整状态效果持续时间。
     * <p>
     * 拦截 addEffect 方法的参数，若目标生物已拥有蓝调时刻效果，
     * 且新效果不是蓝调时刻自身：增益型时长翻倍，负面型时长减半，
     * 中性效果保持原时长。
     *
     * @param instance 即将添加的状态效果实例
     * @return 可能被调整过持续时间的效果实例
     */
    @ModifyVariable(
            method = "addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z",
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 0
    )
    private MobEffectInstance KS$adjustEffectDuration(MobEffectInstance instance) {
        if (instance == null || instance.isInfiniteDuration()) {
            return instance;
        }

        LivingEntity self = (LivingEntity) (Object) this;
        if (!self.hasEffect(KSEffects.BLUE_HOUR)) {
            return instance;
        }
        if (instance.getEffect().value() == KSEffects.BLUE_HOUR.get()) {
            return instance;
        }

        MobEffectCategory category = instance.getEffect().value().getCategory();
        float multiplier;
        if (category == MobEffectCategory.BENEFICIAL) {
            multiplier = BLUE_HOUR_BENEFICIAL_MULTIPLIER;
        } else if (category == MobEffectCategory.HARMFUL) {
            multiplier = BLUE_HOUR_HARMFUL_MULTIPLIER;
        } else {
            return instance;
        }

        int duration = Math.max(1, Math.round((float) instance.getDuration() * multiplier));
        return new MobEffectInstance(instance.getEffect(), duration, instance.getAmplifier(),
                instance.isAmbient(), instance.isVisible(), instance.showIcon());
    }
}
