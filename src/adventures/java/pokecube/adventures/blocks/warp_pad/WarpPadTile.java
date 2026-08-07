package pokecube.adventures.blocks.warp_pad;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import net.minecraft.server.level.ServerLevel;
import org.nfunk.jep.JEP;

import com.google.common.collect.Lists;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;
import pokecube.adventures.PokecubeAdv;
import pokecube.core.blocks.InteractableTile;
import thut.api.Tracker;
import thut.api.entity.teleporting.TeleDest;
import thut.api.entity.teleporting.ThutTeleporter;
import thut.api.maths.Vector3;
import thut.lib.RegHelper;

public class WarpPadTile extends InteractableTile implements IEnergyStorage
{
    public static List<ResourceKey<Level>> invalidDests = Lists.newArrayList();
    public static List<ResourceKey<Level>> invalidSources = Lists.newArrayList();
    public static JEP parser;

    public static boolean initParser(JEP jep, String func)
    {
        jep.initFunTab(); // clear the contents of the function table
        jep.addStandardFunctions();
        jep.initSymTab(); // clear the contents of the symbol table
        jep.addStandardConstants();
        jep.addComplex(); // among other things adds i to the symbol table
        jep.addVariable("dw", 0);
        jep.addVariable("dx", 0);
        jep.addVariable("dy", 0);
        jep.addVariable("dz", 0);
        jep.parseExpression(func);
        return !jep.hasError();
    }

    public static void initParser(final String function)
    {
        parser = new JEP();
        initParser(parser, function);
    }

    public static record WarpDetails(ServerLevel source, GlobalPos pad, TeleDest dest){}

    public static Map<ResourceKey<Level>, Function<WarpDetails, Double>> METRIC_MAP = new HashMap<>();
    public static Function<WarpDetails, Double> METRIC;

    static
    {
        METRIC = (details)->{
            GlobalPos a = details.pad();
            GlobalPos b = details.dest().getPos();
            if(METRIC_MAP.containsKey(a.dimension()))
            {
                var _METRIC = METRIC_MAP.get(a.dimension());
                if(_METRIC != METRIC)
                {
                    return _METRIC.apply(details);
                }
            }
            BlockPos _a = a.pos(), _b = b.pos();
            parser.setVarValue("dx", _a.getX() - _b.getX());
            parser.setVarValue("dy", _a.getY() - _b.getY());
            parser.setVarValue("dz", _a.getZ() - _b.getZ());
            double dw = 0;
            // Default case, sort dimensions and pick difference in index in list.
            var registry = details.source().registryAccess().registry(RegHelper.DIMENSION_REGISTRY);
            if(registry.isPresent()){
                List<ResourceKey<Level>> list = registry.get().registryKeySet().stream().toList();
                int __a = list.indexOf(a.dimension());
                int __b = list.indexOf(b.dimension());
                dw = Math.abs(__a-__b);
            }
            parser.setVarValue("dw", dw);
            return parser.getValue();
        };
    }

    public static double MAXRANGE = 64;
    public static int COOLDOWN = 20;

    public static void warp(final Entity entityIn, final TeleDest dest, final boolean sound)
    {
        ThutTeleporter.transferTo(entityIn, dest, sound);
    }

    private TeleDest dest = null;
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
        if (WarpPadTile.invalidSources.contains(entityIn.level().dimension()) || entityIn.level().isClientSide)
            return;

        final TeleDest dest = this.getDest();
        final BlockPos link = dest.loc.pos();
        final long time = Tracker.instance().getTick();
        final long lastStepped = entityIn.getPersistentData().getLong("lastWarpPadUse");
        // No step now, too soon.
        if (lastStepped - WarpPadTile.COOLDOWN > time) return;
        entityIn.getPersistentData().putLong("lastWarpPadUse", time);
        if (!this.noEnergyNeed && PokecubeAdv.config.warpPadEnergy && getLevel() instanceof ServerLevel level)
        {
            final Vector3 here = new Vector3().set(this);
            GlobalPos posHere = GlobalPos.of(level.dimension(), here.getPos());
            double cost = METRIC.apply(new WarpDetails(level, posHere, dest));
            if (!this.noEnergyNeed && this.energy < cost)
            {
                level.playSound(null, this.getBlockPos().getX() + 0.5, this.getBlockPos().getY() + 0.5,
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
        this.setChanged();
    }

    @Override
    public void loadAdditional(final CompoundTag compound, Provider provider)
    {
        if (compound.contains("dest"))
        {
            final CompoundTag tag = compound.getCompound("dest");
            this.dest = TeleDest.readFromNBT(tag);
        }
        this.energy = compound.getInt("energy");
        this.noEnergyNeed = compound.getBoolean("noEnergyNeed");
        super.loadAdditional(compound, provider);
    }

    @Override
    public void saveAdditional(final CompoundTag compound, Provider provider)
    {
        final CompoundTag tag = new CompoundTag();
        this.getDest().writeToNBT(tag);
        compound.put("dest", tag);
        compound.putInt("energy", this.energy);
        compound.putBoolean("noEnergyNeed", this.noEnergyNeed);
        super.saveAdditional(compound, provider);
    }

    private int energy = 0;

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
        int var = Math.max(maxExtract, this.energy);
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
