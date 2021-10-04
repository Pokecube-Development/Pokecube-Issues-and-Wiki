package thut.api.entity.blockentity.world;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.UpgradeData;
import thut.api.entity.blockentity.IBlockEntity;

public class EntityChunk extends LevelChunk
{
    public static class EntityChunkPrimer extends ProtoChunk
    {

        public EntityChunkPrimer(final ChunkPos pos)
        {
            super(pos, new UpgradeData(new CompoundTag()));
        }

    }

    IBlockEntityWorld worldE;

    public EntityChunk(final IBlockEntityWorld worldIn_, final ChunkPos pos)
    {
        super((Level) worldIn_, new EntityChunkPrimer(pos));
        this.worldE = worldIn_;
    }

    @Override
    public BlockState getBlockState(final BlockPos pos)
    {
        if (!this.worldE.inBounds(pos)) return Blocks.AIR.defaultBlockState();
        final IBlockEntity mob = this.worldE.getBlockEntity();
        final Entity entity = (Entity) mob;
        final int i = pos.getX() - Mth.floor(entity.getX());
        final int j = pos.getY() - Mth.floor(entity.getY());
        final int k = pos.getZ() - Mth.floor(entity.getZ());
        return mob.getBlocks()[i][j][k];
    }

    @Override
    public BlockState setBlockState(final BlockPos pos, final BlockState state, final boolean isMoving)
    {
        if (!this.worldE.inBounds(pos)) return Blocks.AIR.defaultBlockState();
        final IBlockEntity mob = this.worldE.getBlockEntity();
        final Entity entity = (Entity) mob;
        final int i = pos.getX() - Mth.floor(entity.getX());
        final int j = pos.getY() - Mth.floor(entity.getY());
        final int k = pos.getZ() - Mth.floor(entity.getZ());
        mob.getBlocks()[i][j][k] = state;
        return state;
    }

    @Override
    public void setBlockEntity(final BlockPos pos, final BlockEntity tile)
    {
        if (!this.worldE.inBounds(pos)) return;
        final IBlockEntity mob = this.worldE.getBlockEntity();
        final Entity entity = (Entity) mob;
        final int i = pos.getX() - Mth.floor(entity.getX());
        final int j = pos.getY() - Mth.floor(entity.getY());
        final int k = pos.getZ() - Mth.floor(entity.getZ());
        mob.getTiles()[i][j][k] = tile;
        if (tile != null)
        {
            tile.setLevelAndPosition((Level) this.worldE, pos.immutable());
            final boolean invalid = tile.isRemoved();
            if (!invalid) tile.setRemoved();
            tile.clearRemoved();
        }
        return;
    }

    @Override
    public BlockEntity getBlockEntity(final BlockPos pos)
    {
        return super.getBlockEntity(pos);
    }

}
