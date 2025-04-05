package pokecube.core.recipes;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.brewing.IBrewingRecipe;
import pokecube.core.PokecubeItems;
import pokecube.core.items.berries.BerryManager;
import pokecube.core.items.berries.ItemBerry;
import pokecube.core.items.berries.PokeblocData;

public class RecipeBrewBerries implements IBrewingRecipe
{

    @Override
    public ItemStack getOutput(ItemStack input, ItemStack ingredient)
    {
        if (this.isIngredient(ingredient)) return this.makeOutput(input, ingredient);
        return ItemStack.EMPTY;
    }

    @Override
    public boolean isIngredient(ItemStack ingredient)
    {
        return ingredient.getItem() instanceof ItemBerry;
    }

    @Override
    public boolean isInput(ItemStack input)
    {
        if (input.has(BerryManager.TASTE_DATA)) return true;
        return input.getItem() == Items.GLASS_BOTTLE;
    }

    private ItemStack makeOutput(ItemStack input, ItemStack ingredient)
    {
        final ItemStack stack = PokecubeItems.getStack("revive");
        if (ingredient.getItem() instanceof ItemBerry berry)
        {
            int[] flav = berry.type.flavours;
            int[] old = null;
            PokeblocData data = input.get(BerryManager.TASTE_DATA);
            if (data != null) old = data.flavours().toIntArray();
            if (flav != null)
            {
                flav = flav.clone();
                if (old != null) for (int i = 0; i < Math.min(old.length, flav.length); i++) flav[i] += old[i];
                stack.set(BerryManager.TASTE_DATA, new PokeblocData(flav));
            }
        }
        return stack;
    }

}
