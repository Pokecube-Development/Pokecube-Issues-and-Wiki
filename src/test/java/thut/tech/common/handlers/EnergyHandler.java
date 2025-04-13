package thut.tech.common.handlers;

import net.minecraft.core.Direction;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.Nullable;
import thut.api.ThutCaps;
import thut.api.attachments.Energy;
import thut.tech.common.TechCore;
import thut.tech.common.blocks.lift.ControllerTile;
import thut.tech.common.entity.EntityLift;

@EventBusSubscriber(bus = Bus.MOD)
public class EnergyHandler
{
    public static class ProviderLift implements ICapabilityProvider<EntityLift, Direction, IEnergyStorage>
    {
        @Override
        public @Nullable IEnergyStorage getCapability(EntityLift object, Direction context)
        {
            if (!Energy.has(object, context))
            {
                Energy.set(object, new EnergyStorage(TechCore.config.maxLiftEnergy, TechCore.config.maxLiftEnergy));
            }
            return ThutCaps.getEnergy(object, context);
        }
    }

    public static class ProviderController implements ICapabilityProvider<ControllerTile, Direction, IEnergyStorage>
    {
        @Override
        public @Nullable IEnergyStorage getCapability(ControllerTile object, Direction context)
        {
            if (!Energy.has(object, context))
            {
                Energy.set(object, new ControllerEnergy(object));
            }
            return ThutCaps.getEnergy(object, context);
        }
    }

    /**
     * This is essentially a wrapper for the lift's energy storage capability. This allows interfacing with the lift's
     * energy via any of the connected controllers.
     */
    public static class ControllerEnergy extends EnergyStorage
    {
        final ControllerTile tile;
        IEnergyStorage lift = null;

        public ControllerEnergy(final ControllerTile tile)
        {
            super(0, 0);
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
            else this.lift = this.tile.getLift().getCapability(Capabilities.EnergyStorage.ENTITY, null);
        }
    }

    /** Adds the energy capability to the lift mobs. */
    @SubscribeEvent
    public static void onEntityCapabilityAttach(final RegisterCapabilitiesEvent event)
    {
        event.registerEntity(Capabilities.EnergyStorage.ENTITY, TechCore.LIFTTYPE.get(), new ProviderLift());
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, TechCore.CONTROLTYPE.get(),
                new ProviderController());
    }
}
