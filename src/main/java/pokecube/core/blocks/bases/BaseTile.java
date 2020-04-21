package pokecube.core.blocks.bases;

import java.util.UUID;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.dimension.DimensionType;
import pokecube.core.PokecubeCore;
import pokecube.core.blocks.InteractableTile;
import pokecube.core.world.dimension.SecretBaseDimension;
import thut.api.OwnableCaps;
import thut.api.block.IOwnableTE;

public class BaseTile extends InteractableTile
{
    public static TileEntityType<? extends TileEntity> TYPE;

    boolean           any       = false;
    public BlockPos   last_base = null;
    public BlockState original  = Blocks.STONE.getDefaultState();

    public BaseTile()
    {
        super(BaseTile.TYPE);
    }

    @Override
    public ActionResultType onInteract(final BlockPos pos, final PlayerEntity player, final Hand hand,
            final BlockRayTraceResult hit)
    {
        if (!(player instanceof ServerPlayerEntity)) return ActionResultType.SUCCESS;
        final MinecraftServer server = player.getServer();
        UUID targetBase = player.getUniqueID();
        if (!this.any)
        {
            final IOwnableTE tile = (IOwnableTE) this.getCapability(OwnableCaps.CAPABILITY).orElse(null);
            targetBase = tile.getOwnerId();
            if (targetBase == null) return ActionResultType.SUCCESS;
            BlockPos exit_here;
            try
            {
                exit_here = SecretBaseDimension.getSecretBaseLoc(targetBase, server, player.dimension);
            }
            catch (final Exception e)
            {
                PokecubeCore.LOGGER.error(e);
                return ActionResultType.FAIL;
            }
            if (this.last_base == null) this.last_base = exit_here;
            if (exit_here.distanceSq(this.last_base.getX(), this.last_base.getY(), this.last_base.getZ(), false) > 0.0)
            {
                // We need to remove the location.
                this.world.setBlockState(pos, this.original);
                player.sendMessage(new TranslationTextComponent("pokemob.removebase.stale"));
                return ActionResultType.FAIL;
            }
        }
        final DimensionType dim = player.dimension;
        if (dim == DimensionType.OVERWORLD) SecretBaseDimension.sendToBase((ServerPlayerEntity) player, targetBase);
        else SecretBaseDimension.sendToExit((ServerPlayerEntity) player, targetBase);
        return ActionResultType.SUCCESS;
    }

    @Override
    public void read(final CompoundNBT compound)
    {
        super.read(compound);
        this.any = compound.getBoolean("any_use");
        if (compound.contains("base_pos"))
        {
            final CompoundNBT tag = compound.getCompound("base_pos");
            this.last_base = NBTUtil.readBlockPos(tag);
        }
        if (compound.contains("revert_to"))
        {
            final CompoundNBT tag = compound.getCompound("revert_to");
            this.original = NBTUtil.readBlockState(tag);
        }
    }

    @Override
    public CompoundNBT write(final CompoundNBT compound)
    {
        compound.putBoolean("any_use", this.any);
        if (this.last_base != null)
        {
            final CompoundNBT tag = NBTUtil.writeBlockPos(this.last_base);
            compound.put("base_pos", tag);
        }
        if (this.original != null)
        {
            final CompoundNBT tag = NBTUtil.writeBlockState(this.original);
            compound.put("revert_to", tag);
        }
        return super.write(compound);
    }
}
