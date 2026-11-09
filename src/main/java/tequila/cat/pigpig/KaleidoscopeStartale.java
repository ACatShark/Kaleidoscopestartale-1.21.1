package tequila.cat.pigpig;

import com.mojang.logging.LogUtils;

import org.slf4j.Logger;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.NeoForge;

import tequila.cat.pigpig.attachment.ModAttachments;
import tequila.cat.pigpig.config.StartaleConfig;
import tequila.cat.pigpig.event.FeedEvents;
import tequila.cat.pigpig.event.InspirationEvents;
import tequila.cat.pigpig.event.PetterEvents;
import tequila.cat.pigpig.event.TamedProtectionEvents;
import tequila.cat.pigpig.event.VinePlantingEvents;
import tequila.cat.pigpig.fluid.ModFluids;
import tequila.cat.pigpig.init.ModBlockEntities;
import tequila.cat.pigpig.init.ModBlocks;
import tequila.cat.pigpig.init.ModCreativeModeTabs;
import tequila.cat.pigpig.init.ModEffects;
import tequila.cat.pigpig.init.ModItems;
import tequila.cat.pigpig.init.ModRecipes;
import tequila.cat.pigpig.init.ModSounds;

@Mod(KaleidoscopeStartale.MODID)
public class KaleidoscopeStartale {
    public static final String MODID = "kaleidoscope_startale";
    public static final Logger LOGGER = LogUtils.getLogger();

    public KaleidoscopeStartale(IEventBus modEventBus, ModContainer modContainer) {
        // common 配置：通用饲料黑名单
        modContainer.registerConfig(ModConfig.Type.COMMON, StartaleConfig.SPEC);
        modEventBus.addListener((ModConfigEvent.Loading event) -> StartaleConfig.onConfigChanged());
        modEventBus.addListener((ModConfigEvent.Reloading event) -> StartaleConfig.onConfigChanged());

        // 注册表（mod 总线）
        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModRecipes.RECIPE_SERIALIZERS.register(modEventBus);
        ModSounds.SOUNDS.register(modEventBus);
        ModFluids.FLUID_TYPES.register(modEventBus);
        ModFluids.FLUIDS.register(modEventBus);
        ModCreativeModeTabs.CREATIVE_MODE_TABS.register(modEventBus);
        ModAttachments.TYPES.register(modEventBus);
        ModEffects.MOB_EFFECTS.register(modEventBus);

        // 运行时事件（NeoForge 游戏总线）
        NeoForge.EVENT_BUS.register(new VinePlantingEvents());
        NeoForge.EVENT_BUS.register(new FeedEvents());
        NeoForge.EVENT_BUS.register(new PetterEvents());
        NeoForge.EVENT_BUS.register(new TamedProtectionEvents());
        NeoForge.EVENT_BUS.register(new InspirationEvents());
    }
}
