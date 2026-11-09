package tequila.cat.pigpig.ai;

import java.util.Set;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** 被饲料吸引的 AI（喂食事件里按实体可接受的饲料动态挂载）。 */
public class FeedTemptGoal extends TemptGoal {
    public FeedTemptGoal(PathfinderMob mob, double speedModifier, Set<Item> feeds) {
        super(mob, speedModifier, stack -> matches(feeds, stack), false);
    }

    private static boolean matches(Set<Item> feeds, ItemStack stack) {
        return feeds.contains(stack.getItem());
    }
}
