package com.tequilacat.kaleidoscope_startale.mixin;

import com.tequilacat.kaleidoscope_startale.init.KSEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * 生物实体混入类。
 * <p>
 * 在生物获得状态效果时进行干预，若生物已拥有蓝时效果，
 * 则延长新获得效果的持续时间。
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    /**
     * 蓝时效果持续时间倍率。
     */
    @Unique
    private static final float BLUE_HOUR_DURATION_MULTIPLIER = 1.5F;

    /**
     * 延长状态效果持续时间。
     * <p>
     * 拦截 addEffect 方法的参数，若目标生物已拥有蓝时效果，
     * 且新效果不是蓝时自身，则将新效果的持续时间乘以倍率。
     *
     * @param instance 即将添加的状态效果实例
     * @return 可能被延长持续时间后的效果实例
     */
    @ModifyVariable(
            method = "addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z",
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 0
    )
    private MobEffectInstance KS$extendEffectDuration(MobEffectInstance instance) {
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

        int duration = Math.max(1, Math.round((float) instance.getDuration() * BLUE_HOUR_DURATION_MULTIPLIER));
        return new MobEffectInstance(instance.getEffect(), duration, instance.getAmplifier(),
                instance.isAmbient(), instance.isVisible(), instance.showIcon());
    }
}