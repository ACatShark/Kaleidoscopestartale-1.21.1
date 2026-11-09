package tequila.cat.pigpig.compat.jade;

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

import tequila.cat.pigpig.KaleidoscopeStartale;
import tequila.cat.pigpig.data.PetterCooldown;

/** Jade：显示拍拍器剩余冷却。 */
public enum PetterCooldownProvider implements IEntityComponentProvider, StreamServerDataProvider<EntityAccessor, Integer> {
    INSTANCE;

    private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(
            KaleidoscopeStartale.MODID, "petter_cooldown");
    private static final int NOT_COOLING = 0;

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public Integer streamData(EntityAccessor accessor) {
        return PetterCooldown.remaining(accessor.getEntity());
    }

    /** Jade 15.10 起要求 RegistryFriendlyByteBuf 版本的 StreamCodec。 */
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

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, Integer> streamCodec() {
        return COOLDOWN_CODEC;
    }

    @Override
    public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
        int remaining = this.decodeFromData(accessor).orElse(NOT_COOLING);
        if (remaining > 0) {
            tooltip.add(Component.translatable("jade.kaleidoscope_startale.petter_cooldown",
                    IThemeHelper.get().seconds(remaining, accessor.tickRate())));
        }
    }
}
