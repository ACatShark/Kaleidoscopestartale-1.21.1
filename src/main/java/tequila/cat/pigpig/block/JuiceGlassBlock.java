package tequila.cat.pigpig.block;

import com.github.ysbbbbbb.kaleidoscopetavern.block.mixology.CocktailBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 果汁杯：碰撞箱按果汁模型的杯体形状收紧。
 * 酒馆默认酒杯用的是 8×10×8 的方块箱，果汁模型杯体是 6×12×6（x/z 5~11、y 0~12），
 * 所以这里单独覆盖，避免碰撞箱比模型胖一圈。
 */
public class JuiceGlassBlock extends CocktailBlock {
    private static final VoxelShape SHAPE =
            Shapes.box(5.0D / 16.0D, 0.0D, 5.0D / 16.0D, 11.0D / 16.0D, 12.0D / 16.0D, 11.0D / 16.0D);

    public JuiceGlassBlock() {
        super();
    }

    public JuiceGlassBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
}
