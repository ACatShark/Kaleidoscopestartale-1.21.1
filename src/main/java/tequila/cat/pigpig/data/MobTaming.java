package tequila.cat.pigpig.data;

import java.util.UUID;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.player.Player;

import tequila.cat.pigpig.attachment.ModAttachments;

/**
 * 「已驯养」判定：原版可驯服生物看 OwnableEntity，其次是 Startale 的附件，
 * 最后问一次第三方生物自己的驯养状态（这样别的模组驯服过的生物也能被拍拍器识别）。
 */
public final class MobTaming {
    /** 自带驯养/好感度体系的其它 mod 生物，本模组完全不介入。 */
    private static final String TOUHOU_LITTLE_MAID = "touhou_little_maid";

    public static boolean isForeignMaid(LivingEntity entity) {
        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        return id != null && TOUHOU_LITTLE_MAID.equals(id.getNamespace());
    }

    public static boolean isTamed(LivingEntity entity) {
        if (isForeignMaid(entity)) {
            return false;
        }
        if (entity instanceof OwnableEntity ownable && ownable.getOwnerUUID() != null) {
            return true;
        }
        if (entity.getData(ModAttachments.TAME.get()).isTamed()) {
            return true;
        }
        // 其它模组自带的驯养体系：直接读它自己的状态
        return TamingCompat.isForeignTamed(entity);
    }

    public static boolean isOwner(LivingEntity pet, Entity candidate) {
        if (!(candidate instanceof Player player)) {
            return false;
        }
        if (pet instanceof OwnableEntity ownable) {
            UUID owner = ownable.getOwnerUUID();
            if (owner != null) {
                return owner.equals(player.getUUID());
            }
        }
        return pet.getData(ModAttachments.TAME.get()).isOwnedBy(player.getUUID());
    }

    private MobTaming() {
    }
}
