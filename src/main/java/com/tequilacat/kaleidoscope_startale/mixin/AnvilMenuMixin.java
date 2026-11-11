package com.tequilacat.kaleidoscope_startale.mixin;

import com.tequilacat.kaleidoscope_startale.init.KSEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.DataSlot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 铁砧菜单混入类。
 * <p>
 * 聪明：修补、重命名与合并附魔所消耗的经验等级减半。
 */
@Mixin(AnvilMenu.class)
public abstract class AnvilMenuMixin {

    /** 铁砧当前计算出的经验消耗。 */
    @Shadow
    @Final
    private DataSlot cost;

    /**
     * 经验消耗减半。
     * <p>
     * 在每次重算结果后介入，若玩家拥有聪明效果且消耗大于 1，
     * 则把消耗减半（至少为 1），界面显示与实际扣除同时生效。
     *
     * @param callbackInfo 回调信息
     */
    @Inject(method = "createResult", at = @At("TAIL"))
    private void KS$halveAnvilCost(CallbackInfo callbackInfo) {
        int current = this.cost.get();
        if (current <= 1) {
            return;
        }
        Player player = ((ItemCombinerMenuAccessor) this).KS$getPlayer();
        if (player == null || !player.hasEffect(KSEffects.SMART)) {
            return;
        }
        this.cost.set(Math.max(1, current / 2));
    }
}
