package com.tequilacat.kaleidoscope_startale.data.favorability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;

/**
 * 好感度数据记录。
 * <p>
 * 保存点数、上次结算日期与当日喂食次数，并提供等级换算、
 * 升级所需点数、当日剩余喂食次数等派生计算，
 * 以及若干返回修改副本的不可变更新方法。
 *
 * @param points    好感度点数
 * @param lastDay   上次结算的日期（天）
 * @param feedCount 当日喂食次数
 */
public record Favorability(int points, long lastDay, int feedCount) {

    /** 每级所需的点数。 */
    public static final int POINTS_PER_LEVEL = 100;

    /** 最大等级。 */
    public static final int MAX_LEVEL = 10;

    /** 最大点数。 */
    public static final int MAX_POINTS = 900;

    /** 每日最大喂食次数。 */
    public static final int MAX_FEEDS_PER_DAY = 3;

    /** 每次拍击获得的点数。 */
    public static final int GAIN_PER_PET = 10;

    /** 每次喂食获得的点数。 */
    public static final int GAIN_PER_FEED = 5;

    /** 每日结算获得的点数。 */
    public static final int GAIN_PER_DAY = 10;

    /** 每天的刻数。 */
    public static final long TICKS_PER_DAY = 24000L;

    /** 未初始化日期的标记值。 */
    public static final long UNINITIALIZED_DAY = -1L;

    /** 空的好感度数据。 */
    public static final Favorability EMPTY = new Favorability(0, UNINITIALIZED_DAY, 0);

    /**
     * 好感度数据的编解码器。
     * <p>
     * 各字段均为可选，缺省时使用默认值。
     */
    public static final Codec<Favorability> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("points", 0).forGetter(Favorability::points),
            Codec.LONG.optionalFieldOf("last_day", UNINITIALIZED_DAY).forGetter(Favorability::lastDay),
            Codec.INT.optionalFieldOf("feed_count", 0).forGetter(Favorability::feedCount)
    ).apply(instance, Favorability::new));

    /**
     * 好感度数据的流编解码器，用于网络传输。
     */
    public static final StreamCodec<ByteBuf, Favorability> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, Favorability::points,
            ByteBufCodecs.VAR_LONG, Favorability::lastDay,
            ByteBufCodecs.VAR_INT, Favorability::feedCount,
            Favorability::new);

    /**
     * 根据点数计算等级。
     *
     * @param points 好感度点数
     * @return 等级（限制在 1 到最大等级之间）
     */
    public static int levelOf(int points) {
        return Mth.clamp(1 + points / POINTS_PER_LEVEL, 1, MAX_LEVEL);
    }

    /**
     * 根据点数计算升到下一级还需要的点数。
     *
     * @param points 好感度点数
     * @return 升级所需点数，已满级时返回 0
     */
    public static int pointsToNextLevelOf(int points) {
        int level = levelOf(points);
        return level >= MAX_LEVEL ? 0 : level * POINTS_PER_LEVEL - points;
    }

    /**
     * 获取当前等级。
     *
     * @return 当前等级
     */
    public int level() {
        return levelOf(this.points);
    }

    /**
     * 判断是否已达最大等级。
     *
     * @return 是否已满级
     */
    public boolean isMaxLevel() {
        return this.level() >= MAX_LEVEL;
    }

    /**
     * 获取升到下一级还需要的点数。
     *
     * @return 升级所需点数，已满级时返回 0
     */
    public int pointsToNextLevel() {
        return pointsToNextLevelOf(this.points);
    }

    /**
     * 判断今日是否已喂食过。
     *
     * @return 今日是否已喂食
     */
    public boolean fedToday() {
        return this.feedCount > 0;
    }

    /**
     * 获取今日剩余可喂食次数。
     *
     * @return 剩余喂食次数
     */
    public int remainingFeedsToday() {
        return Math.max(0, MAX_FEEDS_PER_DAY - this.feedCount);
    }

    /**
     * 返回修改点数后的副本。
     * <p>
     * 点数会被限制在 0 到最大点数之间。
     *
     * @param newPoints 新点数
     * @return 修改后的副本
     */
    public Favorability withPoints(int newPoints) {
        return new Favorability(Mth.clamp(newPoints, 0, MAX_POINTS), this.lastDay, this.feedCount);
    }

    /**
     * 返回修改当日喂食次数后的副本。
     *
     * @param newFeedCount 新喂食次数
     * @return 修改后的副本
     */
    public Favorability withFeedCount(int newFeedCount) {
        return new Favorability(this.points, this.lastDay, newFeedCount);
    }

    /**
     * 返回修改日期与喂食次数后的副本。
     *
     * @param newDay       新日期
     * @param newFeedCount 新喂食次数
     * @return 修改后的副本
     */
    public Favorability withDay(long newDay, int newFeedCount) {
        return new Favorability(this.points, newDay, newFeedCount);
    }
}