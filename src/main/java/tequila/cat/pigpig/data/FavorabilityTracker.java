package tequila.cat.pigpig.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

import net.neoforged.neoforge.attachment.AttachmentType;

import tequila.cat.pigpig.attachment.ModAttachments;

public final class FavorabilityTracker {
    /**
     * 好感度除了存在附件里，也在实体自身的持久化 NBT 里存一份。
     * 附件在个别情况下读档取不到，如果直接按 0 处理，表现就是退出重进后好感度归零；
     * 所以附件为空时从这份备份恢复。
     */
    private static final String NBT_KEY = "StartaleFavorability";

    public static boolean hasData(LivingEntity entity) {
        if (MobTaming.isForeignMaid(entity)) {
            return false;
        }
        return entity.hasData(ModAttachments.FAVORABILITY.get())
                || entity.getPersistentData().contains(NBT_KEY, Tag.TAG_COMPOUND);
    }

    public static Favorability get(LivingEntity entity) {
        AttachmentType<Favorability> type = ModAttachments.FAVORABILITY.get();
        Favorability current = entity.hasData(type) ? entity.getData(type) : null;
        if (current != null && !isUnset(current)) {
            return current;
        }
        // 附件为空（没读出来或被重置）时以 NBT 备份为准，避免用 0 覆盖真实数据
        Favorability restored = readBackup(entity);
        if (restored != null && !isUnset(restored)) {
            entity.setData(type, restored);
            return restored;
        }
        return current != null ? current : entity.getData(type);
    }

    private static boolean isUnset(Favorability data) {
        return data.points() == 0
                && data.feedCount() == 0
                && data.lastDay() == Favorability.UNINITIALIZED_DAY;
    }

    public static void set(LivingEntity entity, Favorability data) {
        entity.setData(ModAttachments.FAVORABILITY.get(), data);
        writeBackup(entity, data);
    }

    private static void initialize(ServerLevel level, LivingEntity entity) {
        set(entity, new Favorability(0, today(level), 0));
    }

    /**
     * 没有可用数据时才初始化。
     * 可能被重复触发的入口（驯养成功、每日结算）都走这里，
     * 否则一次误触发就会把已有好感度清零。
     */
    public static void ensureInitialized(ServerLevel level, LivingEntity entity) {
        if (!hasData(entity) || isUnset(get(entity))) {
            initialize(level, entity);
        }
    }

    public static void onPetted(LivingEntity entity) {
        settleBeforeMutation(entity);
        addPoints(entity, Favorability.GAIN_PER_PET);
    }

    public static void onFed(LivingEntity entity) {
        if (!hasData(entity)) {
            // 别的模组驯服的生物没有数据，先补一次初始化，否则第一次喂食会被吞掉
            if (!(entity.level() instanceof ServerLevel level) || !MobTaming.isTamed(entity)) {
                return;
            }
            initialize(level, entity);
        }
        settleBeforeMutation(entity);
        Favorability data = get(entity);
        if (data.remainingFeedsToday() > 0) {
            set(entity, data.withPoints(data.points() + Favorability.GAIN_PER_FEED)
                    .withFeedCount(data.feedCount() + 1));
        }
    }

    public static void addPoints(LivingEntity entity, int delta) {
        if (hasData(entity)) {
            Favorability data = get(entity);
            set(entity, data.withPoints(data.points() + delta));
        }
    }

    private static void settleBeforeMutation(LivingEntity entity) {
        if (entity.level() instanceof ServerLevel level) {
            settleDaily(level, entity);
        }
    }

    /** 每日结算：当天喂过 +10，没喂不扣好感度，不做衰减。 */
    public static void settleDaily(ServerLevel level, LivingEntity entity) {
        if (!hasData(entity)) {
            if (MobTaming.isTamed(entity)) {
                ensureInitialized(level, entity);
            }
            return;
        }

        Favorability data = get(entity);
        long today = today(level);
        if (data.lastDay() == Favorability.UNINITIALIZED_DAY) {
            set(entity, data.withDay(today, 0));
            return;
        }
        if (today <= data.lastDay()) {
            return;
        }

        int points = data.points();
        // 隔了几天都只结算这一次，没喂就只把喂食次数归零，点数不变
        if (data.fedToday()) {
            points += Favorability.GAIN_PER_DAY;
        }

        set(entity, new Favorability(Mth.clamp(points, 0, Favorability.MAX_POINTS), today, 0));
    }

    private static long today(ServerLevel level) {
        return level.getDayTime() / Favorability.TICKS_PER_DAY;
    }

    private static Favorability readBackup(LivingEntity entity) {
        CompoundTag persistent = entity.getPersistentData();
        if (!persistent.contains(NBT_KEY, Tag.TAG_COMPOUND)) {
            return null;
        }
        return Favorability.CODEC.parse(NbtOps.INSTANCE, persistent.getCompound(NBT_KEY))
                .result()
                .orElse(null);
    }

    private static void writeBackup(LivingEntity entity, Favorability data) {
        Favorability.CODEC.encodeStart(NbtOps.INSTANCE, data)
                .result()
                .ifPresent(tag -> entity.getPersistentData().put(NBT_KEY, tag));
    }

    private FavorabilityTracker() {
    }
}
