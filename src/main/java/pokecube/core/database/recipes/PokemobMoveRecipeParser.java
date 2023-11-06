package pokecube.core.database.recipes;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonObject;

import net.minecraft.resources.ResourceLocation;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.MoveEntry;
import pokecube.api.moves.utils.IMoveConstants.ContactCategory;
import pokecube.api.moves.utils.IMoveWorldEffect;
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

            final MoveEntry move = MovesUtils.getMove(t);
            final PokeType ptype = PokeType.getType(this.type);
            if (ptype == null) return false;
            if (move == null) return false;
            if (move.type != ptype) return false;
            if (!this.contact && move.getAttackCategory() == ContactCategory.CONTACT) return false;
            if (!this.ranged && move.getAttackCategory() == ContactCategory.RANGED) return false;
            final int power = move.getPWR();
            return power >= this.minPower && power <= this.maxPower;
        }

    }

    public static class RecipeAction implements IMoveWorldEffect
    {
        public final String name;

        public final MoveRecipe recipe;

        public RecipeAction(final String name, final MoveRecipe recipe)
        {
            this.name = name;
            this.recipe = recipe;
        }

        @Override
        public boolean applyOutOfCombat(final IPokemob user, final Vector3 location)
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

    public PokemobMoveRecipeParser()
    {}

    @Override
    public void manageRecipe(final JsonObject json) throws NullPointerException
    {
        json.addProperty("loading_from_other", true);
        final MoveRecipe recipe = MoveRecipes.SERIALIZER.get()
                .fromJson(new ResourceLocation("pokecube:move_recipe_" + RecipeMove.uid++), json);
        final RecipeMove loaded = new RecipeMove(recipe);
        for (final IMoveWorldEffect action : loaded.actions) MoveEventsHandler.addOrMergeActions(action);
    }

    @Override
    public void init()
    {
        RecipeMove.ALLRECIPES.clear();
        RecipeMove.uid = 0;
    }

}
