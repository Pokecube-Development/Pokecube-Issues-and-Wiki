package pokecube.legends.recipes;

import java.util.Collections;
import java.util.Map;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.registries.RegistryObject;
import pokecube.legends.PokecubeLegends;
import pokecube.legends.recipes.LegendsDistorticRecipeSerializer.SerializerDistortic;
import thut.core.common.ThutCore;

public class LegendsDistorticRecipeManager
{
    public static final RegistryObject<RecipeType<?>> LEGENDS_DISTORTIC_RECIPE_TYPE = PokecubeLegends.RECIPE_TYPE
            .register("legends_recipe", () -> new RecipeType<>()
            {
                public String toString()
                {
                    return "pokecube_legends:legends_recipe";
                }
            });
    public static final RegistryObject<SerializerDistortic> LEGENDS_DISTORTIC_RECIPE = PokecubeLegends.RECIPE_SERIALIZER
            .register("legends_recipe", () -> new SerializerDistortic());

    public static void onPlayerClickBlock(final PlayerInteractEvent.RightClickBlock event)
    {

        final ResourceKey<Level> dim = event.getWorld().dimension();

        if (!event.getWorld().isClientSide && event.getPlayer() != null)
        {

            final ItemStack heldItem = event.getPlayer().getItemInHand(event.getHand());

            for (final Recipe<?> recipe : LegendsDistorticRecipeManager
                    .getRecipes(LegendsDistorticRecipeManager.LEGENDS_DISTORTIC_RECIPE_TYPE.get(),
                            event.getWorld().getRecipeManager())
                    .values())
                if (recipe instanceof LegendsDistorticRecipeSerializer)
            {

                final LegendsDistorticRecipeSerializer blockRecipe = (LegendsDistorticRecipeSerializer) recipe;

                if (blockRecipe.isValid(heldItem, event.getWorld().getBlockState(event.getPos()).getBlock())
                        && dim == blockRecipe.dimId)
                {
                    if (event.getPlayer().isShiftKeyDown())
                    {
                        for (int i = heldItem.getCount(); i > 0;)
                        {
                            ItemHandlerHelper.giveItemToPlayer(event.getPlayer(), blockRecipe.getResultItem().copy());
                            i--;
                        }
                        heldItem.shrink(heldItem.getCount());
                    } else {
                        ItemHandlerHelper.giveItemToPlayer(event.getPlayer(), blockRecipe.getResultItem().copy());
                        heldItem.shrink(1);
                    }
                    event.setCanceled(true);
                    break;
                }
            }
        }
    }

    private static Map<ResourceLocation, Recipe<?>> getRecipes(final RecipeType<?> recipeType,
            final RecipeManager manager)
    {

        final Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> recipesMap = ObfuscationReflectionHelper
                .getPrivateValue(RecipeManager.class, manager, "f_44007_");
        return recipesMap.getOrDefault(recipeType, Collections.emptyMap());
    }

    public static void init()
    {
        ThutCore.FORGE_BUS.addListener(LegendsDistorticRecipeManager::onPlayerClickBlock);
    }
}
