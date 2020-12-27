package pokecube.adventures.blocks.warppad;

import java.util.List;

import org.nfunk.jep.JEP;

import com.google.common.collect.Lists;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;
import net.minecraftforge.energy.IEnergyStorage;
import pokecube.adventures.PokecubeAdv;
import pokecube.core.blocks.InteractableTile;
import thut.api.entity.ThutTeleporter;
import thut.api.entity.ThutTeleporter.TeleDest;
import thut.api.maths.Vector3;

public class WarppadTile extends InteractableTile implements IEnergyStorage
{
    public static List<RegistryKey<World>> invalidDests   = Lists.newArrayList();
    public static List<RegistryKey<World>> invalidSources = Lists.newArrayList();
    public static JEP                      parser;

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
        ThutTeleporter.transferTo(entityIn, dest, sound);
    }

    private TeleDest dest         = null;
    public int       energy       = 0;
    boolean          noEnergyNeed = false;

    public WarppadTile()
    {
        super(PokecubeAdv.WARPPAD_TYPE.get());
    }

    public WarppadTile(final TileEntityType<?> tileEntityTypeIn)
    {
        super(tileEntityTypeIn);
    }

    public TeleDest getDest()
    {
        if (this.dest == null) this.dest = new TeleDest().setPos(GlobalPos.getPosition(this.getWorld() != null ? this
                .getWorld().getDimensionKey() : World.OVERWORLD, this.getPos().up(4)));
        return this.dest;
    }

    @Override
    public void onWalkedOn(final Entity entityIn)
    {
        // TODO possible error log when things fail for reasons?
        if (WarppadTile.invalidSources.contains(entityIn.getEntityWorld().getDimensionKey()) || entityIn
                .getEntityWorld().isRemote) return;

        final TeleDest dest = this.getDest();
        final BlockPos link = dest.loc.getPos();
        final long time = this.world.getGameTime();
        final long lastStepped = entityIn.getPersistentData().getLong("lastWarpPadUse");
        // No step now, too soon.
        if (lastStepped - WarppadTile.COOLDOWN > time) return;
        entityIn.getPersistentData().putLong("lastWarpPadUse", time);
        if (!this.noEnergyNeed && PokecubeAdv.config.warpPadEnergy)
        {

            double cost = 0;
            final Vector3 here = Vector3.getNewVector().set(this);
            WarppadTile.parser.setVarValue("dx", link.getX() - here.x);
            WarppadTile.parser.setVarValue("dy", link.getY() - here.y);
            WarppadTile.parser.setVarValue("dz", link.getZ() - here.z);
            WarppadTile.parser.setVarValue("dw", 0);// TODO Decide on distance
                                                    // between dimensions
            cost = WarppadTile.parser.getValue();
            if (!this.noEnergyNeed && this.energy < cost)
            {
                this.getWorld().playSound(null, this.getPos().getX() + 0.5, this.getPos().getY() + 0.5, this.getPos()
                        .getZ() + 0.5, SoundEvents.BLOCK_NOTE_BLOCK_BASEDRUM, SoundCategory.BLOCKS, 1, 1);
                return;
            }
            else this.energy -= cost;
        }
        this.getWorld().playSound(null, this.getPos().getX() + 0.5, this.getPos().getY() + 0.5, this.getPos().getZ()
                + 0.5, SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.BLOCKS, 1, 1);
        this.getWorld().playSound(null, link.getX() + 0.5, link.getY() + 0.5, link.getZ() + 0.5,
                SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.BLOCKS, 1, 1);
        WarppadTile.warp(entityIn, dest, true);
    }

    @Override
    public void read(final BlockState stateIn, final CompoundNBT compound)
    {
        if (compound.contains("dest"))
        {
            final CompoundNBT tag = compound.getCompound("dest");
            this.dest = TeleDest.readFromNBT(tag);
        }
        this.energy = compound.getInt("energy");
        this.noEnergyNeed = compound.getBoolean("noEnergyNeed");
        super.read(stateIn, compound);
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
        if (maxReceive + this.energy > this.getMaxEnergyStored()) var = this.getMaxEnergyStored() - this.energy;
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
