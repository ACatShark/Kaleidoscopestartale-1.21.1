package com.tequilacat.kaleidoscope_startale.init;

import com.tequilacat.kaleidoscope_startale.KaleidoscopeStartale;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public interface KSItemTags {

    /** 杂蔬饲料类物品 */
    TagKey<Item> LIVESTOCK_FEED = modTag("livestock_feed");
    /** 八宝肥虫饲料类物品 */
    TagKey<Item> POULTRY_FEED = modTag("poultry_feed");
    /** 通用饲料类物品 */
    TagKey<Item> UNIVERSAL_FEED = modTag("universal_feed");

    private static TagKey<Item> modTag(String name) {
        return TagKey.create(Registries.ITEM, KaleidoscopeStartale.id(name));
    }
}
