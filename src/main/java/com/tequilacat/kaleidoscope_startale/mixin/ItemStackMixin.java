package com.tequilacat.kaleidoscope_startale.mixin;

import com.tequilacat.kaleidoscope_startale.init.KSEffects;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Consumer;

/**
 * 物品堆叠混入类。
 * <p>
 * 在物品损耗与使用时长上应用状态效果相关的修改，
 * 包括智能效果免疫耐久损耗与餐食效果加速进食。
 */
@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    /**
     * 餐食效果使用时长倍率。
     */
    @Unique
    private static final float MEAL_USE_DURATION_MULTIPLIER = 0.5F;

    /**
     * 跳过耐久损耗。
     * <p>
     * 若持有实体拥有智能效果，则取消 hurtAndBreak 调用，避免物品损耗。
     *
     * @param amount   损耗量
     * @param level    服务端世界
     * @param entity   持有实体
     * @param onBreak  物品损坏回调
     * @param ci       回调信息
     */
    @Inject(
            method = "hurtAndBreak(ILnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void KS$skipDurabilityLoss(int amount, ServerLevel level, LivingEntity entity, Consumer<Item> onBreak, CallbackInfo ci) {
        if (entity != null && entity.hasEffect(KSEffects.SMART)) {
            ci.cancel();
        }
    }

    /**
     * 加速进食。
     * <p>
     * 若使用实体拥有餐食效果且物品为食物，则将使用时长乘以倍率缩短，
     * 并保证缩短后时长不小于 2。
     *
     * @param entity 使用实体
     * @param cir    返回值回调信息
     */
    @Inject(
            method = "getUseDuration(Lnet/minecraft/world/entity/LivingEntity;)I",
            at = @At("RETURN"),
            cancellable = true
    )
    private void KS$fasterEating(LivingEntity entity, CallbackInfoReturnable<Integer> cir) {
        ItemStack self = (ItemStack) (Object) this;
        if (entity == null || !self.has(DataComponents.FOOD) || !entity.hasEffect(KSEffects.MEAL)) {
            return;
        }

        int duration = cir.getReturnValue();
        int shortened = Math.round((float) duration * MEAL_USE_DURATION_MULTIPLIER);
        if (shortened < duration) {
            cir.setReturnValue(Math.max(2, shortened));
        }
    }
}