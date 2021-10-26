package pokecube.core.moves.zmoves;

import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public class CapabilityZMove
{
    public static class Impl implements ZPower
    {
    }

    public static class Storage implements Capability.IStorage<ZPower>
    {
        @Override
        public INBT writeNBT(final Capability<ZPower> capability, final ZPower instance, final Direction side)
        {
            return null;
        }

        @Override
        public void readNBT(final Capability<ZPower> capability, final ZPower instance, final Direction side,
                final INBT nbt)
        {
        }
    }

    private static Impl def = new Impl();

    public static ZPower get(final ICapabilityProvider providerIn)
    {
        return providerIn.getCapability(CapabilityZMove.CAPABILITY).orElse(CapabilityZMove.def);
    }

    @CapabilityInject(ZPower.class)
    public static final Capability<ZPower> CAPABILITY = null;
}
