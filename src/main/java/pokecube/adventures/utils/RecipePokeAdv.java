package pokecube.adventures.utils;

import java.util.function.Supplier;

import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.blocks.genetics.helper.recipe.RecipeClone;
import pokecube.adventures.blocks.genetics.helper.recipe.RecipeExtract;
import pokecube.adventures.blocks.genetics.helper.recipe.RecipeSelector;
import pokecube.adventures.blocks.genetics.helper.recipe.RecipeSplice;
import pokecube.adventures.items.RecipeStatueCoat;

public class RecipePokeAdv
{
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister
            .create(ForgeRegistries.RECIPE_SERIALIZERS, PokecubeAdv.MODID);

    public static final RegistryObject<SimpleCraftingRecipeSerializer<RecipeExtract>> EXTRACT = RecipePokeAdv.RECIPE_SERIALIZERS
            .register("extracting", RecipePokeAdv.special(RecipeExtract::new));
    public static final RegistryObject<SimpleCraftingRecipeSerializer<RecipeSplice>> SPLICE = RecipePokeAdv.RECIPE_SERIALIZERS
            .register("splicing", RecipePokeAdv.special(RecipeSplice::new));
    public static final RegistryObject<SimpleCraftingRecipeSerializer<RecipeClone>> REVIVE = RecipePokeAdv.RECIPE_SERIALIZERS
            .register("reviving", RecipePokeAdv.special(RecipeClone::new));
    public static final RegistryObject<SimpleCraftingRecipeSerializer<RecipeSelector>> SELECTOR = RecipePokeAdv.RECIPE_SERIALIZERS
            .register("selectors", RecipePokeAdv.special(RecipeSelector::new));
    public static final RegistryObject<SimpleCraftingRecipeSerializer<RecipeStatueCoat>> STATUECOAT = RecipePokeAdv.RECIPE_SERIALIZERS
            .register("statue_coating", RecipePokeAdv.special(RecipeStatueCoat::new));

    private static <T extends CraftingRecipe> Supplier<SimpleCraftingRecipeSerializer<T>> special(SimpleCraftingRecipeSerializer.Factory<T> create) {
        return () -> new SimpleCraftingRecipeSerializer<>(create);
    }
}
