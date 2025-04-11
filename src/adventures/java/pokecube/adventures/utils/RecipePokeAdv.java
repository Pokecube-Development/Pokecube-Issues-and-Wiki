package pokecube.adventures.utils;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.neoforged.neoforge.registries.DeferredRegister;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.blocks.genetics.helper.recipe.*;
import pokecube.adventures.items.RecipeStatueCoat;

import java.util.function.Supplier;

public class RecipePokeAdv
{
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(
            BuiltInRegistries.RECIPE_SERIALIZER, PokecubeAdv.MODID);
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(
            BuiltInRegistries.RECIPE_TYPE, PokecubeAdv.MODID);

    public static final Supplier<RecipeSerializer<RecipeExtract>> EXTRACT;
    public static final Supplier<RecipeSerializer<RecipeSplice>> SPLICE;
    public static final Supplier<RecipeSerializer<RecipeClone>> REVIVE;
    public static final Supplier<RecipeSerializer<RecipeSelector>> SELECTOR;
    public static final Supplier<RecipeSerializer<RecipeStatueCoat>> STATUECOAT;

    public static final Supplier<RecipeType<RecipeExtract>> EXTRACT_TYPE;
    public static final Supplier<RecipeType<RecipeClone>> CLONE_TYPE;

    static
    {
        EXTRACT = RECIPE_SERIALIZERS.register("extracting", RecipeExtract.Serializer::new);
        SPLICE = RECIPE_SERIALIZERS.register("splicing", RecipePokeAdv.powered(RecipeSplice::new));
        REVIVE = RECIPE_SERIALIZERS.register("reviving", RecipeClone.Serializer::new);
        SELECTOR = RECIPE_SERIALIZERS.register("selectors", RecipePokeAdv.special(RecipeSelector::new));
        STATUECOAT = RECIPE_SERIALIZERS.register("statue_coating", RecipePokeAdv.special(RecipeStatueCoat::new));

        EXTRACT_TYPE = RECIPE_TYPES.register("extracting",
                () -> RecipeType.simple(ResourceLocation.parse("pokecube_adventures:extracting")));
        CLONE_TYPE = RECIPE_TYPES.register("reviving",
                () -> RecipeType.simple(ResourceLocation.parse("pokecube_adventures:reviving")));
    }

    private static <T extends CraftingRecipe> Supplier<SimpleCraftingRecipeSerializer<T>> special(
            SimpleCraftingRecipeSerializer.Factory<T> create)
    {
        return () -> new SimpleCraftingRecipeSerializer<>(create);
    }

    private static <T extends PoweredRecipe> Supplier<RecipeSerializer<T>> powered(Supplier<T> create)
    {
        return () -> new PoweredRecipe.Serializer<>(create);
    }
}
