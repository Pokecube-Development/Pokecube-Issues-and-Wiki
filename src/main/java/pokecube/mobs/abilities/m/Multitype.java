package pokecube.mobs.abilities.m;

import pokecube.core.database.PokedexEntry;
import pokecube.core.database.abilities.Ability;
import pokecube.core.interfaces.IPokemob;

public class Multitype extends Ability
{
    @Override
    public void onUpdate(IPokemob mob)
    {
        final PokedexEntry entry = mob.getPokedexEntry();
        // TODO implement this when badges are in
        // ItemStack held = mob.getHeldItem();
        // if (ItemBadge.isBadge(held))
        // {
        // ItemBadge badge = (ItemBadge) held.getItem();
        // PokeType type = badge.type;
        // PokedexEntry forme = Database.getEntry(entry.getBaseName() + type);
        // if (forme != null)
        // {
        // mob.setPokedexEntry(forme);
        // return;
        // }
        // }
        if (entry.getBaseForme() != null)
        {
            mob.setPokedexEntry(entry.getBaseForme());
            return;
        }
    }
}
