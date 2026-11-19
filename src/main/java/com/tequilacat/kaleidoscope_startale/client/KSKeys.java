package com.tequilacat.kaleidoscope_startale.client;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

import org.lwjgl.glfw.GLFW;

import com.tequilacat.kaleidoscope_startale.KaleidoscopeStartale;

/**
 * 模组键位定义。
 * <p>
 * 载具的驾驶键位单独注册到「按键设置」里，玩家可以自由改键。
 */
@EventBusSubscriber(modid = KaleidoscopeStartale.MODID, value = Dist.CLIENT)
public final class KSKeys {

    /** 键位分类的翻译键。 */
    public static final String CATEGORY = "key.categories.kaleidoscope_startale.vehicle";

    /** 流动小厨：前进。 */
    public static final KeyMapping ROLLING_KITCHEN_FORWARD = new KeyMapping(
            "key.kaleidoscope_startale.rolling_kitchen.forward",
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_W, CATEGORY);

    /** 流动小厨：后退。 */
    public static final KeyMapping ROLLING_KITCHEN_BACKWARD = new KeyMapping(
            "key.kaleidoscope_startale.rolling_kitchen.backward",
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_S, CATEGORY);

    /** 流动小厨：左转。 */
    public static final KeyMapping ROLLING_KITCHEN_LEFT = new KeyMapping(
            "key.kaleidoscope_startale.rolling_kitchen.left",
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_A, CATEGORY);

    /** 流动小厨：右转。 */
    public static final KeyMapping ROLLING_KITCHEN_RIGHT = new KeyMapping(
            "key.kaleidoscope_startale.rolling_kitchen.right",
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_D, CATEGORY);

    /** 扭扭车：向左前扭。 */
    public static final KeyMapping WIGGLE_CAR_LEFT = new KeyMapping(
            "key.kaleidoscope_startale.wiggle_car.left",
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_A, CATEGORY);

    /** 扭扭车：向右前扭。 */
    public static final KeyMapping WIGGLE_CAR_RIGHT = new KeyMapping(
            "key.kaleidoscope_startale.wiggle_car.right",
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_D, CATEGORY);

    /**
     * 把键位注册进原版按键设置。
     *
     * @param event 键位注册事件
     */
    @SubscribeEvent
    public static void register(RegisterKeyMappingsEvent event) {
        event.register(ROLLING_KITCHEN_FORWARD);
        event.register(ROLLING_KITCHEN_BACKWARD);
        event.register(ROLLING_KITCHEN_LEFT);
        event.register(ROLLING_KITCHEN_RIGHT);
        event.register(WIGGLE_CAR_LEFT);
        event.register(WIGGLE_CAR_RIGHT);
    }
}
