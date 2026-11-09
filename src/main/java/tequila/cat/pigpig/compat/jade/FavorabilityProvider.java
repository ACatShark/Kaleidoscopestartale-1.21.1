package tequila.cat.pigpig.compat.jade;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import tequila.cat.pigpig.KaleidoscopeStartale;
import tequila.cat.pigpig.data.Favorability;
import tequila.cat.pigpig.data.FavorabilityTracker;
import tequila.cat.pigpig.data.MobTaming;

/** Jade：显示已驯养生物的好感度等级与升级所需点数。 */
public enum FavorabilityProvider implements IEntityComponentProvider, IServerDataProvider<EntityAccessor> {
    INSTANCE;

    private static final String KEY_POINTS = "KaleidoscopeStartaleFavorability";
    private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(
            KaleidoscopeStartale.MODID, "favorability");

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public boolean shouldRequestData(EntityAccessor accessor) {
        return accessor.getEntity() instanceof LivingEntity living && !MobTaming.isForeignMaid(living);
    }

    @Override
    public void appendServerData(CompoundTag data, EntityAccessor accessor) {
        if (accessor.getEntity() instanceof LivingEntity living
                && !MobTaming.isForeignMaid(living)
                && FavorabilityTracker.hasData(living)) {
            data.putInt(KEY_POINTS, FavorabilityTracker.get(living).points());
        }
    }

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
