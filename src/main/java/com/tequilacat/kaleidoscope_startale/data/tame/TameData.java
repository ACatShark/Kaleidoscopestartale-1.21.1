package com.tequilacat.kaleidoscope_startale.data.tame;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

/**
 * 驯养数据记录。
 * <p>
 * 保存主人 UUID 与主人名称，提供数据编解码与网络流编解码，
 * 并支持驯养状态与归属判定。
 *
 * @param owner     主人 UUID，未驯养时为 null
 * @param ownerName 主人名称
 */
public record TameData(@Nullable UUID owner, String ownerName) {

    /** 空的驯养数据（未驯养）。 */
    public static final TameData EMPTY = new TameData(null, "");

    /**
     * 驯养数据编解码器。
     * <p>
     * 主人 UUID 为可选字段，主人名称缺省为空字符串。
     */
    public static final Codec<TameData> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            UUIDUtil.CODEC.optionalFieldOf("owner").forGetter(d -> Optional.ofNullable(d.owner())),
            Codec.STRING.optionalFieldOf("owner_name", "").forGetter(TameData::ownerName)
    ).apply(inst, (owner, name) -> new TameData(owner.orElse(null), name)));

    /**
     * 驯养数据的流编解码器，用于网络传输。
     */
    public static final StreamCodec<ByteBuf, TameData> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);

    /**
     * 判断是否已驯养。
     *
     * @return 是否已驯养
     */
    public boolean isTamed() {
        return this.owner != null;
    }

    /**
     * 判断是否归属于指定 UUID。
     *
     * @param uuid 待比较的 UUID
     * @return 是否归属于该 UUID
     */
    public boolean isOwnedBy(UUID uuid) {
        return this.owner != null && this.owner.equals(uuid);
    }
}