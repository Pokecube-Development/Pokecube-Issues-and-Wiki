package pokecube.legends.recipes;

import java.util.Collections;
import java.util.Map;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.items.ItemHandlerHelper;
import pokecube.legends.PokecubeLegends;
import pokecube.legends.recipes.LegendsDistorticRecipeSerializer.SerializerDistortic;

public class LegendsDistorticRecipeManager 
{	
	private static final ResourceLocation ID_DISTORTIC = new ResourceLocation("pokecube_legends:legends_recipe");
    public static final IRecipeType<LegendsDistorticRecipeSerializer> LEGENDS_DISTORTIC_RECIPE_TYPE = IRecipeType.register(LegendsDistorticRecipeManager.ID_DISTORTIC.toString());
    public static final RegistryObject<SerializerDistortic> LEGENDS_DISTORTIC_RECIPE = PokecubeLegends.LEGENDS_SERIALIZERS.register("legends_recipe", () ->
    	new SerializerDistortic());
    
    public static void onPlayerClickBlock (PlayerInteractEvent.RightClickBlock event) {
        
    	final RegistryKey<World> dim = event.getWorld().dimension();
    	
        if (!event.getWorld().isClientSide && event.getPlayer() != null) {
            
            final ItemStack heldItem = event.getPlayer().getItemInHand(event.getHand());
            
            for (final IRecipe<?> recipe : getRecipes(LEGENDS_DISTORTIC_RECIPE_TYPE, event.getWorld().getRecipeManager()).values()) {

                if (recipe instanceof LegendsDistorticRecipeSerializer) {
                    
                    final LegendsDistorticRecipeSerializer blockRecipe = (LegendsDistorticRecipeSerializer) recipe;
                    
                    if (blockRecipe.isValid(heldItem, event.getWorld().getBlockState(event.getPos()).getBlock()) &&
                    		dim == blockRecipe.dimId) {
                        
                        heldItem.shrink(1);
                        
                        ItemHandlerHelper.giveItemToPlayer(event.getPlayer(), blockRecipe.getResultItem().copy());
                        event.setCanceled(true);
                        break;
                    }
                }
            }
        }
    }

	private static Map<ResourceLocation, IRecipe<?>> getRecipes (IRecipeType<?> recipeType, RecipeManager manager) {
        
        final Map<IRecipeType<?>, Map<ResourceLocation, IRecipe<?>>> recipesMap = ObfuscationReflectionHelper.getPrivateValue(RecipeManager.class, manager, "field_199522_d");
        	return recipesMap.getOrDefault(recipeType, Collections.emptyMap());
    }
    
    public static void init()
    {
        MinecraftForge.EVENT_BUS.addListener(LegendsDistorticRecipeManager::onPlayerClickBlock);
    }
}
