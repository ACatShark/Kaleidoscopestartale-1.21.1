package tequila.cat.pigpig.mixin;

import java.util.function.Consumer;

import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import tequila.cat.pigpig.init.ModEffects;

/**
 * 聪明：拥有该效果的实体，身上物品不掉耐久。
 * 所有扣耐久的路径（手持使用、装备受击、盾牌格挡等）最终都会走到
 * ItemStack#hurtAndBreak(int, ServerLevel, LivingEntity, Consumer)，
 * 所以只在这一处拦截即可覆盖全部情况。
 *
 * 膳食：拥有该效果的实体，食用带 food 组件的物品所需时间减半。
 */
@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    private static final float MEAL_USE_DURATION_MULTIPLIER = 0.5F;

    @Inject(
            method = "hurtAndBreak(ILnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void kaleidoscope$skipDurabilityLoss(int amount, ServerLevel level,
                                                 LivingEntity entity, Consumer<Item> onBreak,
                                                 CallbackInfo ci) {
        if (entity != null && entity.hasEffect(ModEffects.SMART)) {
            ci.cancel();
        }
    }

    @Inject(
            method = "getUseDuration(Lnet/minecraft/world/entity/LivingEntity;)I",
            at = @At("RETURN"),
            cancellable = true
    )
    private void kaleidoscope$fasterEating(LivingEntity entity, CallbackInfoReturnable<Integer> cir) {
        ItemStack self = (ItemStack) (Object) this;
        if (entity == null || !self.has(DataComponents.FOOD) || !entity.hasEffect(ModEffects.MEAL)) {
            return;
        }

        int duration = cir.getReturnValue();
        int shortened = Math.round((float) duration * MEAL_USE_DURATION_MULTIPLIER);
        if (shortened < duration) {
            cir.setReturnValue(Math.max(2, shortened));
        }
    }
}
