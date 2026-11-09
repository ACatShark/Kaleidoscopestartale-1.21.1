package com.tequilacat.kaleidoscope_startale.compat.jade;

import com.tequilacat.kaleidoscope_startale.compat.jade.entity.*;
import net.minecraft.world.entity.animal.Animal;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

/**
 * 本模组的 Jade 插件。
 * <p>
 * 为动物注册三个信息提供器：驯养主人、拍击冷却与好感度，
 * 分别在服务端数据注册与客户端组件注册中挂载。
 */
@WailaPlugin("kaleidoscope_startale")
public class StartaleJadePlugin implements IWailaPlugin {

    /**
     * 注册服务端数据提供器。
     * <p>
     * 为动物挂载驯养主人、拍击冷却与好感度的服务端数据提供器。
     *
     * @param registration 通用注册对象
     */
    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerEntityDataProvider(TameOwnerProvider.INSTANCE, Animal.class);
        registration.registerEntityDataProvider(PetterCooldownProvider.INSTANCE, Animal.class);
        registration.registerEntityDataProvider(FavorabilityProvider.INSTANCE, Animal.class);
    }

    /**
     * 注册客户端组件提供器。
     * <p>
     * 为动物挂载驯养主人、拍击冷却与好感度的客户端提示组件。
     *
     * @param registration 客户端注册对象
     */
    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerEntityComponent(TameOwnerProvider.INSTANCE, Animal.class);
        registration.registerEntityComponent(PetterCooldownProvider.INSTANCE, Animal.class);
        registration.registerEntityComponent(FavorabilityProvider.INSTANCE, Animal.class);
    }
}