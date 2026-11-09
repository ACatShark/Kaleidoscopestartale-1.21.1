package com.tequilacat.kaleidoscope_startale.event;

import com.tequilacat.kaleidoscope_startale.item.PetterItem;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * 拍拍器事件类。
 * <p>
 * 监听玩家对实体右键事件，当手持拍拍器右键生物时，
 * 触发拍击行为并按本次产出的物品总数扣除拍拍器耐久。
 */
public class PetterEvents {

    /**
     * 处理实体右键事件。
     * <p>
     * 若目标为活体实体且手持物品为拍拍器，则取消原交互并返回成功，
     * 执行拍击；若服务端且有产出，则按产出总数扣除耐久。
     *
     * @param event 实体右键事件
     */
    @SubscribeEvent
    public void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof LivingEntity target)) {
            return;
        }

        ItemStack stack = event.getItemStack();
        Item item = stack.getItem();
        if (!(item instanceof PetterItem petter)) {
            return;
        }

        Player player = event.getEntity();
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);

        int dropCount = petter.pet(player, target, stack);
        if (dropCount > 0 && !player.level().isClientSide) {
            // 耐久按本次挤出的物品总数扣除
            stack.hurtAndBreak(dropCount, player, LivingEntity.getSlotForHand(event.getHand()));
        }
    }
}