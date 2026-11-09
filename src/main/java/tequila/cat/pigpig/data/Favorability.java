package tequila.cat.pigpig.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;

/**
 * 好感度：0~900 点，每 100 点升一级，最高 10 级。
 * 每天结算一次：当天喂过 +10，没喂不扣（不做衰减）。
 */
public record Favorability(int points, long lastDay, int feedCount) {
    public static final int POINTS_PER_LEVEL = 100;
    public static final int MAX_LEVEL = 10;
    public static final int MAX_POINTS = 900;
    public static final int MAX_FEEDS_PER_DAY = 3;

    public static final int GAIN_PER_PET = 10;
    public static final int GAIN_PER_FEED = 5;
    public static final int GAIN_PER_DAY = 10;

    public static final long TICKS_PER_DAY = 24000L;
    public static final long UNINITIALIZED_DAY = -1L;

    public static final Favorability EMPTY = new Favorability(0, UNINITIALIZED_DAY, 0);

    public static final Codec<Favorability> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("points", 0).forGetter(Favorability::points),
            Codec.LONG.optionalFieldOf("last_day", UNINITIALIZED_DAY).forGetter(Favorability::lastDay),
            Codec.INT.optionalFieldOf("feed_count", 0).forGetter(Favorability::feedCount)
    ).apply(instance, Favorability::new));

    public static final StreamCodec<ByteBuf, Favorability> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, Favorability::points,
            ByteBufCodecs.VAR_LONG, Favorability::lastDay,
            ByteBufCodecs.VAR_INT, Favorability::feedCount,
            Favorability::new);

    public static int levelOf(int points) {
        return Mth.clamp(1 + points / POINTS_PER_LEVEL, 1, MAX_LEVEL);
    }

    public static int pointsToNextLevelOf(int points) {
        int level = levelOf(points);
        return level >= MAX_LEVEL ? 0 : level * POINTS_PER_LEVEL - points;
    }

    public int level() {
        return levelOf(this.points);
    }

    public boolean isMaxLevel() {
        return this.level() >= MAX_LEVEL;
    }

    public int pointsToNextLevel() {
        return pointsToNextLevelOf(this.points);
    }

    public boolean fedToday() {
        return this.feedCount > 0;
    }

    public int remainingFeedsToday() {
        return Math.max(0, MAX_FEEDS_PER_DAY - this.feedCount);
    }

    public Favorability withPoints(int newPoints) {
        return new Favorability(Mth.clamp(newPoints, 0, MAX_POINTS), this.lastDay, this.feedCount);
    }

    public Favorability withFeedCount(int newFeedCount) {
        return new Favorability(this.points, this.lastDay, newFeedCount);
    }

    public Favorability withDay(long newDay, int newFeedCount) {
        return new Favorability(this.points, newDay, newFeedCount);
    }
}
