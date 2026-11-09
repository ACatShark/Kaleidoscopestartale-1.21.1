package tequila.cat.pigpig.data;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

import net.neoforged.neoforge.registries.DeferredBlock;

import tequila.cat.pigpig.init.ModBlocks;

/**
 * 果汁（酒馆流体）到果汁杯方块的映射。
 * 六种果汁流体来自森罗物语：酒馆的压榨配方（甜浆果、发光浆果、葡萄、黄金葡萄、
 * 冰葡萄、青提葡萄），榨汁机榨出的也是这些流体，倒进空酒杯后要换成本模组的果汁杯方块。
 */
public final class JuiceBlocks {
    /** 灌满一只空酒杯需要的果汁量（mB）。 */
    public static final int AMOUNT_PER_GLASS = 250;

    private static final String TAVERN = "kaleidoscope_tavern";

    /** 按流体 id 找到对应的果汁杯方块，找不到返回 null。 */
    public static Block blockForFluid(Fluid fluid) {
        ResourceLocation id = BuiltInRegistries.FLUID.getKey(fluid);
        if (id == null || !TAVERN.equals(id.getNamespace())) {
            return null;
        }

        DeferredBlock<? extends Block> holder = switch (id.getPath()) {
            case "sweet_berries_juice" -> ModBlocks.BERRY_JUICE;
            case "glow_berries_juice" -> ModBlocks.GLOW_BERRY_JUICE;
            case "grape_juice" -> ModBlocks.GRAPE_JUICE;
            case "gold_grape_juice" -> ModBlocks.GOLD_GRAPE_JUICE;
            case "ice_grape_juice" -> ModBlocks.ICE_GRAPE_JUICE;
            case "green_grape_juice" -> ModBlocks.GREEN_GRAPE_JUICE;
            default -> null;
        };
        return holder == null ? null : holder.get();
    }

    private JuiceBlocks() {
    }
}
