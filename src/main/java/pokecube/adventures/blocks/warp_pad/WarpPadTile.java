package pokecube.adventures.blocks.warp_pad;

import java.util.List;

import org.nfunk.jep.JEP;

import com.google.common.collect.Lists;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.energy.IEnergyStorage;
import pokecube.adventures.PokecubeAdv;
import pokecube.core.blocks.InteractableTile;
import thut.api.Tracker;
import thut.api.entity.ThutTeleporter;
import thut.api.entity.ThutTeleporter.TeleDest;
import thut.api.maths.Vector3;

public class WarpPadTile extends InteractableTile implements IEnergyStorage
{
    public static List<ResourceKey<Level>> invalidDests = Lists.newArrayList();
    public static List<ResourceKey<Level>> invalidSources = Lists.newArrayList();
    public static JEP parser;

    public static void initParser(final String function)
    {
        WarpPadTile.parser = new JEP();
        WarpPadTile.parser.initFunTab(); // clear the contents of the function
                                         // table
        WarpPadTile.parser.addStandardFunctions();
        WarpPadTile.parser.initSymTab(); // clear the contents of the symbol
                                         // table
        WarpPadTile.parser.addStandardConstants();
        WarpPadTile.parser.addComplex(); // among other things adds i to the
                                         // symbol
        // table
        WarpPadTile.parser.addVariable("dw", 0);
        WarpPadTile.parser.addVariable("dx", 0);
        WarpPadTile.parser.addVariable("dy", 0);
        WarpPadTile.parser.addVariable("dz", 0);
        WarpPadTile.parser.parseExpression(function);
    }

    public static double MAXRANGE = 64;
    public static int COOLDOWN = 20;

    public static void warp(final Entity entityIn, final TeleDest dest, final boolean sound)
    {
        ThutTeleporter.transferTo(entityIn, dest, sound);
    }

    private TeleDest dest = null;
    public int energy = 0;
    boolean noEnergyNeed = false;

    public WarpPadTile(final BlockPos pos, final BlockState state)
    {
        this(PokecubeAdv.WARP_PAD_TYPE.get(), pos, state);
    }

    public WarpPadTile(final BlockEntityType<?> tileEntityTypeIn, final BlockPos pos, final BlockState state)
    {
        super(tileEntityTypeIn, pos, state);
    }

    public TeleDest getDest()
    {
        if (this.dest == null) this.dest = new TeleDest().setPos(GlobalPos.of(
                this.getLevel() != null ? this.getLevel().dimension() : Level.OVERWORLD, this.getBlockPos().above(4)));
        return this.dest;
    }

    @Override
    public void onWalkedOn(final Entity entityIn)
    {
        // TODO possible error log when things fail for reasons?
        if (WarpPadTile.invalidSources.contains(entityIn.getCommandSenderWorld().dimension())
                || entityIn.getCommandSenderWorld().isClientSide)
            return;

        final TeleDest dest = this.getDest();
        final BlockPos link = dest.loc.pos();
        final long time = Tracker.instance().getTick();
        final long lastStepped = entityIn.getPersistentData().getLong("lastWarpPadUse");
        // No step now, too soon.
        if (lastStepped - WarpPadTile.COOLDOWN > time) return;
        entityIn.getPersistentData().putLong("lastWarpPadUse", time);
        if (!this.noEnergyNeed && PokecubeAdv.config.warpPadEnergy)
        {

            double cost = 0;
            final Vector3 here = Vector3.getNewVector().set(this);
            WarpPadTile.parser.setVarValue("dx", link.getX() - here.x);
            WarpPadTile.parser.setVarValue("dy", link.getY() - here.y + 0.5);
            WarpPadTile.parser.setVarValue("dz", link.getZ() - here.z);
            WarpPadTile.parser.setVarValue("dw", 0);// TODO Decide on distance
                                                    // between dimensions
            cost = WarpPadTile.parser.getValue();
            if (!this.noEnergyNeed && this.energy < cost)
            {
                this.getLevel().playSound(null, this.getBlockPos().getX() + 0.5, this.getBlockPos().getY() + 0.5,
                        this.getBlockPos().getZ() + 0.5, SoundEvents.NOTE_BLOCK_BASEDRUM, SoundSource.BLOCKS, 1, 1);
                return;
            }
            else this.energy -= cost;
        }
        this.getLevel().playSound(null, this.getBlockPos().getX() + 0.5, this.getBlockPos().getY() + 0.5,
                this.getBlockPos().getZ() + 0.5, SoundEvents.ENDERMAN_TELEPORT, SoundSource.BLOCKS, 1, 1);
        this.getLevel().playSound(null, link.getX() + 0.5, link.getY() + 0.5, link.getZ() + 0.5,
                SoundEvents.ENDERMAN_TELEPORT, SoundSource.BLOCKS, 1, 1);
        WarpPadTile.warp(entityIn, dest, true);
    }

    @Override
    public void load(final CompoundTag compound)
    {
        if (compound.contains("dest"))
        {
            final CompoundTag tag = compound.getCompound("dest");
            this.dest = TeleDest.readFromNBT(tag);
        }
        this.energy = compound.getInt("energy");
        this.noEnergyNeed = compound.getBoolean("noEnergyNeed");
        super.load(compound);
    }

    @Override
    public void saveAdditional(final CompoundTag compound)
    {
        final CompoundTag tag = new CompoundTag();
        this.getDest().writeToNBT(tag);
        compound.put("dest", tag);
        compound.putInt("energy", this.energy);
        compound.putBoolean("noEnergyNeed", this.noEnergyNeed);
        super.saveAdditional(compound);
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
