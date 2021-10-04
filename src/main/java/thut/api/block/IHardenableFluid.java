package thut.api.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface IHardenableFluid
{
    public BlockState getSolidState(Level world, BlockPos location);

    public void tryHarden(Level world, BlockPos vec);
}
