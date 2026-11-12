package com.tequilacat.kaleidoscope_startale.init;

import com.tequilacat.kaleidoscope_startale.KaleidoscopeStartale;
import com.tequilacat.kaleidoscope_startale.blockentity.CreamOilBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.bus.api.IEventBus;

public interface KSBlockEntities {
    DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, KaleidoscopeStartale.MODID);

    /** 奶油壶方块实体 */
    DeferredHolder<BlockEntityType<?>, BlockEntityType<CreamOilBlockEntity>> CREAM_OIL =
            BLOCK_ENTITIES.register("cream_oil", () -> BlockEntityType.Builder
                    .of(CreamOilBlockEntity::new, KSBlocks.CREAM_OIL.get())
                    .build(null));

    static void register(IEventBus bus){
        BLOCK_ENTITIES.register(bus);
    }
}
