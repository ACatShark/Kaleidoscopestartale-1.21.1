package com.tequilacat.kaleidoscope_startale.client.vehicle;

import com.tequilacat.kaleidoscope_startale.client.KSKeys;
import com.tequilacat.kaleidoscope_startale.entity.RollingKitchenEntity;
import com.tequilacat.kaleidoscope_startale.entity.WiggleCarEntity;
import com.tequilacat.kaleidoscope_startale.network.VehicleInputPayload;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import com.tequilacat.kaleidoscope_startale.KaleidoscopeStartale;

/**
 * 载具驾驶输入。
 * <p>
 * 客户端每刻读取按键状态写入载具，仅在输入发生变化时向服务端发送数据包，
 * 避免每刻重复发包。
 */
@EventBusSubscriber(modid = KaleidoscopeStartale.MODID, value = Dist.CLIENT)
public final class VehicleInputs {

    /** 上次发送的左转按键状态。 */
    private static boolean lastLeft;

    /** 上次发送的右转按键状态。 */
    private static boolean lastRight;

    /** 上次发送的前进按键状态。 */
    private static boolean lastUp;

    /** 上次发送的后退按键状态。 */
    private static boolean lastDown;

    /** 上次发送的载具实体 ID，-1 表示尚未发送。 */
    private static int lastVehicleId = -1;

    /**
     * 客户端刻结束时读取按键并同步给载具。
     * <p>
     * 驾驶流动小厨时读取前后左右四个按键，驾驶扭扭车时只读取左右两个按键；
     * 未驾驶上述载具时清空按键缓存。
     *
     * @param event 客户端刻事件
     */
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        Entity vehicle = player.getVehicle();
        if (vehicle instanceof RollingKitchenEntity kitchen) {
            boolean left = KSKeys.ROLLING_KITCHEN_LEFT.isDown();
            boolean right = KSKeys.ROLLING_KITCHEN_RIGHT.isDown();
            boolean up = KSKeys.ROLLING_KITCHEN_FORWARD.isDown();
            boolean down = KSKeys.ROLLING_KITCHEN_BACKWARD.isDown();
            kitchen.setInput(left, right, up, down);
            sendIfChanged(kitchen, left, right, up, down);
        } else if (vehicle instanceof WiggleCarEntity car) {
            boolean left = KSKeys.WIGGLE_CAR_LEFT.isDown();
            boolean right = KSKeys.WIGGLE_CAR_RIGHT.isDown();
            car.setInput(left, right);
            sendIfChanged(car, left, right, false, false);
        } else {
            resetCache();
        }
    }

    /**
     * 输入变化时记录并发送，未变化时跳过。
     * <p>
     * 扭扭车没有前后按键，调用时固定传 false。
     *
     * @param vehicle 载具实体
     * @param left    左转
     * @param right   右转
     * @param up      前进
     * @param down    后退
     */
    private static void sendIfChanged(Entity vehicle, boolean left, boolean right, boolean up, boolean down) {
        if (sentBefore(vehicle.getId(), left, right, up, down)) {
            return;
        }
        lastVehicleId = vehicle.getId();
        lastLeft = left;
        lastRight = right;
        lastUp = up;
        lastDown = down;
        PacketDistributor.sendToServer(new VehicleInputPayload(vehicle.getId(), left, right, up, down));
    }

    /**
     * 判断输入是否与上次发送的一致。
     * <p>
     * 载具 ID 参与比较，切换载具后即使按键不变也会重新发送。
     *
     * @param vehicleId 载具实体 ID
     * @param left      左转
     * @param right     右转
     * @param up        前进
     * @param down      后退
     * @return 输入是否已发送过
     */
    private static boolean sentBefore(int vehicleId, boolean left, boolean right, boolean up, boolean down) {
        return vehicleId == lastVehicleId
                && left == lastLeft
                && right == lastRight
                && up == lastUp
                && down == lastDown;
    }

    /**
     * 清空按键缓存，回到未发送状态。
     */
    private static void resetCache() {
        lastVehicleId = -1;
        lastLeft = false;
        lastRight = false;
        lastUp = false;
        lastDown = false;
    }
}
