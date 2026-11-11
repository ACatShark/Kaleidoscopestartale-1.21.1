package com.tequilacat.kaleidoscope_startale.init;

import com.tequilacat.kaleidoscope_startale.KaleidoscopeStartale;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public interface KSSounds {
    DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, KaleidoscopeStartale.MODID);

    DeferredHolder<SoundEvent, SoundEvent> PET_SQUEEZE = SOUNDS.register("pet_squeeze",
            () -> SoundEvent.createVariableRangeEvent(KaleidoscopeStartale.id("pet_squeeze")));

    static void register(IEventBus bus){
        SOUNDS.register(bus);
    }
}
