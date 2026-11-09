package com.tequilacat.kaleidoscope_startale.compat.jade.entity;

import com.tequilacat.kaleidoscope_startale.KaleidoscopeStartale;
import com.tequilacat.kaleidoscope_startale.data.favorability.Favorability;
import com.tequilacat.kaleidoscope_startale.data.favorability.FavorabilityTracker;
import com.tequilacat.kaleidoscope_startale.data.tame.MobTaming;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

/**
 * 好感度提示提供器。
 * <p>
 * 服务端读取活体实体的好感度点数并通过服务端数据下发，
 * 客户端显示当前等级以及升级所需点数（满级时显示已满级）。
 * 女仆模组实体不参与。
 */
public enum FavorabilityProvider implements IEntityComponentProvider, IServerDataProvider<EntityAccessor> {

    /** 单例实例。 */
    INSTANCE;

    /**
     * 服务端数据中好感度点数的键。
     */
    private static final String KEY_POINTS = "KaleidoscopeStartaleFavorability";

    /** 提供器唯一标识。 */
    private static final ResourceLocation UID = KaleidoscopeStartale.id("favorability");

    /**
     * 获取提供器唯一标识。
     *
     * @return 提供器唯一标识
     */
    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    /**
     * 判断是否需要请求服务端数据。
     * <p>
     * 仅对非女仆模组的活体实体请求数据。
     *
     * @param accessor 实体访问器
     * @return 是否需要请求数据
     */
    @Override
    public boolean shouldRequestData(EntityAccessor accessor) {
        return accessor.getEntity() instanceof LivingEntity living && !MobTaming.isForeignMaid(living);
    }

    /**
     * 追加服务端数据。
     * <p>
     * 若实体为活体、非女仆模组且具有好感度数据，则写入点数。
     *
     * @param data     服务端数据标签
     * @param accessor 实体访问器
     */
    @Override
    public void appendServerData(CompoundTag data, EntityAccessor accessor) {
        if (accessor.getEntity() instanceof LivingEntity living
                && !MobTaming.isForeignMaid(living)
                && FavorabilityTracker.hasData(living)) {
            data.putInt(KEY_POINTS, FavorabilityTracker.get(living).points());
        }
    }

    /**
     * 追加提示文本。
     * <p>
     * 显示当前好感度等级；未满级时显示升级所需点数，满级时显示已满级。
     *
     * @param tooltip  提示对象
     * @param accessor 实体访问器
     * @param config   插件配置
     */
    @Override
    public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
        CompoundTag data = accessor.getServerData();
        if (!data.contains(KEY_POINTS)) {
            return;
        }

        int points = data.getInt(KEY_POINTS);
        tooltip.add(Component.translatable("jade.kaleidoscope_startale.favorability", Favorability.levelOf(points)));

        int toNext = Favorability.pointsToNextLevelOf(points);
        if (toNext > 0) {
            tooltip.add(Component.translatable("jade.kaleidoscope_startale.favorability_next", toNext));
        } else {
            tooltip.add(Component.translatable("jade.kaleidoscope_startale.favorability_max"));
        }
    }
}