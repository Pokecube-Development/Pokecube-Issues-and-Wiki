package pokecube.legends.recipes;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import pokecube.legends.PokecubeLegends;
import pokecube.legends.recipes.LegendsDistorticRecipeImpl.SerializerDistortic;
import thut.core.common.ThutCore;

import java.util.function.Supplier;

public class LegendsDistorticRecipeManager
{
    public static final Supplier<RecipeType<LegendsDistorticRecipeImpl>> LEGENDS_DISTORTIC_RECIPE_TYPE = PokecubeLegends.RECIPE_TYPE.register(
            "legends_recipe", () -> new RecipeType<>()
            {
                public String toString()
                {
                    return "pokecube_legends:legends_recipe";
                }
            });
    public static final Supplier<SerializerDistortic> LEGENDS_DISTORTIC_RECIPE = PokecubeLegends.RECIPE_SERIALIZER.register(
            "legends_recipe", SerializerDistortic::new);

    public static void onPlayerClickBlock(final PlayerInteractEvent.RightClickBlock event)
    {

        final ResourceKey<Level> dim = event.getLevel().dimension();

        if (!event.getLevel().isClientSide && event.getEntity() != null)
        {

            final ItemStack heldItem = event.getEntity().getItemInHand(event.getHand());
            for (final RecipeHolder<LegendsDistorticRecipeImpl> recipe : event.getLevel().getRecipeManager()
                    .getAllRecipesFor(LegendsDistorticRecipeManager.LEGENDS_DISTORTIC_RECIPE_TYPE.get()))
                if (recipe.value() instanceof LegendsDistorticRecipeImpl blockRecipe)
                {
                    if (blockRecipe.isValid(heldItem, event.getLevel().getBlockState(event.getPos()).getBlock())
                            && dim == blockRecipe.dimId)
                    {
                        var regAccess = event.getLevel().registryAccess();
                        if (event.getEntity().isShiftKeyDown())
                        {
                            for (int i = heldItem.getCount(); i > 0; )
                            {
                                ItemHandlerHelper.giveItemToPlayer(event.getEntity(),
                                        blockRecipe.getResultItem(regAccess).copy());
                                i--;
                            }
                            heldItem.shrink(heldItem.getCount());
                        }
                        else
                        {
                            ItemHandlerHelper.giveItemToPlayer(event.getEntity(),
                                    blockRecipe.getResultItem(regAccess).copy());
                            heldItem.shrink(1);
                        }
                        event.setCanceled(true);
                        break;
                    }
                }
        }
    }

    public static void init()
    {
        ThutCore.FORGE_BUS.addListener(LegendsDistorticRecipeManager::onPlayerClickBlock);
    }
}
