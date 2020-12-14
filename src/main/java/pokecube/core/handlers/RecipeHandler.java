package pokecube.core.handlers;

import java.util.function.Function;
import java.util.function.Supplier;

import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipeSerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import pokecube.core.PokecubeCore;
import pokecube.core.items.berries.RecipeBrewBerries;
import pokecube.core.items.pokecubes.RecipePokeseals;
import pokecube.core.items.revive.RecipeRevive;

public class RecipeHandler
{
    public static final DeferredRegister<IRecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(
            ForgeRegistries.RECIPE_SERIALIZERS, PokecubeCore.MODID);

    private static <T extends IRecipe<?>> Supplier<SpecialRecipeSerializer<T>> special(
            final Function<ResourceLocation, T> create)
    {
        PokecubeCore.LOGGER.warn("Registering for " + create.toString());
        return () -> new SpecialRecipeSerializer<>(create);
    }

    public static final RegistryObject<SpecialRecipeSerializer<RecipeRevive>>    REVIVE    = RecipeHandler.RECIPE_SERIALIZERS
            .register("revive", RecipeHandler.special(RecipeRevive::new));
    public static final RegistryObject<SpecialRecipeSerializer<RecipePokeseals>> APPLYSEAL = RecipeHandler.RECIPE_SERIALIZERS
            .register("seal_apply", RecipeHandler.special(RecipePokeseals::new));

    public static void initRecipes(final RegistryEvent.Register<IRecipeSerializer<?>> event)
    {
        BrewingRecipeRegistry.addRecipe(new RecipeBrewBerries());
    }
}
