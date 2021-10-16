package pokecube.adventures.blocks.statue;

import java.util.UUID;

import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import pokecube.adventures.PokecubeAdv;
import thut.api.entity.CopyCaps;
import thut.api.entity.ICopyMob;
import thut.core.common.network.TileUpdate;

public class StatueEntity extends TileEntity
{
    public StatueEntity(final TileEntityType<?> type)
    {
        super(type);
    }

    public StatueEntity()
    {
        super(PokecubeAdv.STATUE_TYPE.get());
    }

    public void checkMob()
    {

        final ICopyMob copy = CopyCaps.get(this);
        check:
        if (copy != null)
        {
            final LivingEntity before = copy.getCopiedMob();
            copy.onBaseTick(this.level, null);
            if (copy.getCopiedMob() == null) break check;

            if (copy.getCopiedMob() != before)
            {
                final BlockPos pos = this.getBlockPos();
                final LivingEntity mob = copy.getCopiedMob();
                mob.setUUID(UUID.randomUUID());
                mob.setPos(pos.getX(), pos.getY(), pos.getZ());
                final Direction dir = this.getBlockState().getValue(HorizontalBlock.FACING);
                switch (dir)
                {
                case EAST:
                    mob.yRot = mob.yBodyRot = mob.yRotO = mob.yBodyRotO = -90;
                    break;
                case NORTH:
                    mob.yRot = mob.yBodyRot = mob.yRotO = mob.yBodyRotO = 180;
                    break;
                case SOUTH:
                    mob.yRot = mob.yBodyRot = mob.yRotO = mob.yBodyRotO = 0;
                    break;
                case WEST:
                    mob.yRot = mob.yBodyRot = mob.yRotO = mob.yBodyRotO = 90;
                    break;
                default:
                    break;
                }
                copy.setCopiedNBT(copy.getCopiedMob().serializeNBT());
                this.requestModelDataUpdate();
            }
        }
    }

    @Override
    public SUpdateTileEntityPacket getUpdatePacket()
    {
        this.checkMob();
        return new SUpdateTileEntityPacket(this.worldPosition, 6, this.getUpdateTag());
    }

    @Override
    public CompoundNBT getUpdateTag()
    {
        this.checkMob();
        return this.save(new CompoundNBT());
    }

    @Override
    public void handleUpdateTag(final BlockState state, final CompoundNBT tag)
    {
        super.handleUpdateTag(state, tag);
        final ICopyMob copy = CopyCaps.get(this);
        if (copy != null) copy.onBaseTick(this.level, null);
    }

    @Override
    public void load(final BlockState state, final CompoundNBT tag)
    {
        super.load(state, tag);
        if (this.level instanceof ServerWorld) TileUpdate.sendUpdate(this);
    }
}
