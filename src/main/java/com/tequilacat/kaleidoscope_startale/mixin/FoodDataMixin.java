package com.tequilacat.kaleidoscope_startale.mixin;

import com.tequilacat.kaleidoscope_startale.init.KSEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.level.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 饥饿数据混入类。
 * <p>
 * 在饥饿数据 tick 时干预自然恢复规则判定，
 * 使拥有嗜血效果的玩家无法触发自然恢复。
 */
@Mixin(FoodData.class)
public abstract class FoodDataMixin {

    /**
     * 禁用自然恢复。
     * <p>
     * 重定向 GameRules.getBoolean 调用，在拥有嗜血效果时返回 false，
     * 从而阻止自然恢复判定通过。
     *
     * @param rules  游戏规则
     * @param key    规则键
     * @param player 玩家
     * @return 是否启用自然恢复
     */
    @Redirect(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/GameRules;getBoolean(Lnet/minecraft/world/level/GameRules$Key;)Z"
            )
    )
    private boolean kaleidoscope$disableNaturalRegen(GameRules rules, GameRules.Key<GameRules.BooleanValue> key, Player player) {
        return rules.getBoolean(key) && !player.hasEffect(KSEffects.BLOODTHIRST);
    }
}