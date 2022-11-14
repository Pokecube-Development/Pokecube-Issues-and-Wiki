package thut.tech.common.util;

import java.util.function.Function;
import java.util.function.Supplier;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleRecipeSerializer;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import thut.tech.Reference;
import thut.tech.common.items.RecipeReset;

public class RecipeSerializers
{
	public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(
			ForgeRegistries.RECIPE_SERIALIZERS, Reference.MOD_ID
	);

	public static final RegistryObject<SimpleRecipeSerializer<RecipeReset>> RECIPE_RESET_SERIALIZER = RecipeSerializers.RECIPE_SERIALIZERS.register(
			"resetlinker", RecipeSerializers.special(RecipeReset::new)
	);

	private static <T extends Recipe<?>> Supplier<SimpleRecipeSerializer<T>> special(final Function<ResourceLocation, T> create)
	{
		return () -> new SimpleRecipeSerializer<>(create);
	}
}