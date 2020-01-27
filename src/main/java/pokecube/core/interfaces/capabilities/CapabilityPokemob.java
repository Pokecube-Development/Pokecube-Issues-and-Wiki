package pokecube.core.interfaces.capabilities;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import pokecube.core.interfaces.IPokemob;

public class CapabilityPokemob
{
    public static class Storage implements Capability.IStorage<IPokemob>
    {

        @Override
        public void readNBT(final Capability<IPokemob> capability, final IPokemob instance, final Direction side,
                final INBT nbt)
        {
            if (instance instanceof DefaultPokemob && nbt instanceof CompoundNBT) ((DefaultPokemob) instance).read(
                    (CompoundNBT) nbt);
        }

        @Override
        public INBT writeNBT(final Capability<IPokemob> capability, final IPokemob instance, final Direction side)
        {
            if (instance instanceof DefaultPokemob) return ((DefaultPokemob) instance).write();
            return null;
        }
    }

    @CapabilityInject(IPokemob.class)
    public static final Capability<IPokemob> POKEMOB_CAP = null;

    public static IPokemob getPokemobFor(final ICapabilityProvider entityIn)
    {
        if (entityIn == null) return null;
        final IPokemob pokemobHolder = entityIn.getCapability(CapabilityPokemob.POKEMOB_CAP, null).orElse(null);
        if (pokemobHolder != null) return pokemobHolder;
        else if (IPokemob.class.isInstance(entityIn)) return IPokemob.class.cast(entityIn);
        return pokemobHolder;
    }
}
