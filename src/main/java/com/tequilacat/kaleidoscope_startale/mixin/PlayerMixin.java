package com.tequilacat.kaleidoscope_startale.mixin;

import com.tequilacat.kaleidoscope_startale.effect.BloodthirstEffect;
import com.tequilacat.kaleidoscope_startale.init.KSEffects;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * 玩家混入类。
 * <p>
 * 嗜血：放大玩家的饥饿消耗；
 * 聪明：附魔时消耗的经验等级减半。
 */
@Mixin(Player.class)
public abstract class PlayerMixin {

    /**
     * 放大饥饿消耗。
     * <p>
     * 玩家拥有嗜血效果时，把每次增加的饥饿消耗按倍率放大。
     *
     * @param exhaustion 原始饥饿消耗
     * @return 放大后的饥饿消耗
     */
    @ModifyVariable(
            method = "causeFoodExhaustion(F)V",
            at = @At("HEAD"),
            argsOnly = true
    )
    private float KS$amplifyExhaustion(float exhaustion) {
        Player self = (Player) (Object) this;
        if (!self.hasEffect(KSEffects.BLOODTHIRST)) {
            return exhaustion;
        }
        return exhaustion * BloodthirstEffect.EXHAUSTION_MULTIPLIER;
    }

    /**
     * 附魔经验减半。
     * <p>
     * 玩家拥有聪明效果时，附魔台扣除的经验等级减半（至少扣除 1 级）。
     *
     * @param levelCost 原本要扣除的经验等级
     * @return 减半后的经验等级
     */
    @ModifyVariable(
            method = "onEnchantmentPerformed(Lnet/minecraft/world/item/ItemStack;I)V",
            at = @At("HEAD"),
            argsOnly = true
    )
    private int KS$halveEnchantmentCost(int levelCost) {
        Player self = (Player) (Object) this;
        if (levelCost <= 1 || !self.hasEffect(KSEffects.SMART)) {
            return levelCost;
        }
        return Math.max(1, levelCost / 2);
    }
}
