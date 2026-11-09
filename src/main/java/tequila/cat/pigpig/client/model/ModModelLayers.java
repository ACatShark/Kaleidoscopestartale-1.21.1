package tequila.cat.pigpig.client.model;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

import tequila.cat.pigpig.KaleidoscopeStartale;

public final class ModModelLayers {
    public static final ModelLayerLocation TAMED_PIG = create("tamed_pig");
    public static final ModelLayerLocation TAMED_CHICKEN = create("tamed_chicken");
    public static final ModelLayerLocation TAMED_COW = create("tamed_cow");
    public static final ModelLayerLocation TAMED_ARMADILLO = create("tamed_armadillo");

    private static ModelLayerLocation create(String name) {
        return new ModelLayerLocation(
                ResourceLocation.fromNamespaceAndPath(KaleidoscopeStartale.MODID, name), "main");
    }

    private ModModelLayers() {
    }
}
