package tequila.cat.pigpig.mixin;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import tequila.cat.pigpig.init.ModEffects;

/**
 * 蓝调时刻：拥有该效果时，之后获得的每个状态效果持续时间 ×1.5。
 * 所有施加效果的路径（药水、食物、信标、命令、其它模组等）最终都会调用
 * LivingEntity#addEffect(MobEffectInstance, Entity)，所以只在这一处放大即可。
 * 蓝调时刻本身与无限时长的效果不受影响。
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    private static final float BLUE_HOUR_DURATION_MULTIPLIER = 1.5F;

    @ModifyVariable(
            method = "addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z",
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 0
    )
    private MobEffectInstance kaleidoscope$extendEffectDuration(MobEffectInstance instance) {
        if (instance == null || instance.isInfiniteDuration()) {
            return instance;
        }

        LivingEntity self = (LivingEntity) (Object) this;
        if (!self.hasEffect(ModEffects.BLUE_HOUR)) {
            return instance;
        }
        if (instance.getEffect().value() == ModEffects.BLUE_HOUR.get()) {
            return instance;
        }

        int duration = Math.max(1, Math.round((float) instance.getDuration() * BLUE_HOUR_DURATION_MULTIPLIER));
        return new MobEffectInstance(instance.getEffect(), duration, instance.getAmplifier(),
                instance.isAmbient(), instance.isVisible(), instance.showIcon());
    }
}
