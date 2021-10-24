package pokecube.adventures.blocks.statue;

import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import pokecube.adventures.PokecubeAdv;
import thut.api.entity.CopyCaps;
import thut.api.entity.ICopyMob;
import thut.core.common.network.TileUpdate;

public class StatueEntity extends BlockEntity
{
    public StatueEntity(final BlockEntityType<?> type, final BlockPos pos, final BlockState state)
    {
        super(type, pos, state);
    }

    public StatueEntity(final BlockPos pos, final BlockState state)
    {
        this(PokecubeAdv.STATUE_TYPE.get(), pos, state);
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
                copy.setCopiedID(before.getType().getRegistryName());
                before = null;
            }
            copy.onBaseTick(this.level, null);
            if (copy.getCopiedMob() == null) break check;

            if (copy.getCopiedMob() != before)
            {
                final BlockPos pos = this.getBlockPos();
                final LivingEntity mob = copy.getCopiedMob();
                mob.setUUID(UUID.randomUUID());
                mob.setPos(pos.getX(), pos.getY(), pos.getZ());
                final Direction dir = this.getBlockState().getValue(HorizontalDirectionalBlock.FACING);
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
    public ClientboundBlockEntityDataPacket getUpdatePacket()
    {
        this.checkMob();
        return new ClientboundBlockEntityDataPacket(this.worldPosition, 6, this.getUpdateTag());
    }

    @Override
    public CompoundTag getUpdateTag()
    {
        this.checkMob();
        return this.save(new CompoundTag());
    }

    @Override
    public void handleUpdateTag(final CompoundTag tag)
    {
        super.handleUpdateTag(tag);
        final ICopyMob copy = CopyCaps.get(this);
        if (copy != null) copy.onBaseTick(this.level, null);
    }

    @Override
    public void load(final CompoundTag tag)
    {
        super.load(tag);
        if (this.level instanceof ServerLevel) TileUpdate.sendUpdate(this);
    }
}
