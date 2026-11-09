package tequila.cat.pigpig.event;

import java.util.ArrayList;
import java.util.List;

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

import tequila.cat.pigpig.KaleidoscopeStartale;
import tequila.cat.pigpig.init.ModEffects;

/**
 * 祝尼魔的祝福（单级效果，不区分等级）：
 * - 破坏成熟作物时按 50% 概率额外掉一份产物；
 * - 手持镰刀或锄类农具时，连带收割周围 3×3 内的成熟作物；
 * - 玩家 4 格范围内的掉落物自动拾取。
 */
@EventBusSubscriber(modid = KaleidoscopeStartale.MODID, bus = EventBusSubscriber.Bus.GAME)
public final class JunimoBlessingEvents {
    private static final float EXTRA_DROP_CHANCE = 0.5F;
    private static final int SICKLE_RADIUS = 1;
    private static final double PICKUP_RADIUS = 4.0D;

    /** 范围收割时避免 BlockDropsEvent 递归触发自身。 */
    private static boolean harvestingAround;

    @SubscribeEvent
    public static void onBlockDrops(BlockDropsEvent event) {
        if (!(event.getBreaker() instanceof Player player)) {
            return;
        }
        if (harvestingAround || !player.hasEffect(ModEffects.JUNIMO_BLESSING)) {
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

    /** 掉落物 4 格内的玩家若带祝福，则自动把物品吸过去。 */
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
            if (!player.hasEffect(ModEffects.JUNIMO_BLESSING) || !player.isAlive() || player.isSpectator()) {
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

    private static boolean isMatureCrop(BlockState state) {
        return state.getBlock() instanceof CropBlock crop && crop.getAge(state) >= crop.getMaxAge();
    }

    /** 镰刀类判定：原版锄，或任意 id 含 sickle/scythe 的农具（如森罗物语：厨房的镰刀）。 */
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

    private JunimoBlessingEvents() {
    }
}
