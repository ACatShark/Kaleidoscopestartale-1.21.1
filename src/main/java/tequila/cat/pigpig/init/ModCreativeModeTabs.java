package tequila.cat.pigpig.init;

import com.github.ysbbbbbb.kaleidoscopetavern.item.BottleBlockItem;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import tequila.cat.pigpig.KaleidoscopeStartale;

public final class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, KaleidoscopeStartale.MODID);

    /** 创造模式里展示的酒水层数：直接给满级酿造，方便试饮。 */
    private static final int CREATIVE_BREW_LEVEL = 6;

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> STARTALE_TAB =
            CREATIVE_MODE_TABS.register("startale_tab", () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModItems.ANCIENT_FRUIT.get()))
                    .title(Component.translatable("itemGroup." + KaleidoscopeStartale.MODID))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.RHUBARB.get());
                        output.accept(ModItems.AMARANTH.get());
                        output.accept(ModItems.ANCIENT_FRUIT.get());
                        output.accept(ModItems.GOLDFISH.get());
                        output.accept(ModItems.RHUBARB_SEEDS.get());
                        output.accept(ModItems.AMARANTH_SEEDS.get());
                        output.accept(ModItems.ANCIENT_FRUIT_VINE.get());
                        output.accept(ModItems.RHUBARB_JAM.get());
                        output.accept(ModItems.ANCIENT_FRUIT_PLATTER.get());
                        output.accept(ModItems.AMARANTH_SOUP.get());
                        output.accept(ModItems.FISH_MUSHROOM_SOUP.get());
                        output.accept(ModItems.GLOWING_CATERPILLAR_SOUP.get());
                        output.accept(ModItems.ANCIENT_FRUIT_JUICE_BUCKET.get());
                        output.accept(ModItems.RHUBARB_FONDANT_PIE.get());
                        output.accept(ModItems.OMELETTE_RICE.get());
                        output.accept(ModItems.ANCIENT_FRUIT_SKEWER.get());
                        output.accept(ModItems.EGG_BAKED_CAKE.get());
                        output.accept(ModItems.IDEA_COOKIE.get());
                        output.accept(ModItems.INSPIRATION_MUSHROOM.get());
                        output.accept(ModItems.SLIME_KING_FLAVORED_JELLY.get());
                        output.accept(brewed(ModItems.ANCIENT_FRUIT_WINE.get()));
                        output.accept(brewed(ModItems.REFRESHING_BEER.get()));
                        output.accept(brewed(ModItems.YANJING_U7.get()));
                        output.accept(brewed(ModItems.GRAPE_FLAVORED_WINE.get()));
                        output.accept(brewed(ModItems.JOJA_COLA.get()));
                        output.accept(brewed(ModItems.RED_EYE_WINE.get()));
                        output.accept(ModItems.ICE_ELF_COCKTAIL.get());
                        output.accept(ModItems.BUBBLE_ECHO_COCKTAIL.get());
                        output.accept(ModItems.SPRING_DRINK_COCKTAIL.get());
                        output.accept(ModItems.SPRING_DRINK_1_COCKTAIL.get());
                        output.accept(ModItems.BERRY_JUICE.get());
                        output.accept(ModItems.GLOW_BERRY_JUICE.get());
                        output.accept(ModItems.GRAPE_JUICE.get());
                        output.accept(ModItems.GOLD_GRAPE_JUICE.get());
                        output.accept(ModItems.ICE_GRAPE_JUICE.get());
                        output.accept(ModItems.GREEN_GRAPE_JUICE.get());
                        output.accept(ModItems.ENCHANTED_NIGHTCRAWLER.get());
                        output.accept(ModItems.UNIVERSAL_FEED.get());
                        output.accept(ModItems.LIVESTOCK_FEED.get());
                        output.accept(ModItems.POULTRY_FEED.get());
                        output.accept(ModItems.PETTER.get());
                        output.accept(ModItems.JUICER.get());
                    })
                    .build());

    private static ItemStack brewed(Item item) {
        if (item instanceof BottleBlockItem bottle) {
            return bottle.getFilledStack(CREATIVE_BREW_LEVEL);
        }
        return new ItemStack(item);
    }

    private ModCreativeModeTabs() {
    }
}
