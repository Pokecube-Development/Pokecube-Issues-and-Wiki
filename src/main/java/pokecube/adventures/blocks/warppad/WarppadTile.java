package pokecube.adventures.blocks.warppad;

import java.util.List;

import org.nfunk.jep.JEP;

import com.google.common.collect.Lists;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.energy.IEnergyStorage;
import pokecube.adventures.PokecubeAdv;
import pokecube.core.blocks.InteractableTile;
import pokecube.core.utils.PokecubeSerializer.TeleDest;
import thut.api.entity.ThutTeleporter;
import thut.api.maths.Vector3;
import thut.api.maths.Vector4;

public class WarppadTile extends InteractableTile implements IEnergyStorage
{
    public static TileEntityType<? extends TileEntity> TYPE;
    public static List<DimensionType>                  invalidDests   = Lists.newArrayList();
    public static List<DimensionType>                  invalidSources = Lists.newArrayList();
    public static JEP                                  parser;

    public static void initParser(final String function)
    {
        WarppadTile.parser = new JEP();
        WarppadTile.parser.initFunTab(); // clear the contents of the function
                                         // table
        WarppadTile.parser.addStandardFunctions();
        WarppadTile.parser.initSymTab(); // clear the contents of the symbol
                                         // table
        WarppadTile.parser.addStandardConstants();
        WarppadTile.parser.addComplex(); // among other things adds i to the
                                         // symbol
        // table
        WarppadTile.parser.addVariable("dw", 0);
        WarppadTile.parser.addVariable("dx", 0);
        WarppadTile.parser.addVariable("dy", 0);
        WarppadTile.parser.addVariable("dz", 0);
        WarppadTile.parser.parseExpression(function);
    }

    public static double MAXRANGE = 64;
    public static int    COOLDOWN = 20;

    public static void warp(final Entity entityIn, final TeleDest dest, final boolean sound)
    {
        ThutTeleporter.transferTo(entityIn, dest.loc, sound);
    }

    private TeleDest dest         = null;
    public int       energy       = 0;
    boolean          noEnergyNeed = false;

    public WarppadTile()
    {
        super(WarppadTile.TYPE);
    }

    public WarppadTile(final TileEntityType<?> tileEntityTypeIn)
    {
        super(tileEntityTypeIn);
    }

    public TeleDest getDest()
    {
        if (this.dest == null) this.dest = new TeleDest(new Vector4(this.getPos().getX() + 0.5, this.getPos().getY()
                + 4, this.getPos().getZ() + 0.5, this.world.dimension.getType().getId()));
        return this.dest;
    }

    @Override
    public void onWalkedOn(final Entity entityIn)
    {
        // TODO possible error log when things fail for reasons?
        if (WarppadTile.invalidSources.contains(entityIn.dimension)) return;

        final TeleDest dest = this.getDest();
        if (!this.noEnergyNeed && PokecubeAdv.config.warpPadEnergy)
        {
            final long time = this.world.getGameTime();
            final long lastStepped = entityIn.getPersistentData().getLong("lastWarpPadUse");
            // No step now, too soon.
            if (lastStepped + WarppadTile.COOLDOWN < time) return;

            double cost = 0;
            final Vector4 link = dest.loc;
            final Vector3 here = Vector3.getNewVector().set(this);
            WarppadTile.parser.setVarValue("dx", link.x - here.x);
            WarppadTile.parser.setVarValue("dy", link.y - here.y);
            WarppadTile.parser.setVarValue("dz", link.z - here.z);
            WarppadTile.parser.setVarValue("dw", link.w - this.getWorld().getDimension().getType().getId());
            cost = WarppadTile.parser.getValue();
            entityIn.getPersistentData().putLong("lastWarpPadUse", time);
            if (this.energy < cost)
            {
                entityIn.playSound(SoundEvents.BLOCK_NOTE_BLOCK_BASEDRUM, 1.0F, 1.0F);
                return;
            }
            else this.energy -= cost;
        }
        WarppadTile.warp(entityIn, dest, true);
    }

    @Override
    public void read(final CompoundNBT compound)
    {
        if (compound.contains("dest"))
        {
            final CompoundNBT tag = compound.getCompound("dest");
            this.dest = TeleDest.readFromNBT(tag);
        }
        this.energy = compound.getInt("energy");
        this.noEnergyNeed = compound.getBoolean("noEnergyNeed");
        super.read(compound);
    }

    @Override
    public CompoundNBT write(final CompoundNBT compound)
    {
        final CompoundNBT tag = new CompoundNBT();
        this.getDest().writeToNBT(tag);
        compound.put("dest", tag);
        compound.putInt("energy", this.energy);
        compound.putBoolean("noEnergyNeed", this.noEnergyNeed);
        return super.write(compound);
    }

    @Override
    public int receiveEnergy(final int maxReceive, final boolean simulate)
    {
        int var = maxReceive;
        if (maxReceive + this.energy < this.getMaxEnergyStored()) var = this.getMaxEnergyStored() - this.energy;
        if (!simulate) this.energy += var;
        this.energy = Math.max(0, this.energy);
        this.energy = Math.min(this.getMaxEnergyStored(), this.energy);
        return var;
    }

    @Override
    public int extractEnergy(final int maxExtract, final boolean simulate)
    {
        int var = maxExtract;
        if (maxExtract < this.energy) var = this.energy;
        if (!simulate) this.energy -= var;
        this.energy = Math.max(0, this.energy);
        this.energy = Math.min(this.getMaxEnergyStored(), this.energy);
        return var;
    }

    @Override
    public int getEnergyStored()
    {
        return this.energy;
    }

    @Override
    public int getMaxEnergyStored()
    {
        return PokecubeAdv.config.warpPadMaxEnergy;
    }

    @Override
    public boolean canExtract()
    {
        return false;
    }

    @Override
    public boolean canReceive()
    {
        return true;
    }
}
