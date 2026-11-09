package tequila.cat.pigpig.attachment;

import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * 驯养绑定数据（挂在实体附件 "livestock" 上，服务端权威）。
 * 所有字段都用 optionalFieldOf，保证旧存档读档兼容。
 */
public record TameData(@Nullable UUID owner, String ownerName) {
    public static final TameData EMPTY = new TameData(null, "");

    public static final Codec<TameData> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            UUIDUtil.CODEC.optionalFieldOf("owner").forGetter(d -> Optional.ofNullable(d.owner())),
            Codec.STRING.optionalFieldOf("owner_name", "").forGetter(TameData::ownerName)
    ).apply(inst, (owner, name) -> new TameData(owner.orElse(null), name)));

    public static final StreamCodec<ByteBuf, TameData> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);

    public boolean isTamed() {
        return this.owner != null;
    }

    public boolean isOwnedBy(UUID uuid) {
        return this.owner != null && this.owner.equals(uuid);
    }
}
