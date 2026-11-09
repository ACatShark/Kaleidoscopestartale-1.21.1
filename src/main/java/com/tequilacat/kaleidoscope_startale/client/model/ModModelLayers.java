package com.tequilacat.kaleidoscope_startale.client.model;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

import com.tequilacat.kaleidoscope_startale.KaleidoscopeStartale;

/**
 * 模组模型层定义。
 * <p>
 * 声明已驯养的猪、鸡、牛、犰狳所使用的模型层位置，
 * 供客户端渲染器在烘焙模型时引用。
 */
public final class ModModelLayers {

    /** 已驯养猪模型层。 */
    public static final ModelLayerLocation TAMED_PIG = create("tamed_pig");
    /** 已驯养鸡模型层。 */
    public static final ModelLayerLocation TAMED_CHICKEN = create("tamed_chicken");
    /** 已驯养牛模型层。 */
    public static final ModelLayerLocation TAMED_COW = create("tamed_cow");
    /** 已驯养犰狳模型层。 */
    public static final ModelLayerLocation TAMED_ARMADILLO = create("tamed_armadillo");

    /**
     * 创建模型层位置。
     * <p>
     * 使用本模组命名空间与固定的「main」网格标识。
     *
     * @param name 模型层名称
     * @return 模型层位置
     */
    private static ModelLayerLocation create(String name) {
        return new ModelLayerLocation(KaleidoscopeStartale.id(name), "main");
    }
}