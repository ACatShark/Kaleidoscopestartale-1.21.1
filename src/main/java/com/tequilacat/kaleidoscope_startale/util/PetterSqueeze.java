package com.tequilacat.kaleidoscope_startale.util;

import com.tequilacat.kaleidoscope_startale.init.KSAttachments;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

/**
 * 拍击挤压形变工具类。
 * <p>
 * 记录拍击开始时间与力度，提供随时间变化的纵向缩放比例，
 * 用于渲染时表现「被拍扁后回弹」的弹性形变效果。
 */
public class PetterSqueeze {

    /** 形变总时长（刻）。 */
    public static final int DURATION = 15;

    /** 下压阶段时长（刻）。 */
    private static final float DOWN_TICKS = 5.0F;

    /** 基础压扁比例。 */
    private static final float PRESSED_SCALE = 0.6F;

    /** 最小压扁比例。 */
    private static final float MIN_PRESSED_SCALE = 0.25F;

    /** 每级力度增加的压扁深度。 */
    private static final float POWER_DEPTH_STEP = 0.07F;

    /** outBack 缓动的回弹超调量。 */
    private static final float BACK_OVERSHOOT = 2.6F;

    /** outElastic 缓动的周期系数。 */
    private static final float ELASTIC_PERIOD = 2.0943952F;

    /**
     * 开始一次挤压形变。
     * <p>
     * 记录当前游戏时间作为起始时刻，并保存拍击力度（不小于 0）。
     *
     * @param entity 目标实体
     * @param power  拍击力度
     */
    public static void start(LivingEntity entity, int power) {
        entity.setData(KSAttachments.SQUEEZE_START.get(), entity.level().getGameTime());
        entity.setData(KSAttachments.SQUEEZE_POWER.get(), Math.max(0, power));
    }

    /**
     * 获取当前纵向缩放比例。
     * <p>
     * 未开始、时间无效或已超出时长时返回 1.0；
     * 下压阶段使用 outBack 缓动压扁，回弹阶段使用 outElastic 缓动恢复。
     *
     * @param entity      目标实体
     * @param partialTick 插值刻
     * @return 纵向缩放比例
     */
    public static float yScale(Entity entity, float partialTick) {
        long start = entity.getData(KSAttachments.SQUEEZE_START.get());
        if (start <= 0L) {
            return 1.0F;
        }
        float elapsed = (float) (entity.level().getGameTime() - start) + partialTick;
        if (elapsed < 0.0F || elapsed >= DURATION) {
            return 1.0F;
        }
        float pressed = pressedScale(entity);
        if (elapsed < DOWN_TICKS) {
            return outBack(elapsed / DOWN_TICKS, 1.0F, pressed);
        }
        float bounce = inQuart((elapsed / DOWN_TICKS - 1.0F) * 0.5F);
        return outElastic(bounce, pressed, 1.0F);
    }

    /** 锋利等级越高压得越扁。 */
    private static float pressedScale(Entity entity) {
        int power = entity.getData(KSAttachments.SQUEEZE_POWER.get());
        return Mth.clamp(PRESSED_SCALE - POWER_DEPTH_STEP * power, MIN_PRESSED_SCALE, PRESSED_SCALE);
    }

    /**
     * 由纵向缩放计算横向缩放，保持体积近似不变。
     *
     * @param yScale 纵向缩放比例
     * @return 横向缩放比例
     */
    public static float xzScale(float yScale) {
        return Mth.sqrt(1.0F / yScale);
    }

    /**
     * outBack 缓动函数。
     * <p>
     * 在 from 与 to 之间插值，并在结束前产生超调回弹。
     *
     * @param x    归一化进度
     * @param from 起始值
     * @param to   目标值
     * @return 插值结果
     */
    private static float outBack(float x, float from, float to) {
        float c1 = BACK_OVERSHOOT;
        float c3 = c1 + 1.0F;
        float v = (float) (1.0D + (double) c3 * Math.pow(x - 1.0F, 3.0D) + (double) c1 * Math.pow(x - 1.0F, 2.0D));
        return from + v * (to - from);
    }

    /**
     * inQuart 缓动函数。
     *
     * @param x 归一化进度
     * @return 四次方结果
     */
    private static float inQuart(float x) {
        return x * x * x * x;
    }

    /**
     * outElastic 缓动函数。
     * <p>
     * 在 from 与 to 之间插值，并产生衰减振荡的弹性回弹。
     *
     * @param x    归一化进度
     * @param from 起始值
     * @param to   目标值
     * @return 插值结果
     */
    private static float outElastic(float x, float from, float to) {
        float v;
        if (x <= 0.0F) {
            v = 0.0F;
        } else if (x >= 1.0F) {
            v = 1.0F;
        } else {
            v = (float) (Math.pow(2.0D, -10.0D * x) * Math.sin((x * 10.0D - 0.75D) * (double) ELASTIC_PERIOD) + 1.0D);
        }
        return from + v * (to - from);
    }
}