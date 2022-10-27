package pokecube.mobs.abilities.simple;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import pokecube.adventures.PokecubeAdv;
import pokecube.api.data.PokedexEntry;
import pokecube.api.data.abilities.Ability;
import pokecube.api.data.abilities.AbilityProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.IPokemob.FormeHolder;
import pokecube.api.utils.PokeType;
import pokecube.core.PokecubeItems;
import pokecube.core.database.Database;

@AbilityProvider(name = "multitype")
public class Multitype extends Ability
{
    Map<PokeType, ResourceLocation> formes = Maps.newHashMap();

    @Override
    public void onUpdate(final IPokemob mob)
    {
        final PokedexEntry entry = mob.getPokedexEntry();
        final ItemStack held = mob.getHeldItem();
        final PokeType type = PokecubeAdv.BADGEINV.get(held.getItem());
        if (type != null)
        {
            ResourceLocation key = formes.computeIfAbsent(type, t -> {
                return PokecubeItems.toPokecubeResource(entry.getBaseName() + "_" + type.name);
            });
            FormeHolder holder = Database.formeHolders.get(key);
            mob.setCustomHolder(holder);
        }
        if (entry.getBaseForme() != null)
        {
            mob.setCustomHolder(entry.getModel(mob.getSexe()));
            return;
        }
    }
}
