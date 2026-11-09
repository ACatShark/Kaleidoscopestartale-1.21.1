package com.tequilacat.kaleidoscope_startale.event;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.Animal;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import com.tequilacat.kaleidoscope_startale.KaleidoscopeStartale;
import com.tequilacat.kaleidoscope_startale.data.favorability.FavorabilityTracker;

/**
 * 好感度事件类。
 * <p>
 * 监听动物每刻更新事件，按固定间隔结算好感度的每日变化。
 */
@EventBusSubscriber(modid = KaleidoscopeStartale.MODID)
public class FavorabilityEvents {

    /** 检查间隔（刻），每 20 刻即 1 秒检查一次。 */
    private static final int CHECK_INTERVAL = 20;

    /**
     * 处理动物每刻更新事件。
     * <p>
     * 服务端下，每隔固定刻数对动物结算一次好感度的每日变化。
     *
     * @param event 实体每刻更新事件
     */
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