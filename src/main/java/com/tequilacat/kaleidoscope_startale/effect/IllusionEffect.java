package com.tequilacat.kaleidoscope_startale.effect;

import com.tequilacat.kaleidoscope_startale.KaleidoscopeStartale;
import com.tequilacat.kaleidoscope_startale.init.KSEffects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * 幻梦。
 * <p>
 * 获得创造飞行的能力，最大生命值缩减 50%，体型缩减 50%，
 * 并移除空中挖掘惩罚。
 */
@EventBusSubscriber(modid = KaleidoscopeStartale.MODID)
public class IllusionEffect extends MobEffect {

    /** 创造飞行属性修饰符 ID */
    private static final ResourceLocation CREATIVE_FLIGHT_MODIFIER = KaleidoscopeStartale.id("illusion_creative_flight");
    /** 最大生命值缩减修饰符 ID */
    private static final ResourceLocation MAX_HEALTH_MODIFIER = KaleidoscopeStartale.id("illusion_max_health");
    /** 体型缩减修饰符 ID */
    private static final ResourceLocation SCALE_MODIFIER = KaleidoscopeStartale.id("illusion_scale");
    /** 空中挖掘速度修饰符 ID */
    private static final ResourceLocation AIR_MINING_MODIFIER = KaleidoscopeStartale.id("illusion_air_mining");

    /** 最大生命缩减比例（50%） */
    private static final double MAX_HEALTH_REDUCTION = -0.5D;
    /** 体型缩减比例（50%） */
    private static final double SCALE_REDUCTION = -0.5D;

    public IllusionEffect(int color) {
        super(MobEffectCategory.BENEFICIAL, color);
        addAttributeModifier(NeoForgeMod.CREATIVE_FLIGHT,
                CREATIVE_FLIGHT_MODIFIER, 1, AttributeModifier.Operation.ADD_VALUE);
        addAttributeModifier(Attributes.MAX_HEALTH,
                MAX_HEALTH_MODIFIER, MAX_HEALTH_REDUCTION, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        addAttributeModifier(Attributes.SCALE,
                SCALE_MODIFIER, SCALE_REDUCTION, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.getEffect(KSEffects.ILLUSION) == null) return;
        var attr = player.getAttribute(Attributes.BLOCK_BREAK_SPEED);
        if (attr == null) return;
        if (!player.onGround()) {
            if (attr.getModifier(AIR_MINING_MODIFIER) == null) {
                attr.addTransientModifier(new AttributeModifier(
                        AIR_MINING_MODIFIER,
                        5,
                        AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
                ));
            }
        } else {
            attr.removeModifier(AIR_MINING_MODIFIER);
        }
    }

    @SubscribeEvent
    public static void onEffectRemove(MobEffectEvent.Remove event) {
        if (event.getEffect().value() instanceof IllusionEffect) {
            cleanupAirMiningModifier(event.getEntity());
        }
    }

    @SubscribeEvent
    public static void onEffectExpire(MobEffectEvent.Expired event) {
        if (event.getEffectInstance() != null
                && event.getEffectInstance().getEffect().value() instanceof IllusionEffect) {
            cleanupAirMiningModifier(event.getEntity());
        }
    }

    private static void cleanupAirMiningModifier(LivingEntity entity) {
        if (entity == null) return;
        var attr = entity.getAttribute(Attributes.BLOCK_BREAK_SPEED);
        if (attr != null) {
            attr.removeModifier(AIR_MINING_MODIFIER);
        }
    }
}