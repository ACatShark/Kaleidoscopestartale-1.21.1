package tequila.cat.pigpig.compat.jade;

import net.minecraft.world.entity.animal.Animal;

import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin("kaleidoscope_startale")
public class StartaleJadePlugin implements IWailaPlugin {
    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerEntityDataProvider(TameOwnerProvider.INSTANCE, Animal.class);
        registration.registerEntityDataProvider(PetterCooldownProvider.INSTANCE, Animal.class);
        registration.registerEntityDataProvider(FavorabilityProvider.INSTANCE, Animal.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerEntityComponent(TameOwnerProvider.INSTANCE, Animal.class);
        registration.registerEntityComponent(PetterCooldownProvider.INSTANCE, Animal.class);
        registration.registerEntityComponent(FavorabilityProvider.INSTANCE, Animal.class);
    }
}
