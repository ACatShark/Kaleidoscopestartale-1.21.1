package com.tequilacat.kaleidoscope_startale.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.level.block.Block;

/**
 * 种子物品类。
 * <p>
 * 继承带名称的方块物品，用于表示可种植的种子，
 * 放置时生成对应作物方块。
 */
public class SeedItem extends ItemNameBlockItem {

    /**
     * 构造种子物品。
     *
     * @param block      关联方块
     * @param properties 物品属性
     */
    public SeedItem(Block block, Item.Properties properties) {
        super(block, properties);
    }
}