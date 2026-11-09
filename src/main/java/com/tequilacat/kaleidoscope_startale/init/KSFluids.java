package com.tequilacat.kaleidoscope_startale.init;

import com.tequilacat.kaleidoscope_startale.KaleidoscopeStartale;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class KSFluids {
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(BuiltInRegistries.FLUID, KaleidoscopeStartale.MODID);

    public static final ResourceLocation ANCIENT_FRUIT_JUICE_ID = KaleidoscopeStartale.id("ancient_fruit_juice");
    public static final ResourceLocation FLOWING_ANCIENT_FRUIT_JUICE_ID = KaleidoscopeStartale.id("flowing_ancient_fruit_juice");

    /** 上古水果汁 */
    public static final Supplier<Fluid> ANCIENT_FRUIT_JUICE = FLUIDS.register("ancient_fruit_juice",
        () -> new BaseFlowingFluid.Source(ancientFruitJuiceProperties()));
    public static final Supplier<Fluid> FLOWING_ANCIENT_FRUIT_JUICE = FLUIDS.register("flowing_ancient_fruit_juice",
        () -> new BaseFlowingFluid.Flowing(ancientFruitJuiceProperties()));

    private static BaseFlowingFluid.Properties ancientFruitJuiceProperties() {
        return new BaseFlowingFluid.Properties(KSFluidTypes.ANCIENT_FRUIT_JUICE_TYPE, ANCIENT_FRUIT_JUICE, FLOWING_ANCIENT_FRUIT_JUICE)
                .bucket(KSItems.ANCIENT_FRUIT_JUICE_BUCKET);
    }

    public static void register(IEventBus bus){
        KSFluidTypes.FLUID_TYPES.register(bus);
        FLUIDS.register(bus);
    }
}
