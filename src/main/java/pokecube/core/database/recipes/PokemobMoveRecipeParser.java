package pokecube.core.database.recipes;

import javax.xml.namespace.QName;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapelessRecipe;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import pokecube.core.database.recipes.XMLRecipeHandler.XMLRecipe;
import pokecube.core.database.recipes.XMLRecipeHandler.XMLRecipeInput;
import pokecube.core.handlers.events.MoveEventsHandler;
import pokecube.core.interfaces.IMoveAction;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.utils.Tools;
import thut.api.maths.Vector3;

public class PokemobMoveRecipeParser implements IRecipeParser
{

    private static final QName MOVENAME   = new QName("move");
    private static final QName HUNGERCOST = new QName("cost");

    public static class WrappedRecipeMove implements IMoveAction
    {
        public final IMoveAction parent;
        public final IMoveAction other;

        public WrappedRecipeMove(final IMoveAction parent, final IMoveAction other)
        {
            this.parent = parent;
            this.other = other;
        }

        @Override
        public boolean applyEffect(final IPokemob user, final Vector3 location)
        {
            // Only applies other action if parent action failed.
            return this.parent.applyEffect(user, location) || this.other.applyEffect(user, location);
        }

        @Override
        public String getMoveName()
        {
            return this.parent.getMoveName();
        }

        @Override
        public void init()
        {
            this.parent.init();
            this.other.init();
        }

    }

    public static class RecipeMove implements IMoveAction
    {
        public final String          name;
        public final ShapelessRecipe recipe;
        public final int             hungerCost;

        public RecipeMove(final XMLRecipe recipe)
        {
            this.name = recipe.values.get(PokemobMoveRecipeParser.MOVENAME);
            this.hungerCost = Integer.parseInt(recipe.values.get(PokemobMoveRecipeParser.HUNGERCOST));
            final ItemStack recipeOutputIn = Tools.getStack(recipe.output.getValues());
            final NonNullList<Ingredient> recipeItemsIn = NonNullList.create();

            for (final XMLRecipeInput value : recipe.inputs)
                // Tag
                if (value.id.startsWith("#"))
                {
                    final ResourceLocation id = new ResourceLocation(value.id.replaceFirst("#", ""));
                    final Tag<Item> tag = ItemTags.getCollection().getOrCreate(id);
                    recipeItemsIn.add(Ingredient.fromTag(tag));
                }
                else recipeItemsIn.add(Ingredient.fromStacks(Tools.getStack(value.getValues())));

            this.recipe = new ShapelessRecipe(new ResourceLocation("pokecube:loaded_" + this.name), "pokecube_mobes",
                    recipeOutputIn, recipeItemsIn);
        }

        @Override
        public boolean applyEffect(final IPokemob user, final Vector3 location)
        {
            // FIXME world crafting
            return this.attemptCraft(user, location) || this.attemptWorldCraft(user, location);
        }

        public boolean attemptWorldCraft(final IPokemob user, final Vector3 location)
        {
            // This should look at the block hit, and attempt to craft that into
            // a shapeless recipe.
            return false;
        }

        public boolean attemptCraft(final IPokemob attacker, final Vector3 location)
        {
            // This should look for items near the location, and try to stuff
            // them into a shapeless recipe.
            return false;
        }

        @Override
        public String getMoveName()
        {
            return this.name;
        }

    }

    public PokemobMoveRecipeParser()
    {
    }

    @Override
    public void manageRecipe(final XMLRecipe recipe) throws NullPointerException
    {
        IMoveAction action = new RecipeMove(recipe);
        if (MoveEventsHandler.customActions.containsKey(action.getMoveName())) action = new WrappedRecipeMove(
                MoveEventsHandler.customActions.get(action.getMoveName()), action);
        MoveEventsHandler.customActions.put(action.getMoveName(), action);
    }

}
