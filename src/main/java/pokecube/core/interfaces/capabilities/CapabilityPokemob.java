package pokecube.core.interfaces.capabilities;

import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.utils.EntityTools;

public class CapabilityPokemob
{
    public static IPokemob getPokemobFor(ICapabilityProvider entityIn)
    {
        if (entityIn == null) return null;
        if (entityIn instanceof Entity) entityIn = EntityTools.getCoreEntity((Entity) entityIn);
        final IPokemob pokemobHolder = entityIn.getCapability(PokemobCaps.POKEMOB_CAP, null).orElse(null);
        if (pokemobHolder == null && IPokemob.class.isInstance(entityIn)) return IPokemob.class.cast(entityIn);
        return pokemobHolder;
    }
}
