package tequila.cat.pigpig.config;

import java.util.List;

import net.minecraft.resources.ResourceLocation;

import net.neoforged.neoforge.common.ModConfigSpec;

import tequila.cat.pigpig.data.FeedRegistry;

public final class StartaleConfig {
    public static final ModConfigSpec SPEC;

    private static final ModConfigSpec.ConfigValue<List<? extends String>> UNIVERSAL_FEED_BLACKLIST;

    private static final List<String> DEFAULT_BLACKLIST = List.of("touhou_little_maid:maid", "touhou_little_maid:fairy");

    /** 通用饲料黑名单（实体 ID）。配置未加载时回落到默认值。 */
    public static List<String> universalFeedBlacklist() {
        if (!SPEC.isLoaded()) {
            return DEFAULT_BLACKLIST;
        }
        try {
            return List.copyOf(UNIVERSAL_FEED_BLACKLIST.get());
        } catch (IllegalStateException e) {
            return DEFAULT_BLACKLIST;
        }
    }

    public static void onConfigChanged() {
        FeedRegistry.invalidateBlacklistCache();
    }

    private StartaleConfig() {
    }

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.push("universal_feed");
        UNIVERSAL_FEED_BLACKLIST = builder
                .comment("Blacklisted entity IDs ignore the universal feed entirely.",
                        "Format: namespace:path")
                .defineListAllowEmpty("blacklist", DEFAULT_BLACKLIST,
                        obj -> obj instanceof String s && ResourceLocation.tryParse(s) != null);
        builder.pop();
        SPEC = builder.build();
    }
}
