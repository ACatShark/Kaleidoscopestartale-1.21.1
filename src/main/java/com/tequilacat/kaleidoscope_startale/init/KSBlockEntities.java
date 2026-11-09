package com.tequilacat.kaleidoscope_startale.init;

import com.tequilacat.kaleidoscope_startale.KaleidoscopeStartale;
import com.tequilacat.kaleidoscope_startale.blockentity.JuicerBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.bus.api.IEventBus;

public class KSBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, KaleidoscopeStartale.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<JuicerBlockEntity>> JUICER =
            BLOCK_ENTITY_TYPES.register("juicer", () -> BlockEntityType.Builder
                    .of(JuicerBlockEntity::new, KSBlocks.JUICER.get())
                    .build(null));

    public static void register(IEventBus bus){
        BLOCK_ENTITY_TYPES.register(bus);
    }
}
