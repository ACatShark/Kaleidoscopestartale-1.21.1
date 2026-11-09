package tequila.cat.pigpig.event;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.Saddleable;
import net.minecraft.world.entity.player.Player;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent.LivingTargetType;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import tequila.cat.pigpig.data.MobTaming;

/**
 * 驯养保护：已驯养的生物单次最多受到 25% 最大生命的伤害、
 * 免疫负面效果、不会着火、也不会把主人当目标。
 */
public class TamedProtectionEvents {
    public static final float MAX_DAMAGE_RATIO = 0.25F;

    @SubscribeEvent
    public void onIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity entity = event.getEntity();
        if (!MobTaming.isTamed(entity)) {
            return;
        }
        if (event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return;
        }

        float cap = entity.getMaxHealth() * MAX_DAMAGE_RATIO;
        if (event.getAmount() > cap) {
            event.setAmount(cap);
        }
    }

    @SubscribeEvent
    public void onEffectApplicable(MobEffectEvent.Applicable event) {
        LivingEntity entity = event.getEntity();
        if (!MobTaming.isTamed(entity)) {
            return;
        }

        MobEffectInstance instance = event.getEffectInstance();
        if (instance.getEffect().value().getCategory() != MobEffectCategory.HARMFUL) {
            return;
        }
        // 骑乘时的跳跃提升要保留，否则没法骑着走
        boolean jumpBoost = instance.getEffect().value() == MobEffects.JUMP.value();
        if (jumpBoost && entity instanceof Saddleable) {
            return;
        }
        event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
    }

    @SubscribeEvent
    public void onChangeTarget(LivingChangeTargetEvent event) {
        if (event.getTargetType() != LivingTargetType.MOB_TARGET) {
            return;
        }
        LivingEntity entity = event.getEntity();
        if (!MobTaming.isTamed(entity)) {
            return;
        }

        LivingEntity newTarget = event.getNewAboutToBeSetTarget();
        if (newTarget != null && MobTaming.isOwner(entity, newTarget)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof LivingEntity living)) {
            return;
        }
        if (living.level().isClientSide()) {
            return;
        }

        if (MobTaming.isTamed(living) && (living.isOnFire() || living.getRemainingFireTicks() > 0)) {
            living.clearFire();
        }

        if (!(living instanceof Mob mob)) {
            return;
        }

        Player targetPlayer = mob.getTarget() instanceof Player player ? player : null;
        NeutralMob neutral = mob instanceof NeutralMob n ? n : null;
        if (targetPlayer == null && neutral != null && neutral.getPersistentAngerTarget() != null) {
            targetPlayer = mob.level().getPlayerByUUID(neutral.getPersistentAngerTarget());
        }
        if (targetPlayer == null || !MobTaming.isTamed(living) || !MobTaming.isOwner(living, targetPlayer)) {
            return;
        }

        if (mob.getTarget() == targetPlayer) {
            mob.setTarget((LivingEntity) null);
        }
        if (neutral != null && neutral.getPersistentAngerTarget() != null) {
            neutral.stopBeingAngry();
        }
    }
}
