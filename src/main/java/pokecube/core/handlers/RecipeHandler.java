package pokecube.core.handlers;

import java.util.function.Function;

import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import pokecube.adventures.blocks.genetics.helper.recipe.RecipeExtract;
import pokecube.adventures.utils.RecipePokeAdv;
import pokecube.core.PokecubeCore;
import pokecube.core.items.berries.RecipeBrewBerries;
import pokecube.core.items.pokecubes.RecipePokeseals;
import pokecube.core.items.revive.RecipeRevive;
import pokecube.core.recipes.MoveRecipes;

public class RecipeHandler
{
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister
            .create(ForgeRegistries.RECIPE_SERIALIZERS, PokecubeCore.MODID);

    public static final RegistryObject<SimpleCraftingRecipeSerializer<RecipeRevive>> REVIVE = RecipeHandler.RECIPE_SERIALIZERS
            .register("revive", RecipeHandler.special(RecipeRevive::new));
    public static final RegistryObject<SimpleCraftingRecipeSerializer<RecipePokeseals>> APPLYSEAL = RecipeHandler.RECIPE_SERIALIZERS
            .register("seal_apply", RecipeHandler.special(RecipePokeseals::new));

    public static void init(final IEventBus bus)
    {
        RecipeHandler.RECIPE_SERIALIZERS.register(bus);
        BrewingRecipeRegistry.addRecipe(new RecipeBrewBerries());
        MoveRecipes.init();
    }

    private static <T extends CraftingRecipe> Supplier<SimpleCraftingRecipeSerializer<T>> special(SimpleCraftingRecipeSerializer.Factory<T> create)
    {
        return () -> new SimpleCraftingRecipeSerializer<>(create);
    }
}
