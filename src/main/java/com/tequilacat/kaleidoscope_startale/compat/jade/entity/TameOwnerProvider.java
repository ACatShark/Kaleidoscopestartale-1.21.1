package com.tequilacat.kaleidoscope_startale.compat.jade.entity;

import com.tequilacat.kaleidoscope_startale.KaleidoscopeStartale;
import com.tequilacat.kaleidoscope_startale.init.KSAttachments;
import com.tequilacat.kaleidoscope_startale.data.tame.TameData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.animal.Animal;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import org.jetbrains.annotations.Nullable;

/**
 * 驯养主人提示提供器。
 * <p>
 * 为 Jade 提供动物的驯养主人信息：服务端读取本模组驯养数据中的主人名称，
 * 通过服务端数据下发到客户端，并在实体提示中显示主人。
 * 原版 OwnableEntity 已有主人时不重复显示。
 */
public enum TameOwnerProvider implements IEntityComponentProvider, IServerDataProvider<EntityAccessor> {

    /** 单例实例。 */
    INSTANCE;

    /**
     * 服务端数据中主人名称的键。
     */
    private static final String KEY = "KaleidoscopeStartaleOwner";

    /** 提供器唯一标识。 */
    private static final ResourceLocation UID = KaleidoscopeStartale.id("animal_owner");

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
     * 仅当实体能读出主人名称时才请求。
     *
     * @param accessor 实体访问器
     * @return 是否需要请求数据
     */
    @Override
    public boolean shouldRequestData(EntityAccessor accessor) {
        return ownerName(accessor.getEntity()) != null;
    }

    /**
     * 追加服务端数据。
     * <p>
     * 将主人名称写入服务端数据，供客户端读取。
     *
     * @param data     服务端数据标签
     * @param accessor 实体访问器
     */
    @Override
    public void appendServerData(CompoundTag data, EntityAccessor accessor) {
        String owner = ownerName(accessor.getEntity());
        if (owner != null) {
            data.putString(KEY, owner);
        }
    }

    /**
     * 追加提示文本。
     * <p>
     * 若服务端数据中存在主人名称，则添加显示主人的提示。
     *
     * @param tooltip  提示对象
     * @param accessor 实体访问器
     * @param config   插件配置
     */
    @Override
    public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
        String owner = accessor.getServerData().getString(KEY);
        if (!owner.isEmpty()) {
            tooltip.add(Component.translatable("jade.owner", owner));
        }
    }

    /**
     * 读取实体驯养数据中的主人名称。
     * <p>
     * 仅对动物有效；原版 OwnableEntity 已有主人时返回 null，
     * 由本模组驯养数据提供主人名称，未驯养或无名称时返回 null。
     *
     * @param entity 目标实体
     * @return 主人名称，无可用名称时返回 null
     */
    @Nullable
    private static String ownerName(Entity entity) {
        if (!(entity instanceof Animal)) {
            return null;
        }
        if (entity instanceof OwnableEntity ownable && ownable.getOwnerUUID() != null) {
            return null;
        }

        TameData data = entity.getData(KSAttachments.TAME.get());
        return data.isTamed() && !data.ownerName().isEmpty() ? data.ownerName() : null;
    }
}