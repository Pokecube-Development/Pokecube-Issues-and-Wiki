package thut.api.entity.multipart;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public interface IEntityAccess
{
    public void thutcore$setPosition(Vec3 position);
    public void thutcore$setBlockPosition(BlockPos pos);
    public void thutcore$setChunkPosition(ChunkPos pos);
    public void thutcore$setinBlockState(BlockState state);
}
