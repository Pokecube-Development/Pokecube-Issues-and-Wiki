package pokecube.compat.jei;

import com.google.common.collect.Lists;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IModIngredientRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.blocks.genetics.helper.recipe.RecipeClone;
import pokecube.adventures.utils.RecipePokeAdv;
import pokecube.api.data.PokedexEntry;
import pokecube.api.data.PokedexEntry.EvolutionData;
import pokecube.api.data.PokedexEntry.InteractionLogic;
import pokecube.api.data.PokedexEntry.InteractionLogic.Interaction;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.compat.jei.categories.evolution.Evolution;
import pokecube.compat.jei.categories.interaction.InteractRecipe;
import pokecube.compat.jei.ingredients.Pokemob;
import pokecube.core.PokecubeCore;
import pokecube.core.database.Database;
import pokecube.core.handlers.RecipeHandler;
import pokecube.core.recipes.MoveRecipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@JeiPlugin
public class Compat implements IModPlugin
{
    private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(PokecubeCore.MODID, "jei");

    public static final RecipeType<Evolution> evoType;
    public static final RecipeType<InteractRecipe> interactType;
    public static final RecipeType<MoveRecipe> moveType;
    public static final RecipeType<RecipeClone> clonerType;

    static
    {
        evoType = RecipeType.create(PokecubeAdv.MODID, "pokemob_evolution", Evolution.class);
        interactType = RecipeType.create(PokecubeAdv.MODID, "pokemob_interaction", InteractRecipe.class);
        moveType = RecipeType.create(PokecubeAdv.MODID, "pokemob_move", MoveRecipe.class);
        clonerType = RecipeType.create(PokecubeAdv.MODID, "cloner", RecipeClone.class);
    }

    @Override
    public ResourceLocation getPluginUid()
    {
        return Compat.UID;
    }

    @Override
    public void registerIngredients(final IModIngredientRegistration registration)
    {
        registration.register(Pokemob.TYPE, Pokemob.getIngredients(), Pokemob.HELPER, Pokemob.RENDER, Pokemob.CODEC);
    }

    @Override
    public void registerCategories(final IRecipeCategoryRegistration registration)
    {
        // Makes sure the ingredients were initialized
        Pokemob.getIngredients();
        final IGuiHelper helper = registration.getJeiHelpers().getGuiHelper();
        // Then register the categories
        registration.addRecipeCategories(
                //@formatter:off
                new pokecube.compat.jei.categories.cloner.Category(helper),
                new pokecube.compat.jei.categories.evolution.Category(helper),
                new pokecube.compat.jei.categories.interaction.Category(helper),
                new pokecube.compat.jei.categories.move.Category(helper)
        );//@formatter:on

    }

    @Override
    public void registerRecipes(final IRecipeRegistration registration)
    {
        final List<Evolution> evos = Lists.newArrayList();
        final List<InteractRecipe> interactions = Lists.newArrayList();
        for (PokedexEntry entry : Database.getSortedFormes())
        {
            final PokedexEntry male = entry.getForGender(IPokemob.MALE);
            final PokedexEntry female = entry.getForGender(IPokemob.FEMALE);
            final boolean differs = male != female;

            if (differs)
            {
                final InteractionLogic l = entry.interactionLogic;
                final Map<ItemStack, Interaction> stacks = l.stackActions;
                final Map<ResourceLocation, Interaction> tags = l.tagActions;

                entry = male;
                if (entry.canEvolve())
                    for (final EvolutionData data : entry.evolutions) evos.add(new Evolution(entry, data));

                for (final ItemStack stack : stacks.keySet())
                {
                    final Interaction action = stacks.get(stack);
                    if (!action.male) continue;
                    interactions.add(new InteractRecipe(entry, action, stack, null));
                }
                for (final ResourceLocation tag : tags.keySet())
                {
                    final Interaction action = tags.get(tag);
                    if (!action.male) continue;
                    interactions.add(new InteractRecipe(entry, action, null, tag));
                }

                entry = female;
                if (entry.canEvolve())
                    for (final EvolutionData data : entry.evolutions) evos.add(new Evolution(entry, data));

                for (final ItemStack stack : stacks.keySet())
                {
                    final Interaction action = stacks.get(stack);
                    if (!action.female) continue;
                    interactions.add(new InteractRecipe(entry, action, stack, null));
                }
                for (final ResourceLocation tag : tags.keySet())
                {
                    final Interaction action = tags.get(tag);
                    if (!action.female) continue;
                    interactions.add(new InteractRecipe(entry, action, null, tag));
                }

            }
            else
            {
                if (entry.canEvolve())
                    for (final EvolutionData data : entry.evolutions) evos.add(new Evolution(entry, data));
                final InteractionLogic l = entry.interactionLogic;
                for (final ItemStack stack : l.stackActions.keySet())
                {
                    final Interaction action = l.stackActions.get(stack);
                    interactions.add(new InteractRecipe(entry, action, stack, null));
                }
                for (final ResourceLocation tag : l.tagActions.keySet())
                {
                    final Interaction action = l.tagActions.get(tag);
                    interactions.add(new InteractRecipe(entry, action, null, tag));
                }
            }
        }
        List<MoveRecipe> MOVES = new ArrayList<>();
        PokecubeCore.proxy.getWorld().getRecipeManager().getAllRecipesFor(RecipeHandler.MOVE_TYPE.get())
                .forEach(h -> MOVES.add(h.value()));
        List<RecipeClone> CLONING = new ArrayList<>();
        PokecubeCore.proxy.getWorld().getRecipeManager().getAllRecipesFor(RecipePokeAdv.CLONE_TYPE.get())
                .forEach(h -> CLONING.add(h.value()));

        registration.addRecipes(clonerType, CLONING);
        registration.addRecipes(evoType, evos);
        registration.addRecipes(interactType, interactions);
        registration.addRecipes(moveType, MOVES);
    }
}
