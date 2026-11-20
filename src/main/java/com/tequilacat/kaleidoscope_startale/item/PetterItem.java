package com.tequilacat.kaleidoscope_startale.item;

import com.tequilacat.kaleidoscope_startale.config.KSServerConfig;
import com.tequilacat.kaleidoscope_startale.data.favorability.FavorabilityTracker;
import com.tequilacat.kaleidoscope_startale.data.tame.MobTaming;
import com.tequilacat.kaleidoscope_startale.util.*;
import com.tequilacat.kaleidoscope_startale.init.*;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 拍拍器物品类。
 * <p>
 * 对已驯养且不在冷却中的生物右键，按该生物的掉落表「挤」出产物。
 * 抢夺等级、好感度等级与「牧场英雄」效果都会提高额外掉落概率与数量上限，
 * 并影响拍击幅度；产出成功后重置冷却并增加好感度。
 */
public class PetterItem extends Item {

    /**
     * 构造拍拍器物品。
     *
     * @param properties 物品属性
     */
    public PetterItem(Item.Properties properties) {
        super(properties);
    }

    /**
     * 判断实体是否已驯养。
     *
     * @param entity 目标实体
     * @return 是否已驯养
     */
    public static boolean isTamed(LivingEntity entity) {
        return MobTaming.isTamed(entity);
    }

    /**
     * 拍一次已驯养的生物。
     * <p>
     * 若目标已驯养且不在冷却中，则根据掉落表产出物品并按概率追加额外掉落，
     * 产出成功后重置冷却并增加好感度，同时播放拍击音效与爱心粒子。
     *
     * @param player      玩家
     * @param target      目标生物
     * @param petterStack 拍拍器物品堆叠
     * @return 本次实际挤出的物品总数（0 表示没有产出），调用方按这个数值扣耐久
     */
    public int pet(Player player, LivingEntity target, ItemStack petterStack) {
        if (!(target.level() instanceof ServerLevel level)) {
            return 0;
        }

        int dropped = 0;
        boolean amaranthInsight = player.hasEffect(KSEffects.AMARANTH_INSIGHT);
        if (MobTaming.isTamed(target) && !PetterCooldown.isCooling(target)) {
            List<ItemStack> drops = new ArrayList<>(PetterLoot.roll(player, target, level));
            int looting = getLootingLevel(level, petterStack);
            int favorLevel = favorLevelOf(target);
            if (!drops.isEmpty()) {
                double extraChance = KSServerConfig.PETTER_LOOTING_DROP_CHANCE_PER_LEVEL.get() * looting
                        + KSServerConfig.PETTER_FAVOR_DROP_CHANCE_PER_LEVEL.get() * Math.max(0, favorLevel - 1);
                int extraRolls = 0;
                if (level.random.nextFloat() < Math.min(1.0D, extraChance)) {
                    extraRolls++;
                }
                for (int i = 0; i < extraRolls; i++) {
                    drops.addAll(PetterLoot.roll(player, target, level));
                }
            }

            // 按好感度等级、抢夺等级与苋见之明效果限制本次最多挤出的物品数量
            drops = limitDrops(drops, maxDropsAt(favorLevel, looting, amaranthInsight), level.random);

            dropped = countItems(drops);
            if (dropped > 0) {
                for (ItemStack drop : drops) {
                    Block.popResource(level, target.blockPosition(), drop);
                }

                PetterCooldown.reset(target);
                FavorabilityTracker.onPetted(target);
            }
        }

        playSqueeze(target, level, petterStack, amaranthInsight ? KSServerConfig.PETTER_AMARANTH_INSIGHT_SQUEEZE_POWER.get() : 0);
        if (dropped > 0) {
            spawnHearts(target, level);
        }
        return dropped;
    }

    /**
     * 计算指定好感度等级、抢夺等级与牧场英雄状态下的掉落数量上限。
     * <p>
     * 基础上限来自配置项，抢夺等级每级按配置增加上限；
     * 好感度达到配置中的各级阈值时各额外 +1；
     * 拥有苋见之明效果时再按配置额外提高上限。
     *
     * @param favorLevel 好感度等级
     * @param looting    抢夺等级
     * @param amaranthInsight 是否拥有苋见之明效果
     * @return 掉落数量上限
     */
    private static int maxDropsAt(int favorLevel, int looting, boolean amaranthInsight) {
        int cap = KSServerConfig.PETTER_BASE_DROP_CAP.get()
                + Math.max(0, looting) * KSServerConfig.PETTER_LOOTING_CAP_PER_LEVEL.get();
        for (Number level : KSServerConfig.PETTER_FAVOR_CAP_LEVELS.get()) {
            if (favorLevel >= level.intValue()) {
                cap++;
            }
        }
        if (amaranthInsight) {
            cap += KSServerConfig.PETTER_AMARANTH_INSIGHT_CAP_BONUS.get();
        }
        return cap;
    }

