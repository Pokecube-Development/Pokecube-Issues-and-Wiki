package pokecube.adventures.blocks.genetics.helper.recipe;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.ItemCraftedEvent;
import pokecube.adventures.blocks.genetics.helper.SelectorImpl;
import pokecube.adventures.blocks.genetics.helper.SelectorImpl.SelectorValue;
import thut.core.common.ThutCore;

public class RecipeHandlers
{
    //    private static final String DNADESTRUCT = "dna";
    //    private static final String SELECTORDESTRUCT = "selector";
    //    public static class SelectorRecipeParser implements IRecipeParser
    //    {
    //        @Override
    //        public void manageRecipe(final JsonObject json) throws NullPointerException
    //        {
    //            final XMLRecipe recipe = this.fromJson(json);
    //            final List<Ingredient> inputs = XMLRecipeHandler.getInputItems(json);
    //            if (inputs.size() != 1) throw new NullPointerException("Wrong number of stacks for " + recipe);
    //            final Ingredient stack = inputs.getFirst();
    //            if (stack.isEmpty()) throw new NullPointerException("Invalid stack for " + recipe);
    //            final float dna = Float.parseFloat(recipe.values.get(RecipeHandlers.DNADESTRUCT));
    //            final float select = Float.parseFloat(recipe.values.get(RecipeHandlers.SELECTORDESTRUCT));
    //            final SelectorValue value = new SelectorValue(select, dna);
    //            RecipeSelector.addSelector(stack, value);
    //        }
    //
    //        @Override
    //        public void init()
    //        {
    //            RecipeSelector.clear();
    //        }
    //    }

    public static void init()
    {
        RecipeSelector.clear();
        RecipeSelector.addSelector(Ingredient.of(Items.NETHER_STAR), new SelectorValue(0.25f, 0.0f));
        ThutCore.FORGE_BUS.addListener(RecipeHandlers::onCrafted);
    }

    private static void onCrafted(final ItemCraftedEvent event)
    {
        final ItemStack result = event.getCrafting();
        if (result.has(SelectorImpl.VALUE_STORE) && isBookCopy(event.getInventory()))
            result.remove(SelectorImpl.VALUE_STORE);
    }

    private static boolean isBookCopy(final Container inventory)
    {
        int writtenBooks = 0;
        int writableBooks = 0;
        for (int i = 0; i < inventory.getContainerSize(); i++)
        {
            final ItemStack stack = inventory.getItem(i);
            if (stack.isEmpty()) continue;
            if (stack.is(Items.WRITTEN_BOOK)) writtenBooks++;
            else if (stack.is(Items.WRITABLE_BOOK)) writableBooks += stack.getCount();
            else return false;
        }
        return writtenBooks == 1 && writableBooks > 0;
    }
}
