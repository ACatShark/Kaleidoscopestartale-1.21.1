package com.tequilacat.kaleidoscope_startale.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class KSClientConfig {
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.BooleanValue HUD_ENABLED;
    public static final ModConfigSpec.DoubleValue HUD_PANEL_OPACITY;
    public static final ModConfigSpec.DoubleValue HUD_PICK_RANGE;
    public static final ModConfigSpec.DoubleValue HUD_FADE_SPEED;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.comment("界面配置").push("Hud");
        HUD_ENABLED = builder
                .comment("是否在准星右侧显示已驯养生物的信息（需要佩戴草帽）")
                .comment("Show tamed mob info next to the crosshair (requires a straw hat)")
                .define("enabled", true);
        HUD_PANEL_OPACITY = builder
                .comment("信息面板的不透明度，0 为完全透明，1 为完全不透明")
                .comment("Info panel opacity, 0 is fully transparent and 1 is fully opaque")
                .defineInRange("panelOpacity", 0.5D, 0.0D, 1.0D);
        HUD_PICK_RANGE = builder
                .comment("准星检索距离（格）")
                .comment("Pick range in blocks")
                .defineInRange("pickRange", 4.0D, 0.0D, Integer.MAX_VALUE);
        HUD_FADE_SPEED = builder
                .comment("淡入淡出速度（每秒）")
                .comment("Fade speed in seconds per tick")
                .defineInRange("fadeSpeed", 6.0F, 0.0F, Integer.MAX_VALUE);
        builder.pop();

        SPEC = builder.build();
    }
}
