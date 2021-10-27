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
import net.minecraftforge.common.util.LazyOptional;
import pokecube.adventures.PokecubeAdv;
import pokecube.core.PokecubeCore;
import pokecube.core.database.Database;
import thut.api.ThutCaps;
import thut.api.entity.CopyCaps;
import thut.api.entity.ICopyMob;
import thut.api.entity.IMobColourable;
import thut.core.common.network.TileUpdate;

public class StatueEntity extends TileEntity
{
    public int ticks = 0;

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
            LivingEntity before = copy.getCopiedMob();
            if (before == null)
            {
                copy.setCopiedMob(before = PokecubeCore.createPokemob(Database.missingno, this.level));
                if (copy.getCopiedID() == null) copy.setCopiedID(before.getType().getRegistryName());
                if (!copy.getCopiedNBT().isEmpty()) before.deserializeNBT(copy.getCopiedNBT());
                before = null;
            }
            copy.onBaseTick(this.level, null);
            if (copy.getCopiedMob() == null) break check;
            if (copy.getCopiedMob() != before)
            {
                final BlockPos pos = this.getBlockPos();
                final LivingEntity mob = copy.getCopiedMob();
                final LazyOptional<IMobColourable> colourable = mob.getCapability(ThutCaps.COLOURABLE);
                if (colourable.isPresent()) colourable.orElse(null).getRGBA();
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
        return this.serializeNBT();
    }

    @Override
    public void handleUpdateTag(final BlockState state, final CompoundNBT tag)
    {
        this.deserializeNBT(tag);
        this.checkMob();
    }

    @Override
    public void load(final BlockState state, final CompoundNBT tag)
    {
        super.load(state, tag);
        // The stuff below only matters for when this is placed directly or nbt
        // edited. when loading normally, level is null, so we exit here.
        if (this.level == null) return;
        // Server side send packet that it changed
        if (!this.level.isClientSide()) TileUpdate.sendUpdate(this);
        else
        {
            // Client side clear the mob
            final ICopyMob copy = CopyCaps.get(this);
            copy.setCopiedMob(null);
        }
        // Both sides refresh mob if changed
        this.checkMob();
    }
}
