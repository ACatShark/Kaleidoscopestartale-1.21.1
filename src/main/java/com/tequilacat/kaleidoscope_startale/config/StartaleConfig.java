package com.tequilacat.kaleidoscope_startale.config;

import java.util.List;

import net.minecraft.resources.ResourceLocation;

import net.neoforged.neoforge.common.ModConfigSpec;

import com.tequilacat.kaleidoscope_startale.data.favorability.Favorability;

public class StartaleConfig {
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.ConfigValue<List<? extends String>> UNIVERSAL_FEED_BLACKLIST;

    public static final ModConfigSpec.IntValue PETTER_BASE_DROP_CAP;
    public static final ModConfigSpec.IntValue PETTER_LOOTING_CAP_PER_LEVEL;
    public static final ModConfigSpec.ConfigValue<List<? extends Number>> PETTER_FAVOR_CAP_LEVELS;
    public static final ModConfigSpec.DoubleValue PETTER_LOOTING_DROP_CHANCE_PER_LEVEL;
    public static final ModConfigSpec.DoubleValue PETTER_FAVOR_DROP_CHANCE_PER_LEVEL;
    public static final ModConfigSpec.DoubleValue PETTER_RANCH_HERO_DROP_CHANCE_BONUS;
    public static final ModConfigSpec.IntValue PETTER_RANCH_HERO_EXTRA_ROLLS;
    public static final ModConfigSpec.IntValue PETTER_RANCH_HERO_SQUEEZE_POWER;

    public static final ModConfigSpec.IntValue TAME_CHANCE;
    public static final ModConfigSpec.DoubleValue TAME_HEAL;
    public static final ModConfigSpec.DoubleValue TEMPTATION_SPEED;
    public static final ModConfigSpec.IntValue PETTER_COOLDOWN_TICKS;

    public static final ModConfigSpec.IntValue FAVOR_POINTS_PER_PET;
    public static final ModConfigSpec.IntValue FAVOR_POINTS_PER_FEED;
    public static final ModConfigSpec.IntValue FAVOR_POINTS_PER_DAY;
    public static final ModConfigSpec.IntValue FAVOR_MAX_FEEDS_PER_DAY;
    public static final ModConfigSpec.IntValue FAVOR_POINTS_PER_LEVEL;

    public static final ModConfigSpec.DoubleValue TAMED_MAX_DAMAGE_RATIO;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.comment("通用饲料配置").push("Universal Feed");

        UNIVERSAL_FEED_BLACKLIST = builder
                .comment(
                        "通用饲料黑名单",
                        "格式: namespace:path",
                        "敌对生物与原版可驯服动物(狼/猫/鹦鹉)默认已忽略，",
                        "只有模组生物漏网时才需要写在这里"
                )
                .comment(
                        "Entity IDs blacklisted from the universal feed",
                        "Format: namespace:path",
                        "Hostile mobs and vanilla tamables (wolf/cat/parrot) are already ignored by default,",
                        "list them here only if a modded variant slips through"
                )
                .defineListAllowEmpty("blacklist", List.of(
                        "touhou_little_maid:maid",
                        "touhou_little_maid:fairy"
                ), obj -> obj instanceof String s && ResourceLocation.tryParse(s) != null);

        builder.pop();

