package com.tequilacat.kaleidoscope_startale.client;

import com.tequilacat.kaleidoscope_startale.KaleidoscopeStartale;
import com.tequilacat.kaleidoscope_startale.client.model.ModModelLayers;
import com.tequilacat.kaleidoscope_startale.client.model.entity.tamed.TamedArmadilloModel;
import com.tequilacat.kaleidoscope_startale.client.model.entity.tamed.TamedChickenModel;
import com.tequilacat.kaleidoscope_startale.client.model.entity.tamed.TamedCowModel;
import com.tequilacat.kaleidoscope_startale.client.model.entity.tamed.TamedPigModel;
import com.tequilacat.kaleidoscope_startale.client.renderer.entity.animal.TamedArmadilloRenderer;
import com.tequilacat.kaleidoscope_startale.client.renderer.entity.animal.TamedChickenRenderer;
import com.tequilacat.kaleidoscope_startale.client.renderer.entity.animal.TamedCowRenderer;
import com.tequilacat.kaleidoscope_startale.client.renderer.entity.animal.TamedPigRenderer;
import com.tequilacat.kaleidoscope_startale.init.KSBlockEntities;
import com.tequilacat.kaleidoscope_startale.init.KSItems;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.entity.EntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

/**
 * 客户端事件类。
 * <p>
 * 注册模型层定义与渲染器：为已驯养动物模型注册身体层，
 * 为榨汁机方块实体注册渲染器，并为猪、鸡、牛、犰狳替换为自定义渲染器，
 * 同时为榨汁机物品注册客户端扩展。
 */
@EventBusSubscriber(modid = KaleidoscopeStartale.MODID, value = Dist.CLIENT)
public class ClientEvents {

    /**
     * 注册模型层定义。
     * <p>
     * 为已驯养的猪、鸡、牛、犰狳模型注册身体层。
     *
     * @param event 模型层定义注册事件
     */
    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(ModModelLayers.TAMED_PIG, TamedPigModel::createBodyLayer);
        event.registerLayerDefinition(ModModelLayers.TAMED_CHICKEN, TamedChickenModel::createBodyLayer);
        event.registerLayerDefinition(ModModelLayers.TAMED_COW, TamedCowModel::createBodyLayer);
        event.registerLayerDefinition(ModModelLayers.TAMED_ARMADILLO, TamedArmadilloModel::createBodyLayer);
    }

    /**
     * 注册渲染器。
     * <p>
     * 为榨汁机方块实体注册渲染器，并为猪、鸡、牛、犰狳
     * 替换为已驯养动物对应的自定义渲染器。
     *
     * @param event 渲染器注册事件
     */
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(EntityType.PIG, TamedPigRenderer::new);
        event.registerEntityRenderer(EntityType.CHICKEN, TamedChickenRenderer::new);
        event.registerEntityRenderer(EntityType.COW, TamedCowRenderer::new);
        event.registerEntityRenderer(EntityType.ARMADILLO, TamedArmadilloRenderer::new);
    }

}