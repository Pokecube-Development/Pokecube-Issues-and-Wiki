package pokecube.adventures.blocks.genetics.helper.recipe;

import net.neoforged.neoforge.event.entity.player.PlayerEvent.ItemCraftedEvent;
import pokecube.adventures.PokecubeAdv;
import pokecube.core.PokecubeCore;
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
        ThutCore.FORGE_BUS.addListener(RecipeHandlers::onCrafted);
    }

    private static void onCrafted(final ItemCraftedEvent event)
    {
        if(PokecubeCore.getConfig().debug_data) Thread.dumpStack();
        //        if (!(event.getInventory() instanceof CraftingContainer inv)) return;
        //        final BookCloningRecipe test = new BookCloningRecipe(CraftingBookCategory.MISC);
        //
        //        if (!test.matches(inv, event.getEntity().level())) return;
        //        final SelectorValue value = ClonerHelper.getSelectorValue(event.getCrafting());
        //        if (value == SelectorImpl.defaultSelector) return;
        //        event.getCrafting().getTag().remove(ClonerHelper.SELECTORTAG);
    }
}
