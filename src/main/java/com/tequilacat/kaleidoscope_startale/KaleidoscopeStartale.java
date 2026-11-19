package com.tequilacat.kaleidoscope_startale;

import com.mojang.logging.LogUtils;
import com.tequilacat.kaleidoscope_startale.config.KSClientConfig;
import com.tequilacat.kaleidoscope_startale.init.KSAttachments;
import com.tequilacat.kaleidoscope_startale.config.KSServerConfig;
import com.tequilacat.kaleidoscope_startale.init.KSFluids;
import com.tequilacat.kaleidoscope_startale.init.*;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

@Mod(KaleidoscopeStartale.MODID)
public class KaleidoscopeStartale {
    public static final String MODID = "kaleidoscope_startale";
    public static final Logger LOGGER = LogUtils.getLogger();

    public KaleidoscopeStartale(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, KSServerConfig.SPEC);
        modContainer.registerConfig(ModConfig.Type.CLIENT, KSClientConfig.SPEC);

        KSBlocks.register(modEventBus);
        KSItems.register(modEventBus);
        KSBlockEntities.register(modEventBus);
        KSRecipes.register(modEventBus);
        KSSounds.register(modEventBus);
        KSFluids.register(modEventBus);
        KSCreativeModeTabs.register(modEventBus);
        KSAttachments.TYPES.register(modEventBus);
        KSTeacups.init();
        KSEffects.register(modEventBus);
        KSLootModifiers.register(modEventBus);
        KSEntities.register(modEventBus);
    }

    public static ResourceLocation id(String name) {
        return ResourceLocation.fromNamespaceAndPath(MODID, name);
    }
}
