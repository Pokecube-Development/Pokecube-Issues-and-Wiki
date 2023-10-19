package pokecube.legends.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.PlantType;

public class FlowerBase extends FlowerBlock
{
    public static final Block block = null;

    public FlowerBase(final MobEffect effects, int seconds, final BlockBehaviour.Properties properties)
    {
        super(() -> effects, seconds, properties);
    }

    @Override
    public boolean canBeReplaced(final BlockState state, final BlockPlaceContext useContext)
    {
        return false;
    }

    @Override
    public int getFlammability(final BlockState state, final BlockGetter world, final BlockPos pos,
            final Direction face)
    {
        return 2;
    }

    @Override
    public PlantType getPlantType(final BlockGetter world, final BlockPos pos)
    {
        return PlantType.PLAINS;
    }
}
