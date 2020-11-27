package pokecube.adventures.utils;

import java.util.function.Function;
import java.util.function.Supplier;

import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipeSerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.blocks.genetics.helper.recipe.RecipeClone;
import pokecube.adventures.blocks.genetics.helper.recipe.RecipeExtract;
import pokecube.adventures.blocks.genetics.helper.recipe.RecipeSelector;
import pokecube.adventures.blocks.genetics.helper.recipe.RecipeSplice;

public class RecipePokeAdv
{
    public static final DeferredRegister<IRecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(
            ForgeRegistries.RECIPE_SERIALIZERS, PokecubeAdv.MODID);

    public static final RegistryObject<SpecialRecipeSerializer<RecipeExtract>> EXTRACT = RecipePokeAdv.RECIPE_SERIALIZERS
            .register("extracting", RecipePokeAdv.special(RecipeExtract::new));
    public static final RegistryObject<SpecialRecipeSerializer<RecipeSplice>>  SPLICE  = RecipePokeAdv.RECIPE_SERIALIZERS
            .register("splicing", RecipePokeAdv.special(RecipeSplice::new));
    public static final RegistryObject<SpecialRecipeSerializer<RecipeClone>>   REVIVE  = RecipePokeAdv.RECIPE_SERIALIZERS
            .register("reviving", RecipePokeAdv.special(RecipeClone::new));
    public static final RegistryObject<SpecialRecipeSerializer<RecipeSelector>>   SELECTOR  = RecipePokeAdv.RECIPE_SERIALIZERS
            .register("selectors", RecipePokeAdv.special(RecipeSelector::new));

    private static <T extends IRecipe<?>> Supplier<SpecialRecipeSerializer<T>> special(
            final Function<ResourceLocation, T> create)
    {
        return () -> new SpecialRecipeSerializer<>(create);
    }
}
