package tequila.cat.pigpig.item;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraft.world.entity.animal.sniffer.Sniffer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import net.neoforged.neoforge.common.Tags;

import tequila.cat.pigpig.KaleidoscopeStartale;
import tequila.cat.pigpig.data.FavorabilityTracker;
import tequila.cat.pigpig.data.MobTaming;
import tequila.cat.pigpig.data.PetterCooldown;
import tequila.cat.pigpig.data.PetterSqueeze;
import tequila.cat.pigpig.init.ModEffects;
import tequila.cat.pigpig.init.ModSounds;

/**
 * 拍拍器：对已驯养且不在冷却中的生物右键，按该生物的掉落表「挤」出产物。
 * 抢夺等级与好感度等级都会提高额外掉落的概率；
 * 持有「牧场英雄」效果时额外掉落更多、拍击幅度更大。
 */
public class PetterItem extends Item {
    private static final float LOOTING_DROP_CHANCE_PER_LEVEL = 0.2F;
    private static final float FAVOR_DROP_CHANCE_PER_LEVEL = 0.05F;
    /** 牧场英雄：额外掉落概率 +35%。 */
    private static final float RANCH_HERO_DROP_CHANCE_BONUS = 0.35F;
    /** 牧场英雄：额外滚一次掉落表。 */
    private static final int RANCH_HERO_EXTRA_ROLLS = 1;
    /** 牧场英雄：拍击幅度（压扁力度）加成。 */
    private static final int RANCH_HERO_SQUEEZE_POWER = 2;
    /** 骆驼掉落表的选项数量，均匀分布即每项 20%。 */
    private static final int CAMEL_GIFT_OPTIONS = 5;

    public PetterItem(Item.Properties properties) {
        super(properties);
    }

    public static boolean isTamed(LivingEntity entity) {
        return MobTaming.isTamed(entity);
    }

    /**
     * 拍一次已驯养的生物。
     *
     * @return 本次实际挤出的物品总数（0 表示没有产出），调用方按这个数值扣耐久
     */
    public int pet(Player player, LivingEntity target, ItemStack petterStack) {
        if (!(target.level() instanceof ServerLevel level)) {
            return 0;
        }

        int dropped = 0;
        boolean ranchHero = player.hasEffect(ModEffects.RANCH_HERO);
        if (MobTaming.isTamed(target) && !PetterCooldown.isCooling(target)) {
            List<ItemStack> drops = new ArrayList<>(rollLoot(player, target, level));
            if (!drops.isEmpty()) {
                int looting = getLootingLevel(level, petterStack);
                int favorLevel = favorLevelOf(target);
                float extraChance = LOOTING_DROP_CHANCE_PER_LEVEL * looting
                        + FAVOR_DROP_CHANCE_PER_LEVEL * Math.max(0, favorLevel - 1)
                        + (ranchHero ? RANCH_HERO_DROP_CHANCE_BONUS : 0.0F);
                int extraRolls = 0;
                if (level.random.nextFloat() < Math.min(1.0F, extraChance)) {
                    extraRolls++;
                }
                // 牧场英雄：必定再滚一次掉落表
                if (ranchHero) {
                    extraRolls += RANCH_HERO_EXTRA_ROLLS;
                }
                for (int i = 0; i < extraRolls; i++) {
                    drops.addAll(rollLoot(player, target, level));
                }
            }

            dropped = countItems(drops);
            if (dropped > 0) {
                for (ItemStack drop : drops) {
                    Block.popResource(level, target.blockPosition(), drop);
                }

                PetterCooldown.reset(target);
                FavorabilityTracker.onPetted(target);
            }
        }

        playSqueeze(target, level, petterStack, ranchHero ? RANCH_HERO_SQUEEZE_POWER : 0);
        if (dropped > 0) {
            spawnHearts(target, level);
        }
        return dropped;
    }

    private static int countItems(List<ItemStack> drops) {
        int total = 0;
        for (ItemStack stack : drops) {
            total += stack.getCount();
        }
        return total;
    }

    private static int favorLevelOf(LivingEntity target) {
        return FavorabilityTracker.hasData(target) ? FavorabilityTracker.get(target).level() : 1;
    }

    private static int getSharpnessLevel(ServerLevel level, ItemStack weapon) {
        return level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
                .get(Enchantments.SHARPNESS)
                .map(sharpness -> EnchantmentHelper.getItemEnchantmentLevel(sharpness, weapon))
                .orElse(0);
    }

    private static int getLootingLevel(ServerLevel level, ItemStack weapon) {
        return level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
                .get(Enchantments.LOOTING)
                .map(looting -> EnchantmentHelper.getItemEnchantmentLevel(looting, weapon))
                .orElse(0);
    }

    private static void spawnHearts(LivingEntity target, ServerLevel level) {
        double x = target.getX();
        double y = target.getY() + (double) target.getBbHeight() * 0.6D;
        double z = target.getZ();
        level.sendParticles(ParticleTypes.HEART, x, y, z, 20, 0.15D, 0.2D, 0.15D, 0.0D);
        level.sendParticles(ParticleTypes.HEART, x, y, z, 12, 0.45D, 0.35D, 0.45D, 0.02D);
    }

