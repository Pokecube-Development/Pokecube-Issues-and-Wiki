package pokecube.core.blocks.bases;

import java.util.UUID;

import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
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
    boolean                                            any = false;

    public BaseTile()
    {
        super(BaseTile.TYPE);
    }

    @Override
    public boolean onInteract(final BlockPos pos, final PlayerEntity player, final Hand hand,
            final BlockRayTraceResult hit)
    {
        if (!(player instanceof ServerPlayerEntity)) return true;
        final MinecraftServer server = player.getServer();
        UUID targetBase = player.getUniqueID();
        if (!this.any)
        {
            final IOwnableTE tile = (IOwnableTE) this.getCapability(OwnableCaps.CAPABILITY).orElse(null);
            targetBase = tile.getOwnerId();
            if (targetBase == null) return true;
            BlockPos exit_here;
            try
            {
                exit_here = SecretBaseDimension.getSecretBaseLoc(targetBase, server, player.dimension);
            }
            catch (final Exception e)
            {
                PokecubeCore.LOGGER.error(e);
                return false;
            }
            if (exit_here.distanceSq(pos) > 15)
            {
                // We need to remove the location.
                this.world.setBlockState(pos, Blocks.STONE.getDefaultState());
                player.sendMessage(new TranslationTextComponent("pokemob.removebase.stale"));
            }
        }
        final DimensionType dim = player.dimension;
        if (dim == DimensionType.OVERWORLD) SecretBaseDimension.sendToBase((ServerPlayerEntity) player, targetBase);
        else SecretBaseDimension.sendToExit((ServerPlayerEntity) player, targetBase);
        return true;
    }

    @Override
    public void read(final CompoundNBT compound)
    {
        super.read(compound);
        this.any = compound.getBoolean("any_use");
    }

    @Override
    public CompoundNBT write(final CompoundNBT compound)
    {
        compound.putBoolean("any_use", this.any);
        return super.write(compound);
    }
}
