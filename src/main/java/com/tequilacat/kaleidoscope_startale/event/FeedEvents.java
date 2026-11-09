package com.tequilacat.kaleidoscope_startale.event;

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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gameevent.GameEvent;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import com.tequilacat.kaleidoscope_startale.ai.FeedTemptGoal;
import com.tequilacat.kaleidoscope_startale.init.KSAttachments;
import com.tequilacat.kaleidoscope_startale.data.tame.TameData;
import com.tequilacat.kaleidoscope_startale.data.favorability.FavorabilityTracker;
import com.tequilacat.kaleidoscope_startale.util.FeedRegistry;
import com.tequilacat.kaleidoscope_startale.data.tame.MobTaming;
import com.tequilacat.kaleidoscope_startale.util.PetterCooldown;
import com.tequilacat.kaleidoscope_startale.data.tame.TamingCompat;

/**
 * 喂食事件类。
 * <p>
 * 处理玩家对动物喂食的交互：幼崽加速成长、已驯养动物治疗与缩短产出冷却、
 * 未驯养动物按概率驯服并记录主人；同时对生物注入通用饲料的诱惑 AI，
 * 并兼容原版与模组自带的驯养体系。
 */
public class FeedEvents {

    /** 诱惑 AI 的优先级。 */
    private static final int TEMPT_PRIORITY = 3;

    /** 诱惑 AI 的移动速度。 */
    private static final double TEMPT_SPEED = 1.25F;

    /** 驯服概率分母（1/N 概率驯服）。 */
    private static final int TAME_CHANCE = 3;

    /** 喂食已驯养动物的治疗量。 */
    private static final float TAME_HEAL = 2.0F;

    /**
     * 处理实体右键事件。
     * <p>
     * 通用饲料会拦截交互并执行喂食逻辑；生物自身认可的食物不拦截，
     * 仅对已驯养动物增加好感度。幼崽加速成长，已驯养动物治疗或缩短产出，
     * 未驯养动物按概率驯服。
     *
     * @param event 实体右键事件
     */
    @SubscribeEvent
    public void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof Animal animal)) {
            return;
        }
        ItemStack stack = event.getItemStack();
        boolean ourFeed = FeedRegistry.canFeed(stack, animal);
        // 生物自带独立的模组驯养体系时，通用饲料不去驯服它；拍拍器与好感度仍照常工作
        if (ourFeed && FeedRegistry.isUniversalFeed(stack) && TamingCompat.hasForeignTaming(animal)) {
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

    /**
     * 喂食已驯养的动物。
     * <p>
     * 动物受伤时治疗并消耗食物；否则尝试缩短拍击冷却并消耗食物。
     *
     * @param animal 动物
     * @param player 玩家
     * @param stack  食物物品堆叠
     * @return 是否消耗了食物
     */
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

    /**
     * 缩短产出计时。
     * <p>
     * 对犰狳缩短鳞甲计时、对鸡缩短下蛋计时。
     *
     * @param animal 动物
     * @return 是否成功缩短
     */
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

    /**
     * 计算缩短后的计时。
     * <p>
     * 根据喂食加速秒数换算为刻并减去，若无法缩短则返回 -1。
     *
     * @param remaining 剩余计时（刻）
     * @return 缩短后的计时，无法缩短时返回 -1
     */
    private static int shortenedTimer(int remaining) {
        int reduction = AgeableMob.getSpeedUpSecondsWhenFeeding(remaining) * 20;
        return reduction > 0 ? Math.max(0, remaining - reduction) : -1;
    }

    /**
     * 尝试驯服动物。
     * <p>
     * 消耗食物并按概率驯服，成功时记录主人数据、设为持久存在、
     * 停止移动与攻击目标，并在无好感度数据时初始化；最后播放驯服粒子。
     *
     * @param animal 动物
     * @param player 玩家
     * @param stack  食物物品堆叠
     */
    private static void tryToTame(Animal animal, Player player, ItemStack stack) {
        stack.consume(1, player);

        boolean tamed = animal.getRandom().nextInt(TAME_CHANCE) == 0 && !EventHooks.onAnimalTame(animal, player);
        if (tamed) {
            animal.setData(KSAttachments.TAME.get(), new TameData(player.getUUID(), player.getName().getString()));
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

    /**
     * 播放驯服粒子与实体事件。
     * <p>
     * 广播驯服成功或失败的实体事件；非原版可驯服动物额外生成爱心或烟雾粒子。
     *
     * @param animal 动物
     * @param tamed  是否驯服成功
     */
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

    /**
     * 处理实体加入世界事件。
     * <p>
     * 客户端与服务端都注入，保证两端 goal 集合一致（原版诱惑 AI 也是构造期加入的双端逻辑）。
     * 饲料集合由目标自行按标签代次刷新，所以这里不需要判断当前是否有饲料。
     *
     * @param event 实体加入世界事件
     */
    @SubscribeEvent
    public void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof Animal animal)) {
            return;
        }

        boolean alreadyInjected = animal.goalSelector.getAvailableGoals().stream()
                .anyMatch(wrapped -> wrapped.getGoal() instanceof FeedTemptGoal);
        if (!alreadyInjected) {
            animal.goalSelector.addGoal(TEMPT_PRIORITY, new FeedTemptGoal(animal, TEMPT_SPEED));
        }
    }

    /**
     * 处理标签重载事件。
     * <p>
     * 推进标签代次，使持有标签快照的 AI 在下次 tick 重新读取饲料集合。
     *
     * @param event 标签重载事件
     */
    @SubscribeEvent
    public void onTagsUpdated(TagsUpdatedEvent event) {
        FeedRegistry.onTagsUpdated();
    }
}