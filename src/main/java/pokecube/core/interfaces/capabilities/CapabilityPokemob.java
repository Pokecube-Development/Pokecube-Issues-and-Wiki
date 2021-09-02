package pokecube.core.interfaces.capabilities;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.utils.EntityTools;

public class CapabilityPokemob
{
    public static class Storage implements Capability.IStorage<IPokemob>
    {

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        public void readNBT(final Capability<IPokemob> capability, final IPokemob instance, final Direction side,
                final INBT nbt)
        {
            if (instance instanceof INBTSerializable<?>) ((INBTSerializable) instance).deserializeNBT(nbt);
        }

        @Override
        public INBT writeNBT(final Capability<IPokemob> capability, final IPokemob instance, final Direction side)
        {
            if (instance instanceof INBTSerializable<?>) return ((INBTSerializable<?>) instance).serializeNBT();
            return null;
        }
    }

    public static IPokemob getPokemobFor(ICapabilityProvider entityIn)
    {
        if (entityIn == null) return null;
        if (entityIn instanceof Entity) entityIn = EntityTools.getCoreEntity((Entity) entityIn);
        final IPokemob pokemobHolder = entityIn.getCapability(PokemobCaps.POKEMOB_CAP, null).orElse(null);
        if (pokemobHolder == null && IPokemob.class.isInstance(entityIn)) return IPokemob.class.cast(entityIn);
        return pokemobHolder;
    }
}
