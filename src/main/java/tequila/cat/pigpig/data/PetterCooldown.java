package tequila.cat.pigpig.data;

import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;

import tequila.cat.pigpig.attachment.ModAttachments;

/** 拍拍冷却：默认 12000 tick（10 分钟），喂食可缩短。 */
public final class PetterCooldown {
    public static final int COOLDOWN_TICKS = 12000;

    public static int remaining(Entity entity) {
        long expiry = entity.getData(ModAttachments.PET_COOLDOWN.get());
        if (expiry <= 0L) {
            return 0;
        }
        long left = expiry - entity.level().getGameTime();
        return left > 0L ? (int) Math.min(left, Integer.MAX_VALUE) : 0;
    }

    public static boolean isCooling(Entity entity) {
        return remaining(entity) > 0;
    }

    public static void reset(Entity entity) {
        entity.setData(ModAttachments.PET_COOLDOWN.get(), entity.level().getGameTime() + COOLDOWN_TICKS);
    }

    public static int feedingReduction(Entity entity) {
        return AgeableMob.getSpeedUpSecondsWhenFeeding(remaining(entity)) * 20;
    }

    public static boolean shortenByFeeding(Entity entity) {
        int reduction = feedingReduction(entity);
        if (reduction <= 0) {
            return false;
        }
        long expiry = entity.getData(ModAttachments.PET_COOLDOWN.get());
        entity.setData(ModAttachments.PET_COOLDOWN.get(), Math.max(0L, expiry - (long) reduction));
        return true;
    }

    private PetterCooldown() {
    }
}
