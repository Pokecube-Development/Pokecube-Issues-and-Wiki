package pokecube.compat.jei.categories.interaction;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import pokecube.compat.jei.ingredients.Pokemob;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.PokedexEntry.InteractionLogic.Interaction;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.FormeHolder;

public class InteractRecipe
{
    final Pokemob      from;
    final Pokemob      to;
    final PokedexEntry entry;
    final Interaction  interaction;

    ItemStack key = ItemStack.EMPTY;

    ResourceLocation tag;

    public InteractRecipe(PokedexEntry entry, final Interaction interaction, final ItemStack key,
            final ResourceLocation tag)
    {
        this.entry = entry;
        this.interaction = interaction;
        Pokemob _from = Pokemob.ALLMAP.get(entry);
        FormeHolder holder = entry.getModel(IPokemob.MALE);
        if (interaction.male != interaction.female) holder = entry.getModel(interaction.male ? IPokemob.MALE
                : IPokemob.FEMALE);
        if (holder != null && Pokemob.FORMMAP.containsKey(holder)) _from = Pokemob.FORMMAP.get(holder);
        this.from = _from;
        Pokemob _to = null;
        entry = interaction.forme;
        if (entry != null)
        {
            _to = Pokemob.ALLMAP.get(entry);
            holder = entry.getModel(IPokemob.MALE);
            if (interaction.male != interaction.female) holder = entry.getModel(interaction.male ? IPokemob.MALE
                    : IPokemob.FEMALE);
            if (holder != null && Pokemob.FORMMAP.containsKey(holder)) _to = Pokemob.FORMMAP.get(holder);
        }
        this.to = _to;
        if (key != null) this.key = key;
        this.tag = tag;
    }
}
