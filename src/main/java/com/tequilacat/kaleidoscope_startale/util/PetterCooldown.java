package com.tequilacat.kaleidoscope_startale.util;

import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;

import com.tequilacat.kaleidoscope_startale.config.StartaleConfig;
import com.tequilacat.kaleidoscope_startale.init.KSAttachments;

/**
 * 拍击冷却工具类。
 * <p>
 * 通过附件记录拍击冷却的到期时刻，提供剩余时间查询、
 * 冷却判定、冷却重置以及喂食缩短冷却等操作。
 */
public class PetterCooldown {

    /**
     * 获取剩余冷却时间。
     * <p>
     * 无冷却或已到期时返回 0，否则返回剩余刻数。
     *
     * @param entity 目标实体
     * @return 剩余冷却刻数
     */
    public static int remaining(Entity entity) {
        long expiry = entity.getData(KSAttachments.PET_COOLDOWN.get());
        if (expiry <= 0L) {
            return 0;
        }
        long left = expiry - entity.level().getGameTime();
        return left > 0L ? (int) Math.min(left, Integer.MAX_VALUE) : 0;
    }

    /**
     * 判断实体是否处于冷却中。
     *
     * @param entity 目标实体
     * @return 是否处于冷却中
     */
    public static boolean isCooling(Entity entity) {
        return remaining(entity) > 0;
    }

    /**
     * 重置冷却。
     * <p>
     * 将冷却到期时刻设为当前游戏时间加上冷却总时长。
     *
     * @param entity 目标实体
     */
    public static void reset(Entity entity) {
        entity.setData(KSAttachments.PET_COOLDOWN.get(),
                entity.level().getGameTime() + StartaleConfig.PETTER_COOLDOWN_TICKS.get());
    }

    /**
     * 计算喂食可缩短的冷却刻数。
     * <p>
     * 按剩余冷却换算喂食加速秒数，再转为刻数。
     *
     * @param entity 目标实体
     * @return 可缩短的刻数
     */
    public static int feedingReduction(Entity entity) {
        return AgeableMob.getSpeedUpSecondsWhenFeeding(remaining(entity)) * 20;
    }

    /**
     * 通过喂食缩短冷却。
     * <p>
     * 若可缩短刻数大于 0，则将到期时刻提前相应刻数，且不早于 0。
     *
     * @param entity 目标实体
     * @return 是否成功缩短
     */
    public static boolean shortenByFeeding(Entity entity) {
        int reduction = feedingReduction(entity);
        if (reduction <= 0) {
            return false;
        }
        long expiry = entity.getData(KSAttachments.PET_COOLDOWN.get());
        entity.setData(KSAttachments.PET_COOLDOWN.get(), Math.max(0L, expiry - (long) reduction));
        return true;
    }
}