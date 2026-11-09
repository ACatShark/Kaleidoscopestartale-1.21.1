package com.tequilacat.kaleidoscope_startale.event;

import com.tequilacat.kaleidoscope_startale.data.tame.MobTaming;
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
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/**
 * 已驯养生物保护事件类。
 * <p>
 * 为已驯养生物提供多项保护：限制单次受到的最大伤害比例、
 * 阻止有害状态效果生效、阻止将主人设为目标，
 * 并在每刻清除着火与对主人的仇恨。
 */
public class TamedProtectionEvents {

    /**
     * 单次受伤的最大生命值比例上限。
     */
    public static final float MAX_DAMAGE_RATIO = 0.25F;

    /**
     * 处理受到伤害事件。
     * <p>
     * 若受击者为已驯养生物且伤害不绕过无敌判定，
     * 则将伤害限制在最大生命值的四分之一以内。
     *
     * @param event 受伤事件
     */
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

    /**
     * 处理状态效果生效事件。
     * <p>
     * 若目标为已驯养生物且效果为有害类别，则阻止其生效；
     * 但骑乘状态下的跳跃提升需保留，否则无法骑乘移动。
     *
     * @param event 状态效果生效事件
     */
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

    /**
     * 处理目标变更事件。
     * <p>
     * 若目标变更为已驯养生物的主人，则取消该次目标设置。
     *
     * @param event 目标变更事件
     */
    @SubscribeEvent
    public void onChangeTarget(LivingChangeTargetEvent event) {
        if (event.getTargetType() != LivingChangeTargetEvent.LivingTargetType.MOB_TARGET) {
            return;
        }
        LivingEntity entity = event.getEntity();
        if (!MobTaming.isTamed(entity)) {
            return;
        }

        LivingEntity newTarget = event.getNewAboutToBeSetTarget();
        if (MobTaming.isOwner(entity, newTarget)) {
            event.setCanceled(true);
        }
    }

    /**
     * 处理实体每刻更新事件。
     * <p>
     * 服务端下，已驯养生物会被清除着火状态；
     * 若其目标或持续仇恨目标是主人，则清除目标并停止愤怒。
     *
     * @param event 实体每刻更新事件
     */
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
        if (!MobTaming.isTamed(living) || !MobTaming.isOwner(living, targetPlayer)) {
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