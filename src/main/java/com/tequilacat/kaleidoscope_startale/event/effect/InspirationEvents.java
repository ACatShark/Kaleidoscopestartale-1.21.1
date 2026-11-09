package com.tequilacat.kaleidoscope_startale.event.effect;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;

import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import com.tequilacat.kaleidoscope_startale.init.KSAttachments;
import com.tequilacat.kaleidoscope_startale.init.KSEffects;

/**
 * 灵感事件类。
 * <p>
 * 灵感效果生效期间，玩家受到伤害时消耗经验抵扣伤害；
 * 灵感结束后施加持续 180 秒的灵感枯竭效果，
 * 且灵感枯竭期间无法再次获得灵感。
 */
public class InspirationEvents {

    /** 每点伤害消耗的经验值。 */
    public static final int XP_PER_DAMAGE = 10;

    /** 灵感枯竭持续秒数。 */
    public static final int DROUGHT_SECONDS = 180;

    /** 灵感枯竭持续刻数。 */
    private static final int DROUGHT_TICKS = 3600;

    /**
     * 处理受伤前事件。
     * <p>
     * 若玩家拥有灵感效果且伤害有效，则按每点伤害消耗经验抵扣伤害，
     * 经验不足时只能抵扣部分伤害。
     *
     * @param event 受伤前事件
     */
    @SubscribeEvent
    public void onDamagePre(LivingDamageEvent.Pre event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (player.level().isClientSide() || player.isCreative() || player.isSpectator()) {
            return;
        }
        if (!player.hasEffect(KSEffects.INSPIRATION)) {
            return;
        }
        if (event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return;
        }

        float damage = event.getNewDamage();
        if (damage <= 0.0F) {
            return;
        }

        int xpNeeded = (int) Math.ceil((double) (damage * (float) XP_PER_DAMAGE));
        int spent = Math.min(player.totalExperience, xpNeeded);
        if (spent > 0) {
            player.giveExperiencePoints(-spent);
            float covered = (float) spent / (float) XP_PER_DAMAGE;
            event.setNewDamage(Math.max(0.0F, damage - covered));
        }
    }

    /**
     * 处理状态效果生效事件。
     * <p>
     * 灵感枯竭始终可生效；已有灵感或灵感枯竭时阻止灵感再次生效。
     *
     * @param event 状态效果生效事件
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onEffectApplicable(MobEffectEvent.Applicable event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        MobEffectInstance instance = event.getEffectInstance();
        if (instance.getEffect().value() == KSEffects.INSPIRATION_DROUGHT.get()) {
            event.setResult(MobEffectEvent.Applicable.Result.APPLY);
        } else if (instance.getEffect().value() == KSEffects.INSPIRATION.get()
                && (player.hasEffect(KSEffects.INSPIRATION_DROUGHT) || player.hasEffect(KSEffects.INSPIRATION))) {
            event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
        }
    }

    /**
     * 处理玩家每刻更新事件。
     * <p>
     * 检测灵感效果从有到无的变化，记录状态并在灵感结束时
     * 强制施加灵感枯竭效果。
     *
     * @param event 实体每刻更新事件
     */
    @SubscribeEvent
    public void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (player.level().isClientSide() || !player.isAlive()) {
            return;
        }

        boolean now = player.hasEffect(KSEffects.INSPIRATION);
        boolean before = player.getData(KSAttachments.HAD_INSPIRATION.get());
        if (now == before) {
            return;
        }

        player.setData(KSAttachments.HAD_INSPIRATION.get(), now);
        if (before) {
            player.forceAddEffect(new MobEffectInstance(KSEffects.INSPIRATION_DROUGHT, DROUGHT_TICKS), player);
        }
    }
}