package thut.mixin.accessors;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import thut.api.entity.multipart.IEntityAccess;

@Mixin(Entity.class)
public abstract class EntityAccessor implements IEntityAccess
{
    @Override
    @Mutable
    @Accessor("position")
    public abstract void thutcore$setPosition(Vec3 position);

    @Override
    @Mutable
    @Accessor("blockPosition")
    public abstract void thutcore$setBlockPosition(BlockPos pos);

    @Override
    @Mutable
    @Accessor("chunkPosition")
    public abstract void thutcore$setChunkPosition(ChunkPos pos);

    @Override
    @Mutable
    @Accessor("inBlockState")
    public abstract void thutcore$setinBlockState(BlockState state);
}
