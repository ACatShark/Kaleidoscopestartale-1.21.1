package com.tequilacat.kaleidoscope_startale.config;

import java.util.List;

import net.minecraft.resources.ResourceLocation;

import net.neoforged.neoforge.common.ModConfigSpec;

import com.tequilacat.kaleidoscope_startale.util.FeedRegistry;

public class StartaleConfig {
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.ConfigValue<List<? extends String>> UNIVERSAL_FEED_BLACKLIST;

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

        SPEC = builder.build();
    }

    public static List<String> universalFeedBlacklist() {
        if (SPEC.isLoaded()) {
            try {
                return List.copyOf(UNIVERSAL_FEED_BLACKLIST.get());
            } catch (IllegalStateException ignored) {
                // 落到下面的默认值分支
            }
        }
        return List.copyOf(UNIVERSAL_FEED_BLACKLIST.getDefault());
    }

    public static void onConfigChanged() {
        FeedRegistry.invalidateBlacklistCache();
    }
}
