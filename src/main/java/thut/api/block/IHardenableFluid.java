package thut.api.block;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IHardenableFluid
{
    public BlockState getSolidState(World world, BlockPos location);

    public void tryHarden(World world, BlockPos vec);
}
