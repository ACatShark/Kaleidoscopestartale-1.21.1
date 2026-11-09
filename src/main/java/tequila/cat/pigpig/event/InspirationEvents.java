package tequila.cat.pigpig.event;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import tequila.cat.pigpig.attachment.ModAttachments;
import tequila.cat.pigpig.init.ModEffects;

/**
 * 灵感充沛：受伤时按 10 点经验 / 1 点伤害抵扣；
 * 效果结束后自动转为「灵感枯」，期间不能再获得灵感充沛。
 */
public class InspirationEvents {
    public static final int XP_PER_DAMAGE = 10;
    public static final int DROUGHT_SECONDS = 180;
    private static final int DROUGHT_TICKS = 3600;

    @SubscribeEvent
    public void onDamagePre(LivingDamageEvent.Pre event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (player.level().isClientSide() || player.isCreative() || player.isSpectator()) {
            return;
        }
        if (!player.hasEffect(ModEffects.INSPIRATION)) {
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

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onEffectApplicable(MobEffectEvent.Applicable event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        MobEffectInstance instance = event.getEffectInstance();
        if (instance.getEffect().value() == ModEffects.INSPIRATION_DROUGHT.get()) {
            event.setResult(MobEffectEvent.Applicable.Result.APPLY);
        } else if (instance.getEffect().value() == ModEffects.INSPIRATION.get()
                && (player.hasEffect(ModEffects.INSPIRATION_DROUGHT) || player.hasEffect(ModEffects.INSPIRATION))) {
            event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
        }
    }

    @SubscribeEvent
    public void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (player.level().isClientSide() || !player.isAlive()) {
            return;
        }

        boolean now = player.hasEffect(ModEffects.INSPIRATION);
        boolean before = player.getData(ModAttachments.HAD_INSPIRATION.get());
        if (now == before) {
            return;
        }

        player.setData(ModAttachments.HAD_INSPIRATION.get(), now);
        if (before) {
            player.forceAddEffect(new MobEffectInstance(ModEffects.INSPIRATION_DROUGHT, DROUGHT_TICKS), player);
        }
    }
}
