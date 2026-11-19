package com.tequilacat.kaleidoscope_startale.network;

import com.tequilacat.kaleidoscope_startale.KaleidoscopeStartale;
import com.tequilacat.kaleidoscope_startale.entity.RollingKitchenEntity;
import com.tequilacat.kaleidoscope_startale.entity.WiggleCarEntity;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * 载具输入包。
 * <p>
 * 客户端把当前按键状态发给服务端，服务端确认发送者是该载具的驾驶者后写入输入。
 *
 * @param entityId 载具实体 ID
 * @param left     左转
 * @param right    右转
 * @param up       前进
 * @param down     后退
 */
public record VehicleInputPayload(int entityId, boolean left, boolean right, boolean up, boolean down)
        implements CustomPacketPayload {

    /** 包类型标识。 */
    public static final CustomPacketPayload.Type<VehicleInputPayload> TYPE =
            new CustomPacketPayload.Type<>(KaleidoscopeStartale.id("vehicle_input"));

    /** 编解码器，按实体 ID 与四个按键的顺序读写。 */
    public static final StreamCodec<RegistryFriendlyByteBuf, VehicleInputPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, VehicleInputPayload::entityId,
                    ByteBufCodecs.BOOL, VehicleInputPayload::left,
                    ByteBufCodecs.BOOL, VehicleInputPayload::right,
                    ByteBufCodecs.BOOL, VehicleInputPayload::up,
                    ByteBufCodecs.BOOL, VehicleInputPayload::down,
                    VehicleInputPayload::new);

    /**
     * 获取包类型。
     *
     * @return 包类型标识
     */
    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * 服务端处理：只有当前驾驶者可以改输入。
     *
     * @param payload 输入包
     * @param context 上下文
     */
    public static void handle(VehicleInputPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            switch (player.level().getEntity(payload.entityId())) {
                case RollingKitchenEntity kitchen when kitchen.getControllingPassenger() == player ->
                        kitchen.setInput(payload.left(), payload.right(), payload.up(), payload.down());
                case WiggleCarEntity car when car.getControllingPassenger() == player ->
                        car.setInput(payload.left(), payload.right());
                default -> {
                }
            }
        });
    }
}
