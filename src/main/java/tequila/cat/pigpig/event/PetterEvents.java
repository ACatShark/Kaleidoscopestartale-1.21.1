package tequila.cat.pigpig.event;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import tequila.cat.pigpig.item.PetterItem;

public class PetterEvents {
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
