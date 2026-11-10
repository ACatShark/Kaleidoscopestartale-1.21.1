package com.tequilacat.kaleidoscope_startale.init;

import com.tequilacat.kaleidoscope_startale.KaleidoscopeStartale;
import com.tequilacat.kaleidoscope_startale.blockentity.CreamOilBlockEntity;
import com.tequilacat.kaleidoscope_startale.blockentity.JuicerBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.bus.api.IEventBus;

public interface KSBlockEntities {
    DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, KaleidoscopeStartale.MODID);

    DeferredHolder<BlockEntityType<?>, BlockEntityType<JuicerBlockEntity>> JUICER =
            BLOCK_ENTITIES.register("juicer", () -> BlockEntityType.Builder
                    .of(JuicerBlockEntity::new, KSBlocks.JUICER.get())
                    .build(null));

    DeferredHolder<BlockEntityType<?>, BlockEntityType<CreamOilBlockEntity>> CREAM_OIL =
            BLOCK_ENTITIES.register("cream_oil", () -> BlockEntityType.Builder
                    .of(CreamOilBlockEntity::new, KSBlocks.CREAM_OIL.get())
                    .build(null));

    static void register(IEventBus bus){
        BLOCK_ENTITIES.register(bus);
    }
}
