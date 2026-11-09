package com.tequilacat.kaleidoscope_startale.event.effect;

import com.tequilacat.kaleidoscope_startale.KaleidoscopeStartale;
import com.tequilacat.kaleidoscope_startale.init.KSEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * 祝尼魔的祝福事件类。
 * <p>
 * 祝尼魔的祝福（单级效果，不区分等级）：
 * - 破坏成熟作物时按 50% 概率额外掉一份产物；
 * - 手持镰刀或锄类农具时，连带收割周围 3×3 内的成熟作物；
 * - 玩家 4 格范围内的掉落物自动拾取。
 */
@EventBusSubscriber(modid = KaleidoscopeStartale.MODID)
public final class JunimoBlessingEvents {

    /** 额外掉落概率。 */
    private static final float EXTRA_DROP_CHANCE = 0.5F;

    /** 范围收割半径（以方块计）。 */
    private static final int SICKLE_RADIUS = 1;

    /** 自动拾取半径（以格计）。 */
    private static final double PICKUP_RADIUS = 4.0D;

    /** 范围收割时避免 BlockDropsEvent 递归触发自身。 */
    private static boolean harvestingAround;

    private JunimoBlessingEvents() {
    }

    /**
     * 处理方块掉落事件。
     * <p>
     * 若破坏者为拥有祝福效果的玩家且破坏的是成熟作物，
     * 则按概率追加额外掉落；若工具为镰刀或锄类，则连带收割周围作物。
     *
     * @param event 方块掉落事件
     */
    @SubscribeEvent
    public static void onBlockDrops(BlockDropsEvent event) {
        if (!(event.getBreaker() instanceof Player player)) {
            return;
        }
        if (harvestingAround || !player.hasEffect(KSEffects.JUNIMO_BLESSING)) {
            return;
        }
        if (!isMatureCrop(event.getState())) {
            return;
        }

        ServerLevel level = event.getLevel();
        BlockPos pos = event.getPos();
        addExtraDrops(event, level, pos);

        if (isSickleLike(event.getTool())) {
            harvestAround(level, pos, player);
        }
    }

    /**
     * 处理掉落物每刻更新事件。
     * <p>
     * 掉落物 4 格内的玩家若带祝福，则自动把物品吸过去。
     *
     * @param event 实体每刻更新事件
     */
    @SubscribeEvent
    public static void onItemTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof ItemEntity item)) {
            return;
        }
        if (item.level().isClientSide() || item.isRemoved() || item.hasPickUpDelay() || item.getItem().isEmpty()) {
            return;
        }

        Player nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (Player player : item.level().players()) {
            if (!player.hasEffect(KSEffects.JUNIMO_BLESSING) || !player.isAlive() || player.isSpectator()) {
                continue;
            }
            double distance = player.distanceToSqr(item);
            if (distance <= PICKUP_RADIUS * PICKUP_RADIUS && distance < nearestDistance) {
                nearestDistance = distance;
                nearest = player;
            }
        }

        if (nearest != null) {
            item.playerTouch(nearest);
        }
    }

    /**
     * 追加额外掉落。
     * <p>
     * 按概率复制当前所有非空掉落物并重新弹出到世界中。
     *
     * @param event 方块掉落事件
     * @param level 服务端世界
     * @param pos   方块位置
     */
    private static void addExtraDrops(BlockDropsEvent event, ServerLevel level, BlockPos pos) {
        if (level.getRandom().nextFloat() >= EXTRA_DROP_CHANCE) {
            return;
        }

        List<ItemStack> extras = new ArrayList<>();
        for (ItemEntity drop : event.getDrops()) {
            ItemStack stack = drop.getItem();
            if (!stack.isEmpty()) {
                extras.add(stack.copy());
            }
        }
        for (ItemStack stack : extras) {
            Block.popResource(level, pos, stack);
        }
    }

    /**
     * 范围收割周围成熟作物。
     * <p>
     * 遍历以原点为中心的立方体范围内的成熟作物并破坏掉落，
     * 通过标记避免递归触发本事件逻辑。
     *
     * @param level  服务端世界
     * @param origin 起始位置
     * @param player 玩家
     */
    private static void harvestAround(ServerLevel level, BlockPos origin, Player player) {
        if (harvestingAround) {
            return;
        }

        harvestingAround = true;
        try {
            for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-SICKLE_RADIUS, -SICKLE_RADIUS, -SICKLE_RADIUS),
                    origin.offset(SICKLE_RADIUS, SICKLE_RADIUS, SICKLE_RADIUS))) {
                if (pos.equals(origin)) {
                    continue;
                }
                if (!isMatureCrop(level.getBlockState(pos))) {
                    continue;
                }
                level.destroyBlock(pos.immutable(), true, player);
            }
        } finally {
            harvestingAround = false;
        }
    }

    /**
     * 判断方块状态是否为成熟作物。
     *
     * @param state 方块状态
     * @return 是否为成熟作物
     */
    private static boolean isMatureCrop(BlockState state) {
        return state.getBlock() instanceof CropBlock crop && crop.getAge(state) >= crop.getMaxAge();
    }

    /**
     * 判断工具是否为镰刀或锄类农具。
     * <p>
     * 锄类通过物品标签判定，镰刀类通过物品注册名包含 sickle 或 scythe 判定。
     *
     * @param tool 工具物品堆叠
     * @return 是否为镰刀类工具
     */
    private static boolean isSickleLike(ItemStack tool) {
        if (tool.isEmpty()) {
            return false;
        }
        if (tool.is(ItemTags.HOES)) {
            return true;
        }
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(tool.getItem());
        String path = id.getPath();
        return path.contains("sickle") || path.contains("scythe");
    }
}