package com.tequilacat.kaleidoscope_startale.ai;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.item.Item;

import com.tequilacat.kaleidoscope_startale.util.FeedRegistry;

/**
 * 被饲料吸引的 AI。
 * <p>
 * 实体加入世界时挂载；饲料集合在标签重载后自动刷新，
 * 因此数据包改动饲料标签不需要重新生成生物。
 */
public class FeedTemptGoal extends TemptGoal {

    /** 目标生物。 */
    private final PathfinderMob mob;

    /** 当前生效的饲料集合，标签变化时原地更新。 */
    private final Set<Item> feeds;

    /** 上次刷新饲料集合时的标签代次。 */
    private int feedsGeneration = -1;

    /**
     * 创建饲料诱惑 AI。
     *
     * @param mob           目标生物
     * @param speedModifier 移动速度倍率
     */
    public FeedTemptGoal(PathfinderMob mob, double speedModifier) {
        this(mob, speedModifier, new HashSet<>());
    }

    private FeedTemptGoal(PathfinderMob mob, double speedModifier, Set<Item> feeds) {
        super(mob, speedModifier, stack -> feeds.contains(stack.getItem()), false);
        this.mob = mob;
        this.feeds = feeds;
    }

    @Override
    public boolean canUse() {
        refreshFeeds();
        // 没有可接受的饲料时直接放弃，避免每 tick 都去扫附近玩家
        return !this.feeds.isEmpty() && super.canUse();
    }

    @Override
    public void start() {
        refreshFeeds();
        super.start();
    }

    @Override
    public void tick() {
        refreshFeeds();
        super.tick();
    }

    /**
     * 标签代次变化时重新读取该生物可接受的饲料。
     */
    private void refreshFeeds() {
        int generation = FeedRegistry.tagGeneration();
        if (generation == this.feedsGeneration) {
            return;
        }
        this.feedsGeneration = generation;
        this.feeds.clear();
        this.feeds.addAll(FeedRegistry.feedsFor(this.mob));
    }
}
