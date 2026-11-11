package com.tequilacat.kaleidoscope_startale.event.effect;

import net.minecraft.world.entity.LivingEntity;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import com.tequilacat.kaleidoscope_startale.KaleidoscopeStartale;
import com.tequilacat.kaleidoscope_startale.init.KSEffects;

/**
 * 嗜血事件类。
 * <p>
 * 嗜血：造成伤害后回复相当于该伤害 70% 的生命。
 */
@EventBusSubscriber(modid = KaleidoscopeStartale.MODID)
public class BloodthirstEvents {

    /** 吸血比例。 */
    private static final float LIFESTEAL_RATIO = 0.7F;

    /**
     * 处理受伤后事件。
     * <p>
     * 若伤害来源为拥有嗜血效果的活体实体且非自身伤害，
     * 则按实际造成伤害的吸血比例回复攻击者生命。
     *
     * @param event 受伤后事件
     */
    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Post event) {
        if (!(event.getSource().getEntity() instanceof LivingEntity attacker)) {
            return;
        }
        if (attacker == event.getEntity() || !attacker.hasEffect(KSEffects.BLOODTHIRST)) {
            return;
        }

        float dealt = event.getNewDamage();
        if (dealt > 0.0F) {
            attacker.heal(dealt * LIFESTEAL_RATIO);
        }
    }
}