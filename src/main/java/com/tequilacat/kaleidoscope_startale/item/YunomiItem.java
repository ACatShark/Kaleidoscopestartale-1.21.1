package com.tequilacat.kaleidoscope_startale.item;

import com.tequilacat.kaleidoscope_startale.init.KSBlocks;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.UseOnContext;
import org.jetbrains.annotations.NotNull;

/**
 * 云吞物品类。
 * <p>
 * 继承自 BlockItem，对应云吞方块，堆叠上限为 16。
 * 默认右键不会直接放置方块，仅在玩家潜行使用（次要使用）时
 * 才交由父类处理放置逻辑，以避免与右键交互行为冲突。
 */
public class YunomiItem extends BlockItem {

    /**
     * 构造云吞物品。
     * <p>
     * 绑定云吞方块，并将堆叠上限设为 16。
     */
    public YunomiItem() {
        super(KSBlocks.YUNOMI.get(), new Properties().stacksTo(16));
    }

    /**
     * 处理在方块上使用物品的交互。
     * <p>
     * 当玩家为空或玩家处于次要使用状态（通常为潜行）时，
     * 交由父类执行默认的方块放置逻辑；
     * 否则返回 PASS，将交互让给方块的右键行为处理。
     *
     * @param context 使用上下文，包含玩家、目标方块等信息
     * @return 交互结果
     */
    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null || player.isSecondaryUseActive()) {
            return super.useOn(context);
        }
        return InteractionResult.PASS;
    }
}