        builder.comment("拍拍器配置").push("Petter");
        PETTER_BASE_DROP_CAP = builder
                .comment("基础掉落物数量上限")
                .comment("Base drop cap per pet")
                .defineInRange("baseDropCap", 1, 1, Integer.MAX_VALUE);
        PETTER_LOOTING_CAP_PER_LEVEL = builder
                .comment("每级抢夺提高的掉落物上限")
                .comment("Drop cap increase per Looting level")
                .defineInRange("lootingCapPerLevel", 1, 0, Integer.MAX_VALUE);
        PETTER_FAVOR_CAP_LEVELS = builder
                .comment("好感度达到这些等级时，掉落物上限各 +1")
                .comment("Favor levels that each add 1 to the drop cap")
                .defineListAllowEmpty("favorCapLevels", List.of(3, 5, 8, 10),
                        obj -> obj instanceof Number level
                                && level.intValue() >= 1
                                && level.intValue() <= Favorability.MAX_LEVEL);
        PETTER_LOOTING_DROP_CHANCE_PER_LEVEL = builder
                .comment("每级抢夺提高的额外掉落概率（%）")
                .comment("Extra drop chance per Looting level")
                .defineInRange("lootingDropChancePerLevel", 0.2D, 0.0D, 1.0D);
        PETTER_FAVOR_DROP_CHANCE_PER_LEVEL = builder
                .comment("每级好感度提高的额外掉落概率（%）")
                .comment("Extra drop chance per favor level above 1")
                .defineInRange("favorDropChancePerLevel", 0.05D, 0.0D, 1.0D);
        PETTER_RANCH_HERO_DROP_CHANCE_BONUS = builder
                .comment("牧场英雄效果提供的额外掉落概率（%）")
                .comment("Extra drop chance granted by the Ranch Hero effect")
                .defineInRange("ranchHeroDropChanceBonus", 0.35D, 0.0D, 1.0D);
        PETTER_RANCH_HERO_EXTRA_ROLLS = builder
                .comment("牧场英雄效果额外滚取掉落表的次数")
                .comment("Extra loot table rolls granted by the Ranch Hero effect")
                .defineInRange("ranchHeroExtraRolls", 1, 0, Integer.MAX_VALUE);
        PETTER_RANCH_HERO_SQUEEZE_POWER = builder
                .comment("牧场英雄效果提供的拍击力度加成")
                .comment("Squeeze power bonus granted by the Ranch Hero effect")
                .defineInRange("ranchHeroSqueezePower", 2, 0, Integer.MAX_VALUE);
        builder.pop();

        builder.comment("驯养配置").push("Taming");
        TAME_CHANCE = builder
                .comment("喂食驯养成功率的分母（3 表示 1/3 概率）")
                .comment("Denominator of the feed taming chance (3 means 1 in 3)")
                .defineInRange("tameChance", 3, 1, Integer.MAX_VALUE);
        TAME_HEAL = builder
                .comment("喂食时恢复的生命值")
                .comment("Health restored when feeding")
                .defineInRange("tameHeal", 2.0D, 0.0D, Integer.MAX_VALUE);
        TEMPTATION_SPEED = builder
                .comment("被饲料吸引时的移动速度")
                .comment("Movement speed while tempted by feed")
                .defineInRange("temptationSpeed", 1.25D, 0.1D, 4.0D);
        PETTER_COOLDOWN_TICKS = builder
                .comment("拍拍冷却时长（tick）")
                .comment("Petter cooldown in ticks")
                .defineInRange("petterCooldownTicks", 12000, 0, Integer.MAX_VALUE);
        builder.pop();

        builder.comment("好感度配置").push("Favorability");
        FAVOR_POINTS_PER_PET = builder
                .comment("每次拍击获得的好感度点数")
                .comment("Favor points gained per pet")
                .defineInRange("pointsPerPet", 10, 0, Integer.MAX_VALUE);
        FAVOR_POINTS_PER_FEED = builder
                .comment("每次喂食获得的好感度点数")
                .comment("Favor points gained per feed")
                .defineInRange("pointsPerFeed", 5, 0, Integer.MAX_VALUE);
        FAVOR_POINTS_PER_DAY = builder
                .comment("每日结算获得的好感度点数")
                .comment("Favor points gained on the daily rollover")
                .defineInRange("pointsPerDay", 10, 0, Integer.MAX_VALUE);
        FAVOR_MAX_FEEDS_PER_DAY = builder
                .comment("每日可获得好感度的喂食次数")
                .comment("Feeds per day that still grant favor points")
                .defineInRange("maxFeedsPerDay", 3, 0, Integer.MAX_VALUE);
        FAVOR_POINTS_PER_LEVEL = builder
                .comment("每级好感度所需的点数")
                .comment("Favor points required per level")
                .defineInRange("pointsPerLevel", 100, 1, Integer.MAX_VALUE);
        builder.pop();

        builder.comment("已驯养生物保护配置").push("Tamed Protection");
        TAMED_MAX_DAMAGE_RATIO = builder
                .comment("单次受伤的最大生命值比例，1.0 表示不限制")
                .comment("Max damage per hit as a fraction of max health, 1.0 disables the cap")
                .defineInRange("maxDamageRatio", 0.25D, 0.0D, 1.0D);
        builder.pop();

        SPEC = builder.build();
    }
}
