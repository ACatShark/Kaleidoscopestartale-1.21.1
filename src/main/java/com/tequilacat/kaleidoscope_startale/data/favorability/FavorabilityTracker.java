package com.tequilacat.kaleidoscope_startale.data.favorability;

import com.tequilacat.kaleidoscope_startale.config.StartaleConfig;
import com.tequilacat.kaleidoscope_startale.data.tame.MobTaming;
import com.tequilacat.kaleidoscope_startale.init.KSAttachments;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.attachment.AttachmentType;

/**
 * 好感度追踪工具类。
 * <p>
 * 基于附件存储动物的好感度数据，同时以持久化 NBT 作为备份，
 * 防止附件丢失导致数据被重置。提供初始化、拍击与喂食加点、
 * 按天结算等操作，并排除女仆模组实体。
 */
public class FavorabilityTracker {

    /**
     * 持久化 NBT 中备份数据的键。
     */
    private static final String NBT_KEY = "StartaleFavorability";

    /**
     * 判断实体是否具有好感度数据。
     * <p>
     * 女仆模组实体一律视为无数据；否则检查附件或 NBT 备份是否存在。
     *
     * @param entity 目标实体
     * @return 是否具有好感度数据
     */
    public static boolean hasData(LivingEntity entity) {
        if (MobTaming.isForeignMaid(entity)) {
            return false;
        }
        return entity.hasData(KSAttachments.FAVORABILITY.get())
                || entity.getPersistentData().contains(NBT_KEY, Tag.TAG_COMPOUND);
    }

    /**
     * 获取好感度数据。
     * <p>
     * 优先取附件数据；附件为空或未初始化时以 NBT 备份为准并回写到附件，
     * 避免用 0 覆盖真实数据。
     *
     * @param entity 目标实体
     * @return 好感度数据
     */
    public static Favorability get(LivingEntity entity) {
        AttachmentType<Favorability> type = KSAttachments.FAVORABILITY.get();
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

    /**
     * 判断好感度数据是否为未初始化状态。
     *
     * @param data 好感度数据
     * @return 是否为未初始化状态
     */
    private static boolean isUnset(Favorability data) {
        return data.points() == 0
                && data.feedCount() == 0
                && data.lastDay() == Favorability.UNINITIALIZED_DAY;
    }

    /**
     * 设置好感度数据。
     * <p>
     * 同时写入附件与 NBT 备份。
     *
     * @param entity 目标实体
     * @param data   好感度数据
     */
    public static void set(LivingEntity entity, Favorability data) {
        entity.setData(KSAttachments.FAVORABILITY.get(), data);
        writeBackup(entity, data);
    }

    /**
     * 初始化好感度数据。
     * <p>
     * 以 0 点数、当天日期、0 次喂食创建初始数据。
     *
     * @param level  服务端世界
     * @param entity 目标实体
     */
    private static void initialize(ServerLevel level, LivingEntity entity) {
        set(entity, new Favorability(0, today(level), 0));
    }

    /**
     * 确保实体已初始化好感度数据。
     * <p>
     * 无数据或数据未初始化时执行初始化。
     *
     * @param level  服务端世界
     * @param entity 目标实体
     */
    public static void ensureInitialized(ServerLevel level, LivingEntity entity) {
        if (!hasData(entity) || isUnset(get(entity))) {
            initialize(level, entity);
        }
    }

    /**
     * 处理被拍击时增加好感度。
     * <p>
     * 先结算每日变化，再增加拍击对应的点数。
     *
     * @param entity 目标实体
     */
    public static void onPetted(LivingEntity entity) {
        settleBeforeMutation(entity);
        addPoints(entity, StartaleConfig.FAVOR_POINTS_PER_PET.get());
    }

    /**
     * 处理被喂食时增加好感度。
     * <p>
     * 第三方模组驯服的生物若无数据则先初始化，避免首次喂食被吞掉；
     * 结算每日变化后，在当日剩余喂食次数内增加点数与喂食计数。
     *
     * @param entity 目标实体
     */
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
            set(entity, data.withPoints(data.points() + StartaleConfig.FAVOR_POINTS_PER_FEED.get())
                    .withFeedCount(data.feedCount() + 1));
        }
    }

    /**
     * 增加好感度点数。
     * <p>
     * 仅在有数据时生效。
     *
     * @param entity 目标实体
     * @param delta  点数增量
     */
    public static void addPoints(LivingEntity entity, int delta) {
        if (hasData(entity)) {
            Favorability data = get(entity);
            set(entity, data.withPoints(data.points() + delta));
        }
    }

    /**
     * 在修改数据前先结算每日变化。
     *
     * @param entity 目标实体
     */
    private static void settleBeforeMutation(LivingEntity entity) {
        if (entity.level() instanceof ServerLevel level) {
            settleDaily(level, entity);
        }
    }

    /**
     * 按天结算好感度变化。
     * <p>
     * 已驯养但无数据的生物先初始化；未初始化的日期直接补正为当天；
     * 日期未推进则不处理；跨天时若前一日喂过食则增加每日点数，
     * 最后将点数限制在有效范围内并清零喂食计数。
     *
     * @param level  服务端世界
     * @param entity 目标实体
     */
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
            points += StartaleConfig.FAVOR_POINTS_PER_DAY.get();
        }

        set(entity, new Favorability(Mth.clamp(points, 0, Favorability.maxPoints()), today, 0));
    }

    /**
     * 获取世界当前的天数。
     *
     * @param level 服务端世界
     * @return 当前天数
     */
    private static long today(ServerLevel level) {
        return level.getDayTime() / Favorability.TICKS_PER_DAY;
    }

    /**
     * 从持久化 NBT 读取备份的好感度数据。
     *
     * @param entity 目标实体
     * @return 备份数据，读取失败时返回 null
     */
    private static Favorability readBackup(LivingEntity entity) {
        CompoundTag persistent = entity.getPersistentData();
        if (!persistent.contains(NBT_KEY, Tag.TAG_COMPOUND)) {
            return null;
        }
        return Favorability.CODEC.parse(NbtOps.INSTANCE, persistent.getCompound(NBT_KEY))
                .result()
                .orElse(null);
    }

    /**
     * 将好感度数据写入持久化 NBT 备份。
     *
     * @param entity 目标实体
     * @param data   好感度数据
     */
    private static void writeBackup(LivingEntity entity, Favorability data) {
        Favorability.CODEC.encodeStart(NbtOps.INSTANCE, data)
                .result()
                .ifPresent(tag -> entity.getPersistentData().put(NBT_KEY, tag));
    }
}