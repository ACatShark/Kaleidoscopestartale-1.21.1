package tequila.cat.pigpig.event;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.Animal;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import tequila.cat.pigpig.KaleidoscopeStartale;
import tequila.cat.pigpig.data.FavorabilityTracker;

/** 每秒给所有动物做一次好感度日结算（内部会判断是否跨天）。 */
@EventBusSubscriber(modid = KaleidoscopeStartale.MODID, bus = EventBusSubscriber.Bus.GAME)
public class FavorabilityEvents {
    private static final int CHECK_INTERVAL = 20;

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof Animal animal)) {
            return;
        }
        if (animal.level() instanceof ServerLevel level && animal.tickCount % CHECK_INTERVAL == 0) {
            FavorabilityTracker.settleDaily(level, animal);
        }
    }
}
