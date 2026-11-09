package tequila.cat.pigpig.client;

import net.minecraft.world.entity.EntityType;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

import tequila.cat.pigpig.KaleidoscopeStartale;
import tequila.cat.pigpig.client.model.ModModelLayers;
import tequila.cat.pigpig.client.model.TamedArmadilloModel;
import tequila.cat.pigpig.client.model.TamedChickenModel;
import tequila.cat.pigpig.client.model.TamedCowModel;
import tequila.cat.pigpig.client.model.TamedPigModel;
import tequila.cat.pigpig.client.renderer.TamedArmadilloRenderer;
import tequila.cat.pigpig.client.renderer.TamedChickenRenderer;
import tequila.cat.pigpig.client.renderer.TamedCowRenderer;
import tequila.cat.pigpig.client.renderer.TamedPigRenderer;
import tequila.cat.pigpig.init.ModBlockEntities;

@EventBusSubscriber(modid = KaleidoscopeStartale.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientEvents {
    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(ModModelLayers.TAMED_PIG, TamedPigModel::createBodyLayer);
        event.registerLayerDefinition(ModModelLayers.TAMED_CHICKEN, TamedChickenModel::createBodyLayer);
        event.registerLayerDefinition(ModModelLayers.TAMED_COW, TamedCowModel::createBodyLayer);
        event.registerLayerDefinition(ModModelLayers.TAMED_ARMADILLO, TamedArmadilloModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.JUICER.get(), JuicerRenderer::new);
        event.registerEntityRenderer(EntityType.PIG, TamedPigRenderer::new);
        event.registerEntityRenderer(EntityType.CHICKEN, TamedChickenRenderer::new);
        event.registerEntityRenderer(EntityType.COW, TamedCowRenderer::new);
        event.registerEntityRenderer(EntityType.ARMADILLO, TamedArmadilloRenderer::new);
    }

    private ClientEvents() {
    }
}
