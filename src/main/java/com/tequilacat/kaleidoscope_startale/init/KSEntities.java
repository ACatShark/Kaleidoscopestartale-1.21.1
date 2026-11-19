package com.tequilacat.kaleidoscope_startale.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import com.tequilacat.kaleidoscope_startale.KaleidoscopeStartale;
import com.tequilacat.kaleidoscope_startale.entity.RollingKitchenEntity;
import com.tequilacat.kaleidoscope_startale.entity.WiggleCarEntity;

public interface KSEntities {
    DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(Registries.ENTITY_TYPE, KaleidoscopeStartale.MODID);

    /** 流动小厨载具。 */
    DeferredHolder<EntityType<?>, EntityType<RollingKitchenEntity>> ROLLING_KITCHEN =
            ENTITIES.register("rolling_kitchen", () -> EntityType.Builder
                    .of(RollingKitchenEntity::new, MobCategory.MISC)
                    .sized(3.0F, 1.8F)
                    .eyeHeight(1.5F)
                    .clientTrackingRange(10)
                    .updateInterval(2)
                    .build(KaleidoscopeStartale.id("rolling_kitchen").toString()));

    /** 扭扭车载具。 */
    DeferredHolder<EntityType<?>, EntityType<WiggleCarEntity>> WIGGLE_CAR =
            ENTITIES.register("wiggle_car", () -> EntityType.Builder
                    .of(WiggleCarEntity::new, MobCategory.MISC)
                    .sized(0.9F, 0.9F)
                    .eyeHeight(0.5F)
                    .clientTrackingRange(10)
                    .updateInterval(2)
                    .build(KaleidoscopeStartale.id("wiggle_car").toString()));

    static void register(IEventBus modEventBus) {
        ENTITIES.register(modEventBus);
    }
}
