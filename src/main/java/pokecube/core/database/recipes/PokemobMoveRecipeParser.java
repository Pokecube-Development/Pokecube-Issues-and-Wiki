package pokecube.core.database.recipes;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonObject;

import net.minecraft.resources.ResourceLocation;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.IMoveAction;
import pokecube.api.moves.IMoveConstants;
import pokecube.api.moves.Move_Base;
import pokecube.api.utils.PokeType;
import pokecube.core.eventhandlers.MoveEventsHandler;
import pokecube.core.moves.MovesUtils;
import pokecube.core.recipes.MoveRecipes;
import pokecube.core.recipes.MoveRecipes.MoveRecipe;
import thut.api.maths.Vector3;

public class PokemobMoveRecipeParser implements IRecipeParser
{
    public static class MoveMatcher implements Predicate<String>
    {
        List<String> moves = Lists.newArrayList();

        String move = "";

        String type = "";

        int minPower = 0;
        int maxPower = 300;

        boolean contact = true;
        boolean ranged = true;

        @Override
        public boolean test(final String t)
        {
            if (!this.move.isEmpty()) return t.equals(this.move);
            if (!this.moves.isEmpty()) return this.moves.contains(t);

            final Move_Base move = MovesUtils.getMoveFromName(t);
            final PokeType ptype = PokeType.getType(this.type);
            if (ptype == null) return false;
            if (move == null) return false;
            if (move.move.type != ptype) return false;
            if (!this.contact && (move.getAttackCategory() & IMoveConstants.CATEGORY_CONTACT) > 0) return false;
            if (!this.ranged && (move.getAttackCategory() & IMoveConstants.CATEGORY_DISTANCE) > 0) return false;
            final int power = move.getPWR();
            return power >= this.minPower && power <= this.maxPower;
        }

    }

    private static class WrappedRecipeMove implements IMoveAction
    {
        public IMoveAction parent;
        public IMoveAction other;

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

    public static class RecipeAction implements IMoveAction
    {
        public final String name;

        public final MoveRecipe recipe;

        public RecipeAction(final String name, final MoveRecipe recipe)
        {
            this.name = name;
            this.recipe = recipe;
        }

        @Override
        public boolean applyEffect(final IPokemob user, final Vector3 location)
        {
            return this.recipe.applyEffect(user, location, this.getMoveName());
        }

        @Override
        public String getMoveName()
        {
            return this.name;
        }

    }

    public static class RecipeMove
    {
        public static final List<MoveRecipe> ALLRECIPES = Lists.newArrayList();

        public static final Map<ResourceLocation, MoveRecipe> CUSTOM = Maps.newHashMap();

        public static int uid = 0;

        public final MoveRecipe recipe;

        public final List<RecipeAction> actions = Lists.newArrayList();

        public ResourceLocation key = null;

        public RecipeMove(final MoveRecipe recipe)
        {
            this.recipe = recipe;
            RecipeMove.ALLRECIPES.add(recipe);

            for (final String s : MovesUtils.getKnownMoveNames()) if (this.recipe.match.test(s))
            {
                recipe.matchedMoves.add(s);
                this.actions.add(new RecipeAction(s, this.recipe));
            }
        }
    }

    public static void addOrMergeActions(IMoveAction action)
    {
        if (MoveEventsHandler.customActions.containsKey(action.getMoveName()))
        {
            final IMoveAction prev = MoveEventsHandler.customActions.get(action.getMoveName());
            if (prev instanceof WrappedRecipeMove edit)
            {
                edit.other = action;
                action = prev;
            }
            else action = new WrappedRecipeMove(MoveEventsHandler.customActions.get(action.getMoveName()), action);
        }
        MoveEventsHandler.customActions.put(action.getMoveName(), action);
    }

    public PokemobMoveRecipeParser()
    {}

    @Override
    public void manageRecipe(final JsonObject json) throws NullPointerException
    {
        json.addProperty("loading_from_other", true);
        final MoveRecipe recipe = MoveRecipes.SERIALIZER.get()
                .fromJson(new ResourceLocation("pokecube:move_recipe_" + RecipeMove.uid++), json);
        final RecipeMove loaded = new RecipeMove(recipe);
        for (final IMoveAction action : loaded.actions) PokemobMoveRecipeParser.addOrMergeActions(action);
    }

    @Override
    public void init()
    {
        RecipeMove.ALLRECIPES.clear();
        RecipeMove.uid = 0;
    }

}
