package pokecube.core.impl.capabilities;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Mob;
import pokecube.api.PokecubeAPI;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.ai.AIRoutine;
import pokecube.core.impl.capabilities.impl.PokemobSexed;

public class DefaultPokemob extends PokemobSexed implements IPokemob
{
    public DefaultPokemob()
    {
        for (final AIRoutine routine : AIRoutine.values()) this.setRoutineState(routine, routine.getDefault());
    }

    public DefaultPokemob(final Mob mob)
    {
        this();
        this.setEntity(mob);
    }

    @Override
    public void deserializeNBT(Provider provider, final CompoundTag tag)
    {
        try
        {
            super.deserializeNBT(provider, tag);
        }
        catch (final Exception e)
        {
            PokecubeAPI.LOGGER.error("Error Loading Pokemob", e);
        }
    }

    @Override
    public CompoundTag serializeNBT(Provider provider)
    {
        CompoundTag tag;
        try
        {
            tag = super.serializeNBT(provider);
        }
        catch (final Exception e)
        {
            PokecubeAPI.LOGGER.error("Error Saving Pokemob", e);
            tag = new CompoundTag();
        }
        return tag;
    }
}