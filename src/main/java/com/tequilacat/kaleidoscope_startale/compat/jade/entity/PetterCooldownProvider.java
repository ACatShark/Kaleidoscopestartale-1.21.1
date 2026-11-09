package com.tequilacat.kaleidoscope_startale.compat.jade.entity;

import com.tequilacat.kaleidoscope_startale.KaleidoscopeStartale;
import com.tequilacat.kaleidoscope_startale.util.PetterCooldown;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.StreamServerDataProvider;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.theme.IThemeHelper;

/**
 * 拍击冷却提示提供器。
 * <p>
 * 服务端读取动物的剩余拍击冷却刻数并通过流数据下发，
 * 客户端在冷却大于 0 时以秒为单位显示剩余冷却时间。
 */
public enum PetterCooldownProvider implements IEntityComponentProvider, StreamServerDataProvider<EntityAccessor, Integer> {

    /** 单例实例。 */
    INSTANCE;

    /** 提供器唯一标识。 */
    private static final ResourceLocation UID = KaleidoscopeStartale.id("petter_cooldown");

    /** 未处于冷却时的默认值。 */
    private static final int NOT_COOLING = 0;

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
     * 读取实体的剩余拍击冷却刻数。
     *
     * @param accessor 实体访问器
     * @return 剩余冷却刻数
     */
    @Override
    public Integer streamData(EntityAccessor accessor) {
        return PetterCooldown.remaining(accessor.getEntity());
    }

    /**
     * 剩余冷却刻数的流编解码器。
     */
    private static final StreamCodec<RegistryFriendlyByteBuf, Integer> COOLDOWN_CODEC = new StreamCodec<>() {
        @Override
        public Integer decode(RegistryFriendlyByteBuf buf) {
            return ByteBufCodecs.VAR_INT.decode(buf);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, Integer value) {
            ByteBufCodecs.VAR_INT.encode(buf, value);
        }
    };

    /**
     * 获取流编解码器。
     *
     * @return 流编解码器
     */
    @Override
    public StreamCodec<RegistryFriendlyByteBuf, Integer> streamCodec() {
        return COOLDOWN_CODEC;
    }

    /**
     * 追加提示文本。
     * <p>
     * 剩余冷却大于 0 时，以秒为单位显示剩余拍击冷却时间。
     *
     * @param tooltip  提示对象
     * @param accessor 实体访问器
     * @param config   插件配置
     */
    @Override
    public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
        int remaining = this.decodeFromData(accessor).orElse(NOT_COOLING);
        if (remaining > 0) {
            tooltip.add(Component.translatable("jade.kaleidoscope_startale.petter_cooldown",
                    IThemeHelper.get().seconds(remaining, accessor.tickRate())));
        }
    }
}