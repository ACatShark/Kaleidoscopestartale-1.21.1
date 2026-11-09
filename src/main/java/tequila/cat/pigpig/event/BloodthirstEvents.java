package tequila.cat.pigpig.event;

import net.minecraft.world.entity.LivingEntity;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import tequila.cat.pigpig.KaleidoscopeStartale;
import tequila.cat.pigpig.init.ModEffects;

/** 嗜血：造成伤害后回复相当于该伤害 80% 的生命。 */
@EventBusSubscriber(modid = KaleidoscopeStartale.MODID, bus = EventBusSubscriber.Bus.GAME)
public class BloodthirstEvents {
    private static final float LIFESTEAL_RATIO = 0.8F;

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Post event) {
        if (!(event.getSource().getEntity() instanceof LivingEntity attacker)) {
            return;
        }
        if (attacker == event.getEntity() || !attacker.hasEffect(ModEffects.BLOODTHIRST)) {
            return;
        }

        float dealt = event.getNewDamage();
        if (dealt > 0.0F) {
            attacker.heal(dealt * LIFESTEAL_RATIO);
        }
    }
}
