package pokecube.compat.xaero;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import pokecube.api.data.PokedexEntry;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.IPokemob.FormeHolder;
import pokecube.api.entity.pokemob.PokemobCaps;

@OnlyIn(Dist.CLIENT)
public final class XaeroIconHandler
{
    private XaeroIconHandler()
    {}

    /**
     * Selects the same icon variant used by Pokecube's own GUIs. Xaero calls this method reflectively when it builds
     * an entity-radar icon, so this class does not require Xaero at compile time or runtime.
     */
    public static Object getVariant(final ResourceLocation texture, final EntityRenderer<?> renderer,
            final Entity entity)
    {
        final IPokemob pokemob = PokemobCaps.getPokemobFor(entity);
        if (pokemob == null) return texture;

        final PokedexEntry entry = pokemob.getPokedexEntry();
        if (entry == null) return texture;

        final boolean male = pokemob.getSexe() != IPokemob.FEMALE;
        final boolean shiny = pokemob.isShiny();
        final FormeHolder holder = pokemob.getCustomHolder();
        return holder == null ? entry.getIcon(male, shiny) : holder.getIcon(male, shiny, entry);
    }
}
