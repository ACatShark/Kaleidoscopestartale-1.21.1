package tequila.cat.pigpig.block;

import com.github.ysbbbbbb.kaleidoscopetavern.block.mixology.CocktailBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 带微弱自光的鸡尾酒方块。
 * tavern 的 CocktailBlock 只在无参构造里预置了 BlockBehaviour.Properties，
 * 外部拿不到那条属性链，所以这里直接覆写亮度，而不是用
 * CocktailBlock(Properties) 重建属性（那样会丢掉 noOcclusion、音效、活塞行为等设置）。
 */
public class GlowingCocktailBlock extends CocktailBlock {
    private final int lightLevel;

    public GlowingCocktailBlock(int lightLevel) {
        this.lightLevel = lightLevel;
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        return this.lightLevel;
    }
}
