package com.tequilacat.kaleidoscope_startale.init;

import com.github.ysbbbbbb.kaleidoscopetavern.item.BottleBlockItem;
import com.tequilacat.kaleidoscope_startale.KaleidoscopeStartale;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class KSCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, KaleidoscopeStartale.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> STARTALE_TAB =
            CREATIVE_MODE_TABS.register("startale_tab", () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(KSItems.ANCIENT_FRUIT.get()))
                    .title(Component.translatable("itemGroup." + KaleidoscopeStartale.MODID))
                    .displayItems((parameters, output) -> {
                        output.accept(KSItems.RHUBARB.get());
                        output.accept(KSItems.AMARANTH.get());
                        output.accept(KSItems.ANCIENT_FRUIT.get());
                        output.accept(KSItems.GOLDFISH.get());
                        output.accept(KSItems.RHUBARB_SEEDS.get());
                        output.accept(KSItems.AMARANTH_SEEDS.get());
                        output.accept(KSItems.ANCIENT_FRUIT_VINE.get());
                        output.accept(KSItems.RHUBARB_JAM.get());
                        output.accept(KSItems.ANCIENT_FRUIT_PLATTER.get());
                        output.accept(KSItems.AMARANTH_SOUP.get());
                        output.accept(KSItems.FISH_MUSHROOM_SOUP.get());
                        output.accept(KSItems.GLOWING_CATERPILLAR_SOUP.get());
                        output.accept(KSItems.ANCIENT_FRUIT_JUICE_BUCKET.get());
                        output.accept(KSItems.RHUBARB_FONDANT_PIE.get());
                        output.accept(KSItems.OMELETTE_RICE.get());
                        output.accept(KSItems.ANCIENT_FRUIT_SKEWER.get());
                        output.accept(KSItems.EGG_BAKED_CAKE.get());
                        output.accept(KSItems.IDEA_COOKIE.get());
                        output.accept(KSItems.INSPIRATION_MUSHROOM.get());
                        output.accept(KSItems.SLIME_KING_FLAVORED_JELLY.get());
                        output.accept(brewed(KSItems.ANCIENT_FRUIT_WINE.get()));
                        output.accept(brewed(KSItems.REFRESHING_BEER.get()));
                        output.accept(brewed(KSItems.YANJING_U7.get()));
                        output.accept(brewed(KSItems.GRAPE_FLAVORED_WINE.get()));
                        output.accept(brewed(KSItems.JOJA_COLA.get()));
                        output.accept(brewed(KSItems.RED_EYE_WINE.get()));
                        output.accept(KSItems.ICE_ELF_COCKTAIL.get());
                        output.accept(KSItems.BUBBLE_ECHO_COCKTAIL.get());
                        output.accept(KSItems.SPRING_DRINK_COCKTAIL.get());
                        output.accept(KSItems.SPRING_DRINK_1_COCKTAIL.get());
                        output.accept(KSItems.BERRY_JUICE.get());
                        output.accept(KSItems.GLOW_BERRY_JUICE.get());
                        output.accept(KSItems.GRAPE_JUICE.get());
                        output.accept(KSItems.GOLD_GRAPE_JUICE.get());
                        output.accept(KSItems.ICE_GRAPE_JUICE.get());
                        output.accept(KSItems.GREEN_GRAPE_JUICE.get());
                        output.accept(KSItems.ENCHANTED_NIGHTCRAWLER.get());
                        output.accept(KSItems.UNIVERSAL_FEED.get());
                        output.accept(KSItems.LIVESTOCK_FEED.get());
                        output.accept(KSItems.POULTRY_FEED.get());
                        output.accept(KSItems.PETTER.get());
                        output.accept(KSItems.JUICER.get());
                    })
                    .build());

    private static ItemStack brewed(Item item) {
        if (item instanceof BottleBlockItem bottle) {
            return bottle.getFilledStack(6);
        }
        return new ItemStack(item);
    }

    public static void register(IEventBus bus){
        CREATIVE_MODE_TABS.register(bus);
    }
}
