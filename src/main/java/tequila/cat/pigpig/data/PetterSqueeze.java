package tequila.cat.pigpig.data;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import tequila.cat.pigpig.attachment.ModAttachments;

/** 被捏时的挤压动画（纯客户端表现，靠同步的起始时刻计算）。 */
public final class PetterSqueeze {
    public static final int DURATION = 15;

    private static final float DOWN_TICKS = 5.0F;
    private static final float PRESSED_SCALE = 0.6F;
    private static final float MIN_PRESSED_SCALE = 0.25F;
    private static final float POWER_DEPTH_STEP = 0.07F;
    private static final float BACK_OVERSHOOT = 2.6F;
    private static final float ELASTIC_PERIOD = 2.0943952F;

    public static void start(LivingEntity entity, int power) {
        entity.setData(ModAttachments.SQUEEZE_START.get(), entity.level().getGameTime());
        entity.setData(ModAttachments.SQUEEZE_POWER.get(), Math.max(0, power));
    }

    public static float yScale(Entity entity, float partialTick) {
        long start = entity.getData(ModAttachments.SQUEEZE_START.get());
        if (start <= 0L) {
            return 1.0F;
        }
        float elapsed = (float) (entity.level().getGameTime() - start) + partialTick;
        if (elapsed < 0.0F || elapsed >= DURATION) {
            return 1.0F;
        }
        float pressed = pressedScale(entity);
        if (elapsed < DOWN_TICKS) {
            return outBack(elapsed / DOWN_TICKS, 1.0F, pressed);
        }
        float bounce = inQuart((elapsed / DOWN_TICKS - 1.0F) * 0.5F);
        return outElastic(bounce, pressed, 1.0F);
    }

    /** 锋利等级越高压得越扁。 */
    private static float pressedScale(Entity entity) {
        int power = entity.getData(ModAttachments.SQUEEZE_POWER.get());
        return Mth.clamp(PRESSED_SCALE - POWER_DEPTH_STEP * power, MIN_PRESSED_SCALE, PRESSED_SCALE);
    }

    public static float xzScale(float yScale) {
        return Mth.sqrt(1.0F / yScale);
    }

    private static float outBack(float x, float from, float to) {
        float c1 = BACK_OVERSHOOT;
        float c3 = c1 + 1.0F;
        float v = (float) (1.0D + (double) c3 * Math.pow(x - 1.0F, 3.0D) + (double) c1 * Math.pow(x - 1.0F, 2.0D));
        return from + v * (to - from);
    }

    private static float inQuart(float x) {
        return x * x * x * x;
    }

    private static float outElastic(float x, float from, float to) {
        float v;
        if (x <= 0.0F) {
            v = 0.0F;
        } else if (x >= 1.0F) {
            v = 1.0F;
        } else {
            v = (float) (Math.pow(2.0D, -10.0D * x) * Math.sin((x * 10.0D - 0.75D) * (double) ELASTIC_PERIOD) + 1.0D);
        }
        return from + v * (to - from);
    }

    private PetterSqueeze() {
    }
}
