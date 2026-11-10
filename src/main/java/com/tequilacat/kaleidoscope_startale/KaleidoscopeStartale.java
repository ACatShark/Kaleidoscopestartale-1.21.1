package com.tequilacat.kaleidoscope_startale;

import com.mojang.logging.LogUtils;
import com.tequilacat.kaleidoscope_startale.init.KSAttachments;
import com.tequilacat.kaleidoscope_startale.config.StartaleConfig;
import com.tequilacat.kaleidoscope_startale.event.*;
import com.tequilacat.kaleidoscope_startale.event.effect.InspirationEvents;
import com.tequilacat.kaleidoscope_startale.init.KSFluids;
import com.tequilacat.kaleidoscope_startale.init.*;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(KaleidoscopeStartale.MODID)
public class KaleidoscopeStartale {
    public static final String MODID = "kaleidoscope_startale";
    public static final Logger LOGGER = LogUtils.getLogger();

    public KaleidoscopeStartale(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, StartaleConfig.SPEC);
        modEventBus.addListener((ModConfigEvent.Loading event) -> StartaleConfig.onConfigChanged());
        modEventBus.addListener((ModConfigEvent.Reloading event) -> StartaleConfig.onConfigChanged());

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

        NeoForge.EVENT_BUS.register(new VinePlantingEvents());
        NeoForge.EVENT_BUS.register(new FeedEvents());
        NeoForge.EVENT_BUS.register(new PetterEvents());
        NeoForge.EVENT_BUS.register(new TamedProtectionEvents());
        NeoForge.EVENT_BUS.register(new InspirationEvents());
    }

    public static ResourceLocation id(String name) {
        return ResourceLocation.fromNamespaceAndPath(MODID, name);
    }
}
