package pokecube.adventures.blocks.siphon;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.entity.MobEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import pokecube.core.blocks.InteractableTile;

public class SiphonTile extends InteractableTile implements ITickableTileEntity
{
    public static class EnergyStore implements IEnergyStorage, ICapabilitySerializable<CompoundNBT>
    {
        private final LazyOptional<IEnergyStorage> holder = LazyOptional.of(() -> this);
        public int                                 currentOutput;
        public int                                 theoreticalOutput;

        @Override
        public <T> LazyOptional<T> getCapability(final Capability<T> cap, final Direction side)
        {
            return CapabilityEnergy.ENERGY.orEmpty(cap, this.holder);
        }

        @Override
        public CompoundNBT serializeNBT()
        {
            final CompoundNBT tag = new CompoundNBT();
            tag.putInt("cO", this.currentOutput);
            tag.putInt("tO", this.theoreticalOutput);
            return tag;
        }

        @Override
        public void deserializeNBT(final CompoundNBT nbt)
        {
            this.currentOutput = nbt.getInt("cO");
            this.theoreticalOutput = nbt.getInt("yO");
        }

        @Override
        public int receiveEnergy(final int maxReceive, final boolean simulate)
        {
            return 0;
        }

        @Override
        public int extractEnergy(final int maxExtract, final boolean simulate)
        {
            final int output = Math.min(maxExtract, this.currentOutput);
            if (!simulate) this.currentOutput -= output;
            return output;
        }

        @Override
        public int getEnergyStored()
        {
            return this.currentOutput;
        }

        @Override
        public int getMaxEnergyStored()
        {
            return this.theoreticalOutput;
        }

        @Override
        public boolean canExtract()
        {
            return true;
        }

        @Override
        public boolean canReceive()
        {
            return false;
        }

    }

    public static TileEntityType<? extends TileEntity> TYPE;

    public AxisAlignedBB   box;
    public List<MobEntity> mobs       = Lists.newArrayList();
    public long            updateTime = -1;
    public EnergyStore     energy;

    public SiphonTile()
    {
        super(SiphonTile.TYPE);
    }

    public SiphonTile(final TileEntityType<?> tileEntityTypeIn)
    {
        super(tileEntityTypeIn);
    }

    @Override
    public void tick()
    {
        if (!this.world.isRemote) MinecraftForge.EVENT_BUS.post(new SiphonTickEvent(this));
    }
}
