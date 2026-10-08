package pokecube.mobs.abilities.simple;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.world.item.ItemStack;
import pokecube.adventures.PokecubeAdv;
import pokecube.api.data.PokedexEntry;
import pokecube.api.data.abilities.Ability;
import pokecube.api.data.abilities.AbilityProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.utils.PokeType;
import pokecube.core.database.Database;

@AbilityProvider(name = "multitype")
public class Multitype extends Ability
{
    Map<PokeType, PokedexEntry> formes = Maps.newHashMap();

    @Override
    public void onUpdate(final IPokemob mob)
    {
        final PokedexEntry entry = mob.getPokedexEntry();
        final ItemStack held = mob.getHeldItem();
        final PokeType type = PokecubeAdv.BADGEINV.getOrDefault(held.getItem(), PokeType.getType("normal"));
        PokedexEntry key = formes.computeIfAbsent(type, t -> {
            PokedexEntry _entry = Database.getEntry(entry.getBaseName() + "_" + type.name);
            if (_entry == null)
            {
                _entry = entry;
            }
            return _entry;
        });
        mob.setPokedexEntry(key);
    }
}
