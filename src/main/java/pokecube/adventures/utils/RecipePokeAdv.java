package pokecube.adventures.utils;

import java.util.function.Function;
import java.util.function.Supplier;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleRecipeSerializer;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.blocks.genetics.helper.recipe.RecipeClone;
import pokecube.adventures.blocks.genetics.helper.recipe.RecipeExtract;
import pokecube.adventures.blocks.genetics.helper.recipe.RecipeSelector;
import pokecube.adventures.blocks.genetics.helper.recipe.RecipeSplice;

public class RecipePokeAdv
{
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(
            ForgeRegistries.RECIPE_SERIALIZERS, PokecubeAdv.MODID);

    public static final RegistryObject<SimpleRecipeSerializer<RecipeExtract>> EXTRACT = RecipePokeAdv.RECIPE_SERIALIZERS
            .register("extracting", RecipePokeAdv.special(RecipeExtract::new));
    public static final RegistryObject<SimpleRecipeSerializer<RecipeSplice>>  SPLICE  = RecipePokeAdv.RECIPE_SERIALIZERS
            .register("splicing", RecipePokeAdv.special(RecipeSplice::new));
    public static final RegistryObject<SimpleRecipeSerializer<RecipeClone>>   REVIVE  = RecipePokeAdv.RECIPE_SERIALIZERS
            .register("reviving", RecipePokeAdv.special(RecipeClone::new));
    public static final RegistryObject<SimpleRecipeSerializer<RecipeSelector>>   SELECTOR  = RecipePokeAdv.RECIPE_SERIALIZERS
            .register("selectors", RecipePokeAdv.special(RecipeSelector::new));

    private static <T extends Recipe<?>> Supplier<SimpleRecipeSerializer<T>> special(
            final Function<ResourceLocation, T> create)
    {
        return () -> new SimpleRecipeSerializer<>(create);
    }
}
