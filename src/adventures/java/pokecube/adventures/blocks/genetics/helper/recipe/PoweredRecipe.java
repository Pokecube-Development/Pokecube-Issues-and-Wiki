package pokecube.adventures.blocks.genetics.helper.recipe;

import java.util.function.Function;
import java.util.function.Supplier;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import pokecube.adventures.blocks.genetics.helper.crafting.PoweredCraftingInventory;

public abstract class PoweredRecipe implements IPoweredRecipe, Recipe<PoweredCraftingInventory>
{
    public static class Serializer<T extends PoweredRecipe> implements RecipeSerializer<T>
    {
        private final MapCodec<T> codec;
        private final StreamCodec<RegistryFriendlyByteBuf, T> streamCodec;

        public Serializer(Supplier<T> constructor)
        {
            this.codec = MapCodec.unit(constructor);
            this.streamCodec = StreamCodec.unit(constructor.get());
        }

        @Override
        public MapCodec<T> codec()
        {
            return codec;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, T> streamCodec()
        {
            return streamCodec;
        }

    }

    public static RecipeType<PoweredRecipe> TYPE = Registry.register(BuiltInRegistries.RECIPE_TYPE,
            ResourceLocation.parse("pokecube_adventures:powered_recipe"), new RecipeType<PoweredRecipe>()
            {
                @Override
                public String toString()
                {
                    return "pokecube_adventures:powered_recipe";
                }
            });

    public PoweredRecipe()
    {}

    public abstract Function<ItemStack, Integer> getCostFunction();

    /** Used to check if a recipe matches current crafting inventory */
    @Override
    public boolean matches(final PoweredCraftingInventory inv, final Level worldIn)
    {
        return !this.assemble(inv, worldIn.registryAccess()).isEmpty();
    }

    @Override
    public ItemStack getResultItem(Provider registries)
    {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean isSpecial()
    {
        return true;
    }

    @Override
    public RecipeType<?> getType()
    {
        return TYPE;
    }
}
