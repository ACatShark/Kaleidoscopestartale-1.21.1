package tequila.cat.pigpig.mixin;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.level.GameRules;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import tequila.cat.pigpig.init.ModEffects;

/**
 * 拥有嗜血效果的玩家不能自然回血。
 * FoodData#tick 里对 GameRules#getBoolean 只有一次调用，读的正是
 * RULE_NATURAL_REGENERATION，把它的结果改成 false 就能同时关掉
 * 饱食度满时按饱和度回血、饱食度 18 以上缓慢回血这两条路径，
 * 饥饿值消耗与饿死逻辑不受影响。
 */
@Mixin(FoodData.class)
public abstract class FoodDataMixin {
    @Redirect(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/GameRules;getBoolean(Lnet/minecraft/world/level/GameRules$Key;)Z"
            )
    )
    private boolean kaleidoscope$disableNaturalRegen(GameRules rules, GameRules.Key<GameRules.BooleanValue> key,
                                                     Player player) {
        return rules.getBoolean(key) && !player.hasEffect(ModEffects.BLOODTHIRST);
    }
}
