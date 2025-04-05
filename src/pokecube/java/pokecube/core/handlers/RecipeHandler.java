package pokecube.core.handlers;

import java.util.function.Supplier;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import pokecube.core.PokecubeCore;
import pokecube.core.items.UsableItemEffects;
import pokecube.core.items.berries.BerryManager;
import pokecube.core.recipes.MoveRecipes;
import pokecube.core.recipes.RecipeBrewBerries;
import pokecube.core.recipes.RecipePokeseals;
import pokecube.core.recipes.RecipeRevive;
import thut.core.common.ThutCore;

public class RecipeHandler
{
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister
            .create(BuiltInRegistries.RECIPE_SERIALIZER, PokecubeCore.MODID);

    public static final Supplier<SimpleCraftingRecipeSerializer<RecipeRevive>> REVIVE = RecipeHandler.RECIPE_SERIALIZERS
            .register("revive", RecipeHandler.special(RecipeRevive::new));
    public static final Supplier<SimpleCraftingRecipeSerializer<RecipePokeseals>> APPLYSEAL = RecipeHandler.RECIPE_SERIALIZERS
            .register("seal_apply", RecipeHandler.special(RecipePokeseals::new));

    public static void init(final IEventBus bus)
    {
        RecipeHandler.RECIPE_SERIALIZERS.register(bus);
        ThutCore.FORGE_BUS.addListener(RecipeHandler::registerBrewing);
        MoveRecipes.init();

        bus.addListener(BerryManager::modifyComponents);
        bus.addListener(UsableItemEffects::modifyComponents);

    }

    public static void registerBrewing(RegisterBrewingRecipesEvent event)
    {
        event.getBuilder().addRecipe(new RecipeBrewBerries());
    }

    private static <T extends CraftingRecipe> Supplier<SimpleCraftingRecipeSerializer<T>> special(
            SimpleCraftingRecipeSerializer.Factory<T> create)
    {
        return () -> new SimpleCraftingRecipeSerializer<>(create);
    }
}
