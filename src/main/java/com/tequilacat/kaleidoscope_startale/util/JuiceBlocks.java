package com.tequilacat.kaleidoscope_startale.util;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

import net.neoforged.neoforge.registries.DeferredBlock;

import com.tequilacat.kaleidoscope_startale.init.KSBlocks;

/**
 * 果汁方块映射工具类。
 * <p>
 * 根据酒馆模组的果汁流体查找对应的果汁方块，
 * 用于将流体转换为可放置的果汁方块。
 */
public class JuiceBlocks {

    /** 每杯果汁对应的流体量。 */
    public static final int AMOUNT_PER_GLASS = 250;

    /**
     * 酒馆模组的命名空间。
     */
    private static final String TAVERN = "kaleidoscope_tavern";

    /**
     * 根据流体获取对应的果汁方块。
     * <p>
     * 仅处理酒馆模组命名空间下的流体，按流体路径匹配果汁方块；
     * 非酒馆流体或未匹配的流体返回 null。
     *
     * @param fluid 流体
     * @return 对应的果汁方块，未匹配时返回 null
     */
    public static Block blockForFluid(Fluid fluid) {
        ResourceLocation id = BuiltInRegistries.FLUID.getKey(fluid);
        if (!TAVERN.equals(id.getNamespace())) {
            return null;
        }

        DeferredBlock<? extends Block> holder = switch (id.getPath()) {
            case "sweet_berries_juice" -> KSBlocks.BERRY_JUICE;
            case "glow_berries_juice" -> KSBlocks.GLOW_BERRY_JUICE;
            case "grape_juice" -> KSBlocks.GRAPE_JUICE;
            case "gold_grape_juice" -> KSBlocks.GOLD_GRAPE_JUICE;
            case "ice_grape_juice" -> KSBlocks.ICE_GRAPE_JUICE;
            case "green_grape_juice" -> KSBlocks.GREEN_GRAPE_JUICE;
            default -> null;
        };
        return holder == null ? null : holder.get();
    }
}