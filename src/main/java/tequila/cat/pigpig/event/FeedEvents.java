package tequila.cat.pigpig.event;

import java.util.Set;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.armadillo.Armadillo;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import tequila.cat.pigpig.ai.FeedTemptGoal;
import tequila.cat.pigpig.attachment.ModAttachments;
import tequila.cat.pigpig.attachment.TameData;
import tequila.cat.pigpig.data.FavorabilityTracker;
import tequila.cat.pigpig.data.FeedRegistry;
import tequila.cat.pigpig.data.MobTaming;
import tequila.cat.pigpig.data.PetterCooldown;
import tequila.cat.pigpig.data.TamingCompat;

/**
 * 喂食：
 *  - 幼崽：直接加速成长（并缩短产蛋 / 脱甲计时）；
 *  - 已驯养：回血 / 缩短拍拍冷却 / 加好感度；
 *  - 未驯养：1/3 概率驯养成功，并绑定主人。
 * 另外给所有可喂的生物动态挂上「被饲料吸引」的 AI。
 */
public class FeedEvents {
    private static final int TEMPT_PRIORITY = 3;
    private static final double TEMPT_SPEED = 1.25F;
    private static final int TAME_CHANCE = 3;
    private static final float TAME_HEAL = 2.0F;

    @SubscribeEvent
    public void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof Animal animal)) {
            return;
        }
        ItemStack stack = event.getItemStack();
        boolean ourFeed = FeedRegistry.canFeed(stack.getItem(), animal);
        // 生物自带独立的模组驯养体系时，通用饲料不去驯服它；拍拍器与好感度仍照常工作
        if (ourFeed && FeedRegistry.isUniversalFeed(stack.getItem()) && TamingCompat.hasForeignTaming(animal)) {
            return;
        }
        if (!ourFeed) {
            // 生物自身 isFood 认可的食物：不拦截，繁殖/成长仍交给原版，只给已驯养的加好感度
            if (animal.isFood(stack) && !event.getLevel().isClientSide()
                    && MobTaming.isTamed(animal)
                    && (animal.isBaby() || animal.canFallInLove())) {
                FavorabilityTracker.onFed(animal);
            }
            return;
        }

        Player player = event.getEntity();
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);

        if (event.getLevel().isClientSide()) {
            return;
        }

        if (animal.isBaby()) {
            stack.consume(1, player);
            animal.ageUp(AgeableMob.getSpeedUpSecondsWhenFeeding(-animal.getAge()), true);
            shortenProduction(animal);
            if (MobTaming.isTamed(animal)) {
                FavorabilityTracker.onFed(animal);
            }
            return;
        }

        // 必须用 MobTaming：原版骨头驯服的狼/猫、驯服的马等没有 TAME 附件，
        // 只查附件会把它们当成未驯养，进而走 tryToTame 把好感度重置成 0。
        boolean wasTamed = MobTaming.isTamed(animal);
        boolean consumed;
        if (wasTamed) {
            consumed = feedTamed(animal, player, stack);
        } else {
            tryToTame(animal, player, stack);
            consumed = true;
        }

        boolean shortened = shortenProduction(animal);
        if (!consumed && shortened) {
            stack.consume(1, player);
            consumed = true;
        }

        if (wasTamed && consumed) {
            FavorabilityTracker.onFed(animal);
        }
    }

    private static boolean feedTamed(Animal animal, Player player, ItemStack stack) {
        if (animal.getHealth() < animal.getMaxHealth()) {
            animal.heal(TAME_HEAL);
            stack.consume(1, player);
            animal.gameEvent(GameEvent.EAT);
            return true;
        }
        if (PetterCooldown.shortenByFeeding(animal)) {
            stack.consume(1, player);
            return true;
        }
        return false;
    }

    /** 缩小产蛋 / 脱甲计时（Armadillo#scuteTime 由 access transformer 打开）。 */
    private static boolean shortenProduction(Animal animal) {
        if (animal instanceof Armadillo armadillo) {
            int next = shortenedTimer(armadillo.scuteTime);
            if (next < 0) {
                return false;
            }
            armadillo.scuteTime = next;
            return true;
        }
        if (animal instanceof Chicken chicken) {
            int next = shortenedTimer(chicken.eggTime);
            if (next < 0) {
                return false;
            }
            chicken.eggTime = next;
            return true;
        }
        return false;
    }

    private static int shortenedTimer(int remaining) {
        int reduction = AgeableMob.getSpeedUpSecondsWhenFeeding(remaining) * 20;
        return reduction > 0 ? Math.max(0, remaining - reduction) : -1;
    }

    private static void tryToTame(Animal animal, Player player, ItemStack stack) {
        stack.consume(1, player);

        boolean tamed = animal.getRandom().nextInt(TAME_CHANCE) == 0 && !EventHooks.onAnimalTame(animal, player);
        if (tamed) {
            animal.setData(ModAttachments.TAME.get(), new TameData(player.getUUID(), player.getName().getString()));
            animal.setPersistenceRequired();
            animal.getNavigation().stop();
            animal.setTarget((LivingEntity) null);
            if (animal.level() instanceof ServerLevel serverLevel) {
                // 只在确实没有好感度数据时初始化，绝不能覆盖已有进度
                FavorabilityTracker.ensureInitialized(serverLevel, animal);
            }
        }

        tamingParticles(animal, tamed);
    }

    private static void tamingParticles(Animal animal, boolean tamed) {
        animal.level().broadcastEntityEvent(animal, (byte) (tamed ? 7 : 6));
        if (animal instanceof TamableAnimal) {
            return;
        }
        if (animal.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(tamed ? ParticleTypes.HEART : ParticleTypes.SMOKE,
                    animal.getX(), animal.getY() + (double) animal.getBbHeight() * 0.6D, animal.getZ(),
                    7, 0.3D, 0.3D, 0.3D, 0.02D);
        }
    }

    @SubscribeEvent
    public void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) {
            return;
        }
        if (!(event.getEntity() instanceof Animal animal)) {
            return;
        }

        Set<Item> feeds = FeedRegistry.feedsFor(animal);
        if (feeds.isEmpty()) {
            return;
        }

        boolean alreadyInjected = animal.goalSelector.getAvailableGoals().stream()
                .anyMatch(wrapped -> wrapped.getGoal() instanceof FeedTemptGoal);
        if (!alreadyInjected) {
            animal.goalSelector.addGoal(TEMPT_PRIORITY, new FeedTemptGoal(animal, TEMPT_SPEED, feeds));
        }
    }
}
