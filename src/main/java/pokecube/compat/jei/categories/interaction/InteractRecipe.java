package pokecube.compat.jei.categories.interaction;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import pokecube.compat.jei.ingredients.Pokemob;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.PokedexEntry.InteractionLogic.Interaction;

public class InteractRecipe
{
    final Pokemob      from;
    final Pokemob      to;
    final PokedexEntry entry;
    final Interaction  interaction;

    ItemStack key = ItemStack.EMPTY;

    ResourceLocation tag;

    public InteractRecipe(final PokedexEntry entry, final Interaction interaction, final ItemStack key,
            final ResourceLocation tag)
    {
        this.entry = entry;
        this.interaction = interaction;
        this.from = Pokemob.ALLMAP.get(this.entry);
        if (interaction.forme != null) this.to = Pokemob.ALLMAP.get(interaction.forme);
        else this.to = null;
        if (key != null) this.key = key;
        this.tag = tag;
    }
}
