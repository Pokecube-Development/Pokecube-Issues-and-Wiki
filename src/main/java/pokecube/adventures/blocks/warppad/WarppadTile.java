package pokecube.adventures.blocks.warppad;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import pokecube.adventures.PokecubeAdv;
import pokecube.core.blocks.InteractableTile;
import pokecube.core.utils.PokecubeSerializer.TeleDest;
import thut.api.maths.Vector4;

public class WarppadTile extends InteractableTile
{
    public static final TileEntityType<? extends TileEntity> TYPE = TileEntityType.Builder.create(WarppadTile::new,
            PokecubeAdv.WARPPAD).build(null);

    public static void warp(final Entity entityIn, final TeleDest dest)
    {
        if (entityIn.getEntityWorld().isRemote) return;
        entityIn.setPositionAndUpdate(dest.loc.x, dest.loc.y, dest.loc.z);
        if (dest.loc.w != entityIn.dimension.getId())
        {
            final DimensionType destination = DimensionType.getById((int) dest.loc.w);
            System.out.println(destination);
            if (!net.minecraftforge.common.ForgeHooks.onTravelToDimension(entityIn, destination)) return;

            if (entityIn instanceof ServerPlayerEntity)
            {
                // final ServerPlayerEntity player = (ServerPlayerEntity)
                // entityIn;
                // player.getServer().getPlayerList().recreatePlayerEntity(player,
                // destination, true);
            }
            else
            {
                entityIn.world.getProfiler().startSection("changeDimension");
                final MinecraftServer minecraftserver = entityIn.getServer();
                final DimensionType dimensiontype = entityIn.dimension;
                final ServerWorld serverworld = minecraftserver.getWorld(dimensiontype);
                final ServerWorld serverworld1 = minecraftserver.getWorld(destination);
                entityIn.dimension = destination;
                entityIn.detach();

                final Vec3d vec3d = entityIn.getMotion();
                final float f = 0.0F;
                final BlockPos blockpos = entityIn.getPosition();

                entityIn.world.getProfiler().endStartSection("reloading");
                final Entity entity = entityIn.getType().create(serverworld1);
                if (entity != null)
                {
                    entity.copyDataFromOld(entityIn);
                    entity.moveToBlockPosAndAngles(blockpos, entity.rotationYaw + f, entity.rotationPitch);
                    entity.setMotion(vec3d);
                    serverworld1.func_217460_e(entity);
                }

                entityIn.remove(false);
                entityIn.world.getProfiler().endSection();
                serverworld.resetUpdateEntityTick();
                serverworld1.resetUpdateEntityTick();
                entityIn.world.getProfiler().endSection();
            }
        }
    }

    public TeleDest dest   = null;
    public int      energy = 0;

    public WarppadTile()
    {
        super(WarppadTile.TYPE);
    }

    public WarppadTile(final TileEntityType<?> tileEntityTypeIn)
    {
        super(tileEntityTypeIn);
    }

    @Override
    public void onWalkedOn(final Entity entityIn)
    {
        if (this.dest == null) this.dest = new TeleDest(new Vector4(this.getPos().getX(), this.getPos().getY() + 4, this
                .getPos().getZ(), this.world.dimension.getType().getId()));
        if (this.dest != null) WarppadTile.warp(entityIn, this.dest);
    }

    @Override
    public void read(final CompoundNBT compound)
    {
        if (compound.contains("dest"))
        {
            final CompoundNBT tag = compound.getCompound("dest");
            this.dest = TeleDest.readFromNBT(tag);
        }
        super.read(compound);
    }

    @Override
    public CompoundNBT write(final CompoundNBT compound)
    {
        if (this.dest != null)
        {
            final CompoundNBT tag = new CompoundNBT();
            this.dest.writeToNBT(tag);
            compound.put("dest", tag);
        }
        return super.write(compound);
    }
}