    /**
     * 按上限裁剪掉落物列表。
     * <p>
     * 先打乱顺序，避免总是偏向掉落表靠前的条目；
     * 再按顺序取物品，最后一个物品只保留上限内允许的数量。
     *
     * @param drops    掉落物列表
     * @param maxItems 物品数量上限
     * @param random   随机源
     * @return 裁剪后的掉落物列表
     */
    private static List<ItemStack> limitDrops(List<ItemStack> drops, int maxItems, RandomSource random) {
        if (maxItems <= 0 || drops.isEmpty()) {
            return List.of();
        }

        List<ItemStack> candidates = new ArrayList<>(drops);
        Util.shuffle(candidates, random);

        List<ItemStack> result = new ArrayList<>();
        int total = 0;
        for (ItemStack stack : candidates) {
            if (total >= maxItems) {
                break;
            }
            int allowed = Math.min(stack.getCount(), maxItems - total);
            if (allowed <= 0) {
                continue;
            }
            ItemStack copy = stack.copy();
            copy.setCount(allowed);
            result.add(copy);
            total += allowed;
        }
        return result;
    }

    /**
     * 统计掉落物总数。
     *
     * @param drops 掉落物列表
     * @return 物品总数
     */
    private static int countItems(List<ItemStack> drops) {
        int total = 0;
        for (ItemStack stack : drops) {
            total += stack.getCount();
        }
        return total;
    }

    /**
     * 获取目标的好感度等级。
     * <p>
     * 若目标没有好感度数据则返回 1。
     *
     * @param target 目标生物
     * @return 好感度等级
     */
    private static int favorLevelOf(LivingEntity target) {
        return FavorabilityTracker.hasData(target) ? FavorabilityTracker.get(target).level() : 1;
    }

    /**
     * 获取武器的锋利等级。
     *
     * @param level  服务端世界
     * @param weapon 武器物品堆叠
     * @return 锋利等级
     */
    private static int getSharpnessLevel(ServerLevel level, ItemStack weapon) {
        return level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
                .get(Enchantments.SHARPNESS)
                .map(sharpness -> EnchantmentHelper.getItemEnchantmentLevel(sharpness, weapon))
                .orElse(0);
    }

    /**
     * 获取武器的抢夺等级。
     *
     * @param level  服务端世界
     * @param weapon 武器物品堆叠
     * @return 抢夺等级
     */
    private static int getLootingLevel(ServerLevel level, ItemStack weapon) {
        return level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
                .get(Enchantments.LOOTING)
                .map(looting -> EnchantmentHelper.getItemEnchantmentLevel(looting, weapon))
                .orElse(0);
    }

    /**
     * 生成爱心粒子。
     * <p>
     * 在目标身体上方分两批生成爱心粒子，营造互动反馈。
     *
     * @param target 目标生物
     * @param level  服务端世界
     */
    private static void spawnHearts(LivingEntity target, ServerLevel level) {
        double x = target.getX();
        double y = target.getY() + (double) target.getBbHeight() * 0.6D;
        double z = target.getZ();
        level.sendParticles(ParticleTypes.HEART, x, y, z, 20, 0.15D, 0.2D, 0.15D, 0.0D);
        level.sendParticles(ParticleTypes.HEART, x, y, z, 12, 0.45D, 0.35D, 0.45D, 0.02D);
    }

    /**
     * 播放拍击效果。
     * <p>
     * 根据锋利等级与额外加成启动挤压形变，并播放拍击音效。
     *
     * @param target      目标生物
     * @param level       服务端世界
     * @param petterStack 拍拍器物品堆叠
     * @param powerBonus  额外拍击力度加成
     */
    private static void playSqueeze(LivingEntity target, ServerLevel level, ItemStack petterStack, int powerBonus) {
        PetterSqueeze.start(target, getSharpnessLevel(level, petterStack) + powerBonus);
        level.playSound(null, target.blockPosition(), KSSounds.PET_SQUEEZE.get(), SoundSource.NEUTRAL, 1.0F, 1.0F);
    }

    /**
     * 判断是否为有效修复材料。
     * <p>
     * 铁砧修复材料为蓝色羊毛。
     *
     * @param stack           待修复物品堆叠
     * @param repairCandidate 修复材料堆叠
     * @return 是否为有效修复材料
     */
    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repairCandidate) {
        return repairCandidate.is(Items.BLUE_WOOL);
    }

    /**
     * 追加物品提示文本。
     * <p>
     * 添加拍拍器说明与冷却提示，均使用灰色样式。
     *
     * @param stack             物品堆叠
     * @param context           提示上下文
     * @param tooltipComponents 提示组件列表
     * @param tooltipFlag       提示标志
     */
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip.kaleidoscope_startale.petter")
                .withStyle(ChatFormatting.GRAY));
//        tooltipComponents.add(dropCapTooltip().withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.translatable("tooltip.kaleidoscope_startale.petter.cooldown")
                .withStyle(ChatFormatting.GRAY));
    }

    /**
     * 按当前配置拼出掉落上限提示。
     * <p>
     * 基础上限、好感度触发等级与抢夺每级加成都会随配置变化，
     * 所以这里现读配置，避免提示与实际数值脱节。
     *
     * @return 掉落上限提示文本
     */
    private static MutableComponent dropCapTooltip() {
        String levels = KSServerConfig.PETTER_FAVOR_CAP_LEVELS.get().stream()
                .map(level -> Integer.toString(level.intValue()))
                .collect(Collectors.joining("/"));
        return Component.translatable("tooltip.kaleidoscope_startale.petter.limit",
                KSServerConfig.PETTER_BASE_DROP_CAP.get(),
                levels,
                KSServerConfig.PETTER_LOOTING_CAP_PER_LEVEL.get());
    }
}
