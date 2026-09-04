package pokecube.core.impl.capabilities;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import pokecube.api.PokecubeAPI;
import pokecube.api.data.PokedexEntry.InteractionLogic.Interaction;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.ai.AIRoutine;
import pokecube.api.entity.pokemob.ai.GeneralStates;
import pokecube.api.utils.TagNames;
import pokecube.core.impl.capabilities.impl.PokemobSexed;
import thut.api.Tracker;
import thut.api.attachments.Shearable;
import thut.api.item.ItemList;

import java.util.ArrayList;
import java.util.List;

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