package com.tequilacat.kaleidoscope_startale.init;

import com.mojang.serialization.Codec;
import com.tequilacat.kaleidoscope_startale.KaleidoscopeStartale;
import com.tequilacat.kaleidoscope_startale.data.tame.TameData;
import com.tequilacat.kaleidoscope_startale.data.favorability.Favorability;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * 模组数据附件注册类。
 * <p>
 * 注册实体附件类型：驯养归属、拍拍冷却、挤压起始时刻与力度、
 * 灵感标记以及好感度数据，并按需配置序列化、同步与死亡保留。
 */
public interface KSAttachments {
    DeferredRegister<AttachmentType<?>> TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, KaleidoscopeStartale.MODID);

    /**
     * 驯养归属。
     * <p>
     * 存档保存并同步到客户端，供 Jade 与渲染器使用；死亡时保留。
     */
    DeferredHolder<AttachmentType<?>, AttachmentType<TameData>> TAME =
            TYPES.register("livestock", () -> AttachmentType.builder(() -> TameData.EMPTY)
                    .serialize(TameData.CODEC)
                    .sync(TameData.STREAM_CODEC)
                    .copyOnDeath()
                    .build());

    /**
     * 拍拍冷却到期时间（游戏刻）。
     * <p>
     * 存档保存并同步到客户端，供驯养信息面板显示剩余冷却。
     */
    DeferredHolder<AttachmentType<?>, AttachmentType<Long>> PET_COOLDOWN =
            TYPES.register("pet_cooldown", () -> AttachmentType.builder(() -> 0L)
                    .serialize(Codec.LONG)
                    .sync(ByteBufCodecs.VAR_LONG)
                    .build());

    /**
     * 被捏的起始时刻。
     * <p>
     * 仅客户端渲染需要，同步即可。
     */
    DeferredHolder<AttachmentType<?>, AttachmentType<Long>> SQUEEZE_START =
            TYPES.register("squeeze_start", () -> AttachmentType.builder(() -> 0L)
                    .sync(ByteBufCodecs.VAR_LONG)
                    .build());

    /**
     * 本次挤压的力度（锋利等级）。
     * <p>
     * 客户端据此加深压扁程度。
     */
    DeferredHolder<AttachmentType<?>, AttachmentType<Integer>> SQUEEZE_POWER =
            TYPES.register("squeeze_power", () -> AttachmentType.builder(() -> 0)
                    .sync(ByteBufCodecs.VAR_INT)
                    .build());

    /**
     * 上一刻是否拥有「灵感充沛」。
     * <p>
     * 用于判定效果结束并施加「灵感枯竭」，无需保存与同步。
     */
    DeferredHolder<AttachmentType<?>, AttachmentType<Boolean>> HAD_INSPIRATION =
            TYPES.register("had_inspiration", () -> AttachmentType.builder(() -> Boolean.FALSE).build());

    /**
     * 好感度数据。
     * <p>
     * 存档保存并同步到客户端，死亡时保留。
     */
    DeferredHolder<AttachmentType<?>, AttachmentType<Favorability>> FAVORABILITY =
            TYPES.register("favorability", () -> AttachmentType.builder(() -> Favorability.EMPTY)
                    .serialize(Favorability.CODEC)
                    .sync(Favorability.STREAM_CODEC)
                    .copyOnDeath()
                    .build());
}