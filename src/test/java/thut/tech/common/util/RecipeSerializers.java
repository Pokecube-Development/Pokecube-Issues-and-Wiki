package thut.tech.common.util;

import java.util.function.Supplier;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.neoforged.neoforge.registries.DeferredRegister;
import thut.tech.Reference;
import thut.tech.common.items.RecipeReset;

public class RecipeSerializers
{
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister
            .create(BuiltInRegistries.RECIPE_SERIALIZER, Reference.MOD_ID);

    public static final Supplier<SimpleCraftingRecipeSerializer<RecipeReset>> RECIPE_RESET_SERIALIZER = RecipeSerializers.RECIPE_SERIALIZERS
            .register("resetlinker", RecipeSerializers.special(RecipeReset::new));

    private static <T extends CraftingRecipe> Supplier<SimpleCraftingRecipeSerializer<T>> special(
            final SimpleCraftingRecipeSerializer.Factory<T> create)
    {
        return () -> new SimpleCraftingRecipeSerializer<>(create);
    }
}