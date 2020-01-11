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
import pokecube.adventures.PokecubeAdv;
import pokecube.core.blocks.InteractableTile;

public class SiphonTile extends InteractableTile implements ITickableTileEntity
{
    public static class EnergyStore implements IEnergyStorage, ICapabilitySerializable<CompoundNBT>
    {
        private final LazyOptional<IEnergyStorage> holder = LazyOptional.of(() -> this);
        public int                                 currentOutput;
        public int                                 theoreticalOutput;

        @Override
        public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side)
        {
            return CapabilityEnergy.ENERGY.orEmpty(cap, holder);
        }

        @Override
        public CompoundNBT serializeNBT()
        {
            CompoundNBT tag = new CompoundNBT();
            tag.putInt("cO", currentOutput);
            tag.putInt("tO", theoreticalOutput);
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundNBT nbt)
        {
            currentOutput = nbt.getInt("cO");
            theoreticalOutput = nbt.getInt("yO");
        }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate)
        {
            return 0;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate)
        {
            int output = Math.min(maxExtract, currentOutput);
            if (!simulate) currentOutput -= output;
            return output;
        }

        @Override
        public int getEnergyStored()
        {
            return currentOutput;
        }

        @Override
        public int getMaxEnergyStored()
        {
            return theoreticalOutput;
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

    public static final TileEntityType<? extends TileEntity> TYPE = TileEntityType.Builder.create(SiphonTile::new,
            PokecubeAdv.SIPHON).build(null);

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
        if (!world.isRemote) MinecraftForge.EVENT_BUS.post(new SiphonTickEvent(this));
    }
}
