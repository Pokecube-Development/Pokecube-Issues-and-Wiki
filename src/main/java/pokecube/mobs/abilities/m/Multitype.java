package pokecube.mobs.abilities.m;

import net.minecraft.world.item.ItemStack;
import pokecube.adventures.PokecubeAdv;
import pokecube.api.data.PokedexEntry;
import pokecube.api.data.abilities.Ability;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.core.database.Database;
import pokecube.core.utils.PokeType;

public class Multitype extends Ability
{

    @Override
    public void onUpdate(final IPokemob mob)
    {
        final PokedexEntry entry = mob.getPokedexEntry();
        final ItemStack held = mob.getHeldItem();
        final PokeType type = PokecubeAdv.BADGEINV.get(held.getItem());
        if (type != null)
        {
            final PokedexEntry forme = Database.getEntry(entry.getBaseName() + type);
            if (forme != null)
            {
                mob.setPokedexEntry(forme);
                return;
            }
        }
        if (entry.getBaseForme() != null)
        {
            mob.setPokedexEntry(entry.getBaseForme());
            return;
        }
    }
}
