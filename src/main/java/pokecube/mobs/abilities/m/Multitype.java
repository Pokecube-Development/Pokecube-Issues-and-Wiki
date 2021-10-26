package pokecube.mobs.abilities.m;

import net.minecraft.world.item.ItemStack;
import pokecube.adventures.PokecubeAdv;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.abilities.Ability;
import pokecube.core.interfaces.IPokemob;
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
