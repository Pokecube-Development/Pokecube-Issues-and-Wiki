package pokecube.gimmicks.vanilla_pokemobs;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Mob;
import pokecube.api.PokecubeAPI;
import pokecube.api.entity.pokemob.ai.AIRoutine;
import pokecube.core.impl.capabilities.impl.PokemobSaves;

public class VanillaPokemob extends PokemobSaves
{

    public VanillaPokemob()
    {
        super();
        for (final AIRoutine routine : AIRoutine.values()) this.setRoutineState(routine, routine.getDefault());
    }

    public VanillaPokemob(final Mob mob)
    {
        this();
        this.setEntity(mob);
    }

    @Override
    public void deserializeNBT(Provider provider, CompoundTag tag)
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

    // Cache of raw max hp, set to whatever the initial max hp of the mob was
    float rawMaxHP = -1;
    /**
     * Here we ensure that the vanilla mobs have at least their original HP stat.
     */
    @Override
    public int getMaxHPStat()
    {
        if (rawMaxHP < 0) rawMaxHP = getEntity().getMaxHealth();
        return (int) Math.max(super.getMaxHPStat(), rawMaxHP);
    }
}