    private static void playSqueeze(LivingEntity target, ServerLevel level, ItemStack petterStack, int powerBonus) {
        PetterSqueeze.start(target, getSharpnessLevel(level, petterStack) + powerBonus);
        level.playSound(null, target.blockPosition(), ModSounds.PET_SQUEEZE.get(), SoundSource.NEUTRAL, 1.0F, 1.0F);
    }

    private static List<ItemStack> rollLoot(Player player, LivingEntity pet, ServerLevel level) {
        if (pet instanceof Camel) {
            return randomCamelGift(level);
        }
        if (pet instanceof Cat) {
            LootParams params = new LootParams.Builder(level)
                    .withParameter(LootContextParams.THIS_ENTITY, pet)
                    .withParameter(LootContextParams.ORIGIN, pet.position())
                    .create(LootContextParamSets.GIFT);
            return randomItems(level, BuiltInLootTables.CAT_MORNING_GIFT, params);
        }
        if (pet instanceof Wolf) {
            return randomRawMeat(level);
        }

        List<ItemStack> drops = new ArrayList<>(rollEntityLoot(player, pet, level));
        if (pet instanceof Sniffer) {
            // 嗅探兽：挖掘表 + 击杀表各滚一次
            drops.addAll(rollDiggingLoot(pet, level));
        }
        return drops;
    }

    private static List<ItemStack> rollEntityLoot(Player player, LivingEntity pet, ServerLevel level) {
        ResourceKey<LootTable> lootTable = pet.getLootTable();
        if (lootTable == null) {
            return List.of();
        }
        LootParams params = new LootParams.Builder(level)
                .withParameter(LootContextParams.THIS_ENTITY, pet)
                .withParameter(LootContextParams.ORIGIN, pet.position())
                .withParameter(LootContextParams.DAMAGE_SOURCE, level.damageSources().playerAttack(player))
                .withParameter(LootContextParams.ATTACKING_ENTITY, player)
                .withParameter(LootContextParams.DIRECT_ATTACKING_ENTITY, player)
                .withParameter(LootContextParams.LAST_DAMAGE_PLAYER, player)
                .withLuck(player.getLuck())
                .create(LootContextParamSets.ENTITY);
        return randomItems(level, lootTable, params);
    }

    /** 嗅探兽的挖掘掉落表（原版刨地用的那套）。 */
    private static List<ItemStack> rollDiggingLoot(LivingEntity pet, ServerLevel level) {
        LootParams params = new LootParams.Builder(level)
                .withParameter(LootContextParams.THIS_ENTITY, pet)
                .withParameter(LootContextParams.ORIGIN, pet.position())
                .create(LootContextParamSets.GIFT);
        return randomItems(level, BuiltInLootTables.SNIFFER_DIGGING, params);
    }

    /** 骆驼：发光地衣 / 草 / 蕨 / 仙人掌 / 随机药水，五项各 20%。 */
    private static List<ItemStack> randomCamelGift(ServerLevel level) {
        return switch (level.getRandom().nextInt(CAMEL_GIFT_OPTIONS)) {
            case 0 -> List.of(new ItemStack(Items.GLOW_LICHEN));
            case 1 -> List.of(new ItemStack(Items.SHORT_GRASS));
            case 2 -> List.of(new ItemStack(Items.FERN));
            case 3 -> List.of(new ItemStack(Items.CACTUS));
            default -> List.of(randomPotion(level));
        };
    }

    /** 从药水注册表里随机挑一瓶（不做过滤，水、粗制、平凡、浓稠也在池子里）。 */
    private static ItemStack randomPotion(ServerLevel level) {
        Optional<Holder.Reference<Potion>> pick = BuiltInRegistries.POTION.getRandom(level.getRandom());
        if (pick.isEmpty()) {
            return new ItemStack(Items.POTION);
        }

        ItemStack stack = new ItemStack(Items.POTION);
        stack.set(DataComponents.POTION_CONTENTS, new PotionContents(pick.get()));
        return stack;
    }

    private static List<ItemStack> randomRawMeat(ServerLevel level) {
        Optional<Holder<Item>> pick = BuiltInRegistries.ITEM.getRandomElementOf(Tags.Items.FOODS_RAW_MEAT, level.getRandom());
        if (pick.isEmpty()) {
            KaleidoscopeStartale.LOGGER.warn("[petter] 生肉标签 {} 为空，无法给狼掉落", Tags.Items.FOODS_RAW_MEAT.location());
            return List.of();
        }
        return List.of(new ItemStack(pick.get().value()));
    }

    private static List<ItemStack> randomItems(ServerLevel level, ResourceKey<LootTable> key, LootParams params) {
        LootTable table = level.getServer().reloadableRegistries().getLootTable(key);
        return List.copyOf(table.getRandomItems(params));
    }

    /** 铁砧修复材料：蓝色羊毛。 */
    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repairCandidate) {
        return repairCandidate.is(Items.BLUE_WOOL);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip.kaleidoscope_startale.petter").withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.translatable("tooltip.kaleidoscope_startale.petter.cooldown").withStyle(ChatFormatting.GRAY));
    }
}
