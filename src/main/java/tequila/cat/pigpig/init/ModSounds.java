package tequila.cat.pigpig.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import tequila.cat.pigpig.KaleidoscopeStartale;

public final class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(Registries.SOUND_EVENT, KaleidoscopeStartale.MODID);

    public static final DeferredHolder<SoundEvent, SoundEvent> JUICER_WORKING =
            SOUNDS.register("juicer_working", () -> SoundEvent.createVariableRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(KaleidoscopeStartale.MODID, "juicer_working")));
    public static final DeferredHolder<SoundEvent, SoundEvent> PET_SQUEEZE =
            SOUNDS.register("pet_squeeze", () -> SoundEvent.createVariableRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(KaleidoscopeStartale.MODID, "pet_squeeze")));

    private ModSounds() {
    }
}
