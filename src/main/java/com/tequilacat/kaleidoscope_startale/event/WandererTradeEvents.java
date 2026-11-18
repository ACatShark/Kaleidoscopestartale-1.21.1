package com.tequilacat.kaleidoscope_startale.event;

import com.tequilacat.kaleidoscope_startale.KaleidoscopeStartale;
import com.tequilacat.kaleidoscope_startale.config.KSServerConfig;
import com.tequilacat.kaleidoscope_startale.init.KSItems;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.BasicItemListing;
import net.neoforged.neoforge.event.village.WandererTradesEvent;

/**
 * 流浪商人交易事件类。
 * <p>
 * 向流浪商人的稀有交易列表中添加一项交易：
 * 以绿宝石换取远古果实藤蔓，价格、数量与使用次数均由配置项决定。
 */
@EventBusSubscriber(modid = KaleidoscopeStartale.MODID)
public class WandererTradeEvents {

    /** 交易给予的玩家经验。 */
    private static final int XP = 5;

    /** 价格浮动倍率。 */
    private static final float PRICE_MULTIPLIER = 0.05F;

    /**
     * 处理流浪商人交易事件。
     * <p>
     * 读取配置中的绿宝石花费、藤蔓数量与最大使用次数，
     * 向稀有交易列表添加对应的交易。
     *
     * @param event 流浪商人交易事件
     */
    @SubscribeEvent
    public static void onWandererTrades(WandererTradesEvent event) {
        event.getRareTrades().add(new BasicItemListing(
                KSServerConfig.WANDERER_VINE_EMERALD_COST.get(),
                new ItemStack(KSItems.ANCIENT_FRUIT_VINE.get(), KSServerConfig.WANDERER_VINE_COUNT.get()),
                KSServerConfig.WANDERER_VINE_MAX_USES.get(),
                XP,
                PRICE_MULTIPLIER));
    }
}