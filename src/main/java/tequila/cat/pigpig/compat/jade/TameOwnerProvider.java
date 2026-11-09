package tequila.cat.pigpig.compat.jade;

import org.jetbrains.annotations.Nullable;

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

import tequila.cat.pigpig.KaleidoscopeStartale;
import tequila.cat.pigpig.attachment.ModAttachments;
import tequila.cat.pigpig.attachment.TameData;

/**
 * Jade：显示本模组驯养动物的主人。
 * 原版可驯服生物（OwnableEntity）已由 Jade 自带逻辑显示，这里直接跳过避免重复。
 */
public enum TameOwnerProvider implements IEntityComponentProvider, IServerDataProvider<EntityAccessor> {
    INSTANCE;

    private static final String KEY = "KaleidoscopeStartaleOwner";
    private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(
            KaleidoscopeStartale.MODID, "animal_owner");

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public boolean shouldRequestData(EntityAccessor accessor) {
        return ownerName(accessor.getEntity()) != null;
    }

    @Override
    public void appendServerData(CompoundTag data, EntityAccessor accessor) {
        String owner = ownerName(accessor.getEntity());
        if (owner != null) {
            data.putString(KEY, owner);
        }
    }

    @Override
    public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
        String owner = accessor.getServerData().getString(KEY);
        if (!owner.isEmpty()) {
            tooltip.add(Component.translatable("jade.owner", owner));
        }
    }

    @Nullable
    private static String ownerName(Entity entity) {
        if (!(entity instanceof Animal)) {
            return null;
        }
        if (entity instanceof OwnableEntity ownable && ownable.getOwnerUUID() != null) {
            return null;
        }

        TameData data = entity.getData(ModAttachments.TAME.get());
        return data.isTamed() && !data.ownerName().isEmpty() ? data.ownerName() : null;
    }
}
