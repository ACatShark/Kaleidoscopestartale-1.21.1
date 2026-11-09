package tequila.cat.pigpig.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;

import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import tequila.cat.pigpig.KaleidoscopeStartale;
import tequila.cat.pigpig.block.entity.JuicerBlockEntity;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, KaleidoscopeStartale.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<JuicerBlockEntity>> JUICER =
            BLOCK_ENTITIES.register("juicer", () -> BlockEntityType.Builder
                    .of(JuicerBlockEntity::new, ModBlocks.JUICER.get())
                    .build(null));

    private ModBlockEntities() {
    }
}
