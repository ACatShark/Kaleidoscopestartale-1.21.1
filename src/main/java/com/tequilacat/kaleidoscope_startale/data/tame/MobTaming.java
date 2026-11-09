package com.tequilacat.kaleidoscope_startale.data.tame;

import com.tequilacat.kaleidoscope_startale.init.KSAttachments;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

/**
 * 生物驯养工具类。
 * <p>
 * 统一判断生物的驯养状态与归属关系，兼容原版 OwnableEntity、
 * 模组自身驯养数据以及第三方模组的驯养体系，并排除女仆模组的生物。
 */
public class MobTaming {

    /**
     * 女仆模组的命名空间。
     */
    private static final String TOUHOU_LITTLE_MAID = "touhou_little_maid";

    /**
     * 判断实体是否来自女仆模组。
     *
     * @param entity 目标实体
     * @return 是否为女仆模组实体
     */
    public static boolean isForeignMaid(LivingEntity entity) {
        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        return TOUHOU_LITTLE_MAID.equals(id.getNamespace());
    }

    /**
     * 判断实体是否已驯养。
     * <p>
     * 女仆模组实体一律视为未驯养；依次检查原版主人 UUID、
     * 模组自身驯养数据以及第三方模组的驯养状态。
     *
     * @param entity 目标实体
     * @return 是否已驯养
     */
    public static boolean isTamed(LivingEntity entity) {
        if (isForeignMaid(entity)) {
            return false;
        }
        if (entity instanceof OwnableEntity ownable && ownable.getOwnerUUID() != null) {
            return true;
        }
        if (entity.getData(KSAttachments.TAME.get()).isTamed()) {
            return true;
        }
        // 其它模组自带的驯养体系：直接读它自己的状态
        return TamingCompat.isForeignTamed(entity);
    }

    /**
     * 判断候选实体是否为宠物的主人。
     * <p>
     * 仅当候选为玩家时有效；优先按原版主人 UUID 比较，
     * 否则回退到模组自身驯养数据的主人判定。
     *
     * @param pet       宠物
     * @param candidate 候选实体
     * @return 是否为宠物主人
     */
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
        return pet.getData(KSAttachments.TAME.get()).isOwnedBy(player.getUUID());
    }
}