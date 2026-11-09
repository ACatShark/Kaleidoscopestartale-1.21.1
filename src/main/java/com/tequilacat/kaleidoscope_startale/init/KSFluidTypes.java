package com.tequilacat.kaleidoscope_startale.init;

import com.github.ysbbbbbb.kaleidoscopetavern.fluid.JuiceFluidType;
import com.tequilacat.kaleidoscope_startale.KaleidoscopeStartale;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class KSFluidTypes {
    public static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(NeoForgeRegistries.FLUID_TYPES, KaleidoscopeStartale.MODID);

    /** 上古水果汁 */
    public static final Supplier<FluidType> ANCIENT_FRUIT_JUICE_TYPE = FLUID_TYPES.register("ancient_fruit_juice",
            () -> new JuiceFluidType(KSFluids.ANCIENT_FRUIT_JUICE_ID, 0));
}
