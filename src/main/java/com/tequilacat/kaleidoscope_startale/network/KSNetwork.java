package com.tequilacat.kaleidoscope_startale.network;

import com.tequilacat.kaleidoscope_startale.KaleidoscopeStartale;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/**
 * 网络包注册。
 * <p>
 * 在事件总线上集中注册自定义数据包，目前只有载具输入包。
 */
@EventBusSubscriber(modid = KaleidoscopeStartale.MODID)
public final class KSNetwork {

    /**
     * 注册数据包。
     * <p>
     * 协议版本号为 1，收发双方需保持一致；输入包只由客户端发往服务端。
     *
     * @param event 数据包注册事件
     */
    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1")
                .playToServer(VehicleInputPayload.TYPE,
                        VehicleInputPayload.STREAM_CODEC,
                        VehicleInputPayload::handle);
    }
}
