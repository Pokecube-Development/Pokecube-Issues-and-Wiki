package pokecube.core.items.berries;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.brewing.IBrewingRecipe;
import pokecube.core.PokecubeItems;

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
        final CompoundNBT tag = input.getTag();
        if (tag != null && tag.contains("pokebloc")) return true;
        return input.getItem() == Items.GLASS_BOTTLE;
    }

    private ItemStack makeOutput(ItemStack input, ItemStack ingredient)
    {

        final CompoundNBT pokebloc = new CompoundNBT();
        final ItemStack stack = PokecubeItems.getStack("revive");

        if (ingredient.getItem() instanceof ItemBerry)
        {
            final ItemBerry berry = (ItemBerry) ingredient.getItem();
            int[] flav = berry.type.flavours;
            int[] old = null;
            if (input.hasTag() && input.getTag().contains("pokebloc")) old = input.getTag().getIntArray("pokebloc");
            if (flav != null)
            {
                flav = flav.clone();
                if (old != null) for (int i = 0; i < Math.min(old.length, flav.length); i++)
                    flav[i] += old[i];
                pokebloc.putIntArray("pokebloc", flav);
                final CompoundNBT tag = input.hasTag() ? input.getTag().copy() : new CompoundNBT();
                tag.put("pokebloc", pokebloc);
                stack.setTag(tag);
            }
        }
        return stack;
    }

}
