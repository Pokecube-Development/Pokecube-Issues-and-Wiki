package pokecube.api.entity.pokemob;

import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import pokecube.api.entity.IOngoingAffected;
import pokecube.core.utils.EntityTools;

public class PokemobCaps
{
    public static final Capability<IPokemob> POKEMOB_CAP = CapabilityManager.get(new CapabilityToken<>(){});
    public static final Capability<IOngoingAffected> AFFECTED_CAP = CapabilityManager.get(new CapabilityToken<>(){});

    public static IPokemob getPokemobFor(ICapabilityProvider entityIn)
    {
        if (entityIn == null) return null;
        if (entityIn instanceof Entity entity) entityIn = EntityTools.getCoreEntity(entity);
        final IPokemob pokemobHolder = entityIn.getCapability(PokemobCaps.POKEMOB_CAP, null).orElse(null);
        if (pokemobHolder == null && IPokemob.class.isInstance(entityIn)) return IPokemob.class.cast(entityIn);
        return pokemobHolder;
    }
}
