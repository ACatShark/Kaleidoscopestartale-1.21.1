package tequila.cat.pigpig.attachment;

import com.mojang.serialization.Codec;

import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import tequila.cat.pigpig.KaleidoscopeStartale;
import tequila.cat.pigpig.data.Favorability;

public final class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, KaleidoscopeStartale.MODID);

    /** 驯养归属（存档保存 + 同步到客户端，Jade / 渲染器要用）。 */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<TameData>> TAME =
            TYPES.register("livestock", () -> AttachmentType.builder(() -> TameData.EMPTY)
                    .serialize(TameData.CODEC)
                    .sync(TameData.STREAM_CODEC)
                    .copyOnDeath()
                    .build());

    /** 拍拍冷却到期时间（游戏刻）。 */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Long>> PET_COOLDOWN =
            TYPES.register("pet_cooldown", () -> AttachmentType.builder(() -> 0L)
                    .serialize(Codec.LONG)
                    .build());

    /** 被捏的起始时刻（仅客户端渲染需要，同步即可）。 */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Long>> SQUEEZE_START =
            TYPES.register("squeeze_start", () -> AttachmentType.builder(() -> 0L)
                    .sync(ByteBufCodecs.VAR_LONG)
                    .build());

    /** 本次挤压的力度（锋利等级），客户端据此加深压扁程度。 */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Integer>> SQUEEZE_POWER =
            TYPES.register("squeeze_power", () -> AttachmentType.builder(() -> 0)
                    .sync(ByteBufCodecs.VAR_INT)
                    .build());

    /** 上一 tick 是否拥有「灵感充沛」，用于判定 buff 结束并施加「灵感枯」。 */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Boolean>> HAD_INSPIRATION =
            TYPES.register("had_inspiration", () -> AttachmentType.builder(() -> Boolean.FALSE).build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Favorability>> FAVORABILITY =
            TYPES.register("favorability", () -> AttachmentType.builder(() -> Favorability.EMPTY)
                    .serialize(Favorability.CODEC)
                    .sync(Favorability.STREAM_CODEC)
                    .copyOnDeath()
                    .build());

    private ModAttachments() {
    }
}
