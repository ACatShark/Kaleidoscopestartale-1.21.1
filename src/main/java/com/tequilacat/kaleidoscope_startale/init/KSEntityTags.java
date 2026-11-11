package com.tequilacat.kaleidoscope_startale.init;

import com.tequilacat.kaleidoscope_startale.KaleidoscopeStartale;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

public interface KSEntityTags {

    /** 杂蔬饲料喂养的生物 */
    TagKey<EntityType<?>> LIVESTOCK_FEED_MOBS = modTag("livestock_feed_mobs");
    /** 八宝肥虫饲料喂养的生物 */
    TagKey<EntityType<?>> POULTRY_FEED_MOBS = modTag("poultry_feed_mobs");
    /** 通用饲料喂养的生物 */
    TagKey<EntityType<?>> UNIVERSAL_FEED_MOBS = modTag("universal_feed_mobs");

    private static TagKey<EntityType<?>> modTag(String name) {
        return TagKey.create(Registries.ENTITY_TYPE, KaleidoscopeStartale.id(name));
    }
}
