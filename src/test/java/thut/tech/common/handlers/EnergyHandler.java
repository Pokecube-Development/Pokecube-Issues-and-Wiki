package thut.tech.common.handlers;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thut.api.ThutCaps;
import thut.tech.Reference;
import thut.tech.common.TechCore;
import thut.tech.common.blocks.lift.ControllerTile;
import thut.tech.common.entity.EntityLift;

@Mod.EventBusSubscriber
public class EnergyHandler
{
    /** Pretty standard storable EnergyStorage. */
    public static class ProviderLift extends EnergyStorage implements ICapabilityProvider
    {
        private final LazyOptional<IEnergyStorage> holder = LazyOptional.of(() -> this);

        public ProviderLift()
        {
            super(TechCore.config.maxLiftEnergy, TechCore.config.maxLiftEnergy);
        }

        @Override
        public <T> LazyOptional<T> getCapability(final Capability<T> capability, final Direction facing)
        {
            return ThutCaps.ENERGY.orEmpty(capability, this.holder);
        }
    }

    /**
     * This is essentially a wrapper for the lift's energy storage capability.
     * This allows interfacing with the lift's energy via any of the connected
     * controllers.
     */
    public static class ProviderLiftController implements ICapabilityProvider, IEnergyStorage
    {
        private final LazyOptional<IEnergyStorage> holder = LazyOptional.of(() -> this);
        final ControllerTile tile;
        IEnergyStorage lift = null;

        public ProviderLiftController(final ControllerTile tile)
        {
            this.tile = tile;
        }

        @Override
        public boolean canExtract()
        {
            this.updateLift();
            if (this.lift != null) return this.lift.canExtract();
            return false;
        }

        @Override
        public boolean canReceive()
        {
            this.updateLift();
            if (this.lift != null) return this.lift.canReceive();
            return false;
        }

        @Override
        public int extractEnergy(final int maxExtract, final boolean simulate)
        {
            this.updateLift();
            if (this.lift != null) return this.lift.extractEnergy(maxExtract, simulate);
            return 0;
        }

        @Override
        public <T> LazyOptional<T> getCapability(final Capability<T> capability, final Direction facing)
        {
            return ThutCaps.ENERGY.orEmpty(capability, this.holder);
        }

        @Override
        public int getEnergyStored()
        {
            this.updateLift();
            if (this.lift != null) return this.lift.getEnergyStored();
            return 0;
        }

        @Override
        public int getMaxEnergyStored()
        {
            this.updateLift();
            if (this.lift != null) return this.lift.getMaxEnergyStored();
            return 0;
        }

        @Override
        public int receiveEnergy(final int maxReceive, final boolean simulate)
        {
            this.updateLift();
            if (this.lift != null) return this.lift.receiveEnergy(maxReceive, simulate);
            return 0;
        }

        private void updateLift()
        {
            if (this.tile.getLift() == null) this.lift = null;
            else this.lift = this.tile.getLift().getCapability(ThutCaps.ENERGY, null).orElse(null);
        }
    }

    private static final ResourceLocation ENERGY = new ResourceLocation(Reference.MOD_ID, "energy");

    @SubscribeEvent
    /** Adds the energy capability to the lift mobs. */
    public static void onEntityCapabilityAttach(final AttachCapabilitiesEvent<Entity> event)
    {
        if (event.getObject() instanceof EntityLift && !event.getCapabilities().containsKey(EnergyHandler.ENERGY))
            event.addCapability(EnergyHandler.ENERGY, new ProviderLift());
    }

    @SubscribeEvent
    /** Adds the energy capability to the lift controllers. */
    public static void onTileCapabilityAttach(final AttachCapabilitiesEvent<BlockEntity> event)
    {
        if (event.getObject() instanceof ControllerTile && !event.getCapabilities().containsKey(EnergyHandler.ENERGY))
            event.addCapability(EnergyHandler.ENERGY, new ProviderLiftController((ControllerTile) event.getObject()));
    }
}
