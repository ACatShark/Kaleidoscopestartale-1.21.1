package com.tequilacat.kaleidoscope_startale.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public interface KSBlockTags {

    // Carry On:禁止搬起的方块
    TagKey<Block> CARRY_ON_BLACKLIST = carryOnBlacklist("block_blacklist");
    // 静谧四季
    TagKey<Block> SPRING_CROPS_BLOCK = seasonsBlockTag("spring_crops");
    TagKey<Block> SUMMER_CROPS_BLOCK = seasonsBlockTag("summer_crops");
    TagKey<Block> AUTUMN_CROPS_BLOCK = seasonsBlockTag("autumn_crops");
    TagKey<Block> WINTER_CROPS_BLOCK = seasonsBlockTag("winter_crops");
    TagKey<Block> YEAR_ROUND_CROPS_BLOCK = seasonsBlockTag("year_round_crops");
    // 节气
    TagKey<Block> CROPS_SPRING = eclipticSeasonsTag("crops/spring"); // 春
    TagKey<Block> CROPS_SUMMER = eclipticSeasonsTag("crops/summer"); // 夏
    TagKey<Block> CROPS_AUTUMN = eclipticSeasonsTag("crops/autumn"); // 秋
    TagKey<Block> CROPS_WINTER = eclipticSeasonsTag("crops/winter"); // 冬
    TagKey<Block> CROPS_ALL_SEASONS_BLOCK = seasonsBlockTag("crops/all_seasons"); // 全年生长
    TagKey<Block> CROPS_DRY_AVERAGE = eclipticSeasonsTag("crops/dry_average"); // 干燥
    TagKey<Block> CROPS_AVERAGE_MOIST = eclipticSeasonsTag("crops/average_moist"); // 一般
    TagKey<Block> CROPS_MOIST_HUMID = eclipticSeasonsTag("crops/moist_humid"); // 湿润
    TagKey<Block> CROPS_HUMID_HUMID = eclipticSeasonsTag("crops/humid_humid"); // 潮湿


    static TagKey<Block> mcBlockTag(String name) {
        return TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("minecraft", name));
    }

    static TagKey<Block> seasonsBlockTag(String name) {
        return TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("sereneseasons", name));
    }

    static TagKey<Block> eclipticSeasonsTag(String name) {
        return TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("eclipticseasons", name));
    }

    static TagKey<Block> carryOnBlacklist(String name) {
        return TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("carryon", name));
    }
}
