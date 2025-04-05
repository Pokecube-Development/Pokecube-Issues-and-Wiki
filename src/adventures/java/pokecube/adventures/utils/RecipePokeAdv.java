package pokecube.adventures.utils;

import java.util.function.Supplier;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.neoforged.neoforge.registries.DeferredRegister;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.blocks.genetics.helper.recipe.PoweredRecipe;
import pokecube.adventures.blocks.genetics.helper.recipe.RecipeClone;
import pokecube.adventures.blocks.genetics.helper.recipe.RecipeExtract;
import pokecube.adventures.blocks.genetics.helper.recipe.RecipeSelector;
import pokecube.adventures.blocks.genetics.helper.recipe.RecipeSplice;
import pokecube.adventures.items.RecipeStatueCoat;

public class RecipePokeAdv
{
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister
            .create(BuiltInRegistries.RECIPE_SERIALIZER, PokecubeAdv.MODID);

    public static final Supplier<RecipeSerializer<RecipeExtract>> EXTRACT = RecipePokeAdv.RECIPE_SERIALIZERS
            .register("extracting", RecipePokeAdv.powered(RecipeExtract::new));
    public static final Supplier<RecipeSerializer<RecipeSplice>> SPLICE = RecipePokeAdv.RECIPE_SERIALIZERS
            .register("splicing", RecipePokeAdv.powered(RecipeSplice::new));
    public static final Supplier<RecipeSerializer<RecipeClone>> REVIVE = RecipePokeAdv.RECIPE_SERIALIZERS
            .register("reviving", RecipePokeAdv.powered(RecipeClone::new));
    public static final Supplier<RecipeSerializer<RecipeSelector>> SELECTOR = RecipePokeAdv.RECIPE_SERIALIZERS
            .register("selectors", RecipePokeAdv.special(RecipeSelector::new));
    public static final Supplier<RecipeSerializer<RecipeStatueCoat>> STATUECOAT = RecipePokeAdv.RECIPE_SERIALIZERS
            .register("statue_coating", RecipePokeAdv.special(RecipeStatueCoat::new));

    private static <T extends CraftingRecipe> Supplier<SimpleCraftingRecipeSerializer<T>> special(SimpleCraftingRecipeSerializer.Factory<T> create) {
        return () -> new SimpleCraftingRecipeSerializer<>(create);
    }
    
    private static <T extends PoweredRecipe> Supplier<RecipeSerializer<T>> powered(Supplier<T> create){
        return ()->new PoweredRecipe.Serializer<>(create);
    }
}
