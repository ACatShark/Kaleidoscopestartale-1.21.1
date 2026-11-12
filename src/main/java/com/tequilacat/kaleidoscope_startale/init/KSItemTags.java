package com.tequilacat.kaleidoscope_startale.init;

import com.tequilacat.kaleidoscope_startale.KaleidoscopeStartale;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public interface KSItemTags {

    // ==================== 模组本体 ====================
    /** 杂蔬饲料类物品 */
    TagKey<Item> LIVESTOCK_FEED = modTag("livestock_feed");
    /** 八宝肥虫饲料类物品 */
    TagKey<Item> POULTRY_FEED = modTag("poultry_feed");
    /** 通用饲料类物品 */
    TagKey<Item> UNIVERSAL_FEED = modTag("universal_feed");
    /** 抢夺额外掉落。 */
    TagKey<Item> LOOTING_APPLICABLE = modTag("looting_applicable");
    /** 允许显示该面板的头部装备。 */
    TagKey<Item> TAMED_DATA_DISPLAY_HAT = modTag("tamed_data_display_hat");


    // ==================== 作物 ====================
    TagKey<Item> CROPS_AMARANTH = neoforgeItemTag("crop/amaranth");
    TagKey<Item> CROPS_ANCIENT_FRUIT = neoforgeItemTag("crop/ancient_fruit");
    TagKey<Item> CROPS_RHUBARB = neoforgeItemTag("crop/rhubarb");
    TagKey<Item> FRUITS = neoforgeItemTag("fruits");
    TagKey<Item> GRAIN = neoforgeItemTag("grain");
    TagKey<Item> VEGETABLES = neoforgeItemTag("vegetables");


    // ==================== Diet ====================
    TagKey<Item> DIET_FRUITS = dietItemTag("fruits");
    TagKey<Item> DIET_GRAINS = dietItemTag("grains");
    TagKey<Item> DIET_PROTEINS = dietItemTag("proteins");
    TagKey<Item> DIET_VEGETABLES = dietItemTag("vegetables");


    private static TagKey<Item> modTag(String name) {
        return TagKey.create(Registries.ITEM, KaleidoscopeStartale.id(name));
    }

    private static TagKey<Item> neoforgeItemTag(String name) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", name));
    }

    private static TagKey<Item> dietItemTag(String name) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("diet", name));
    }
}
