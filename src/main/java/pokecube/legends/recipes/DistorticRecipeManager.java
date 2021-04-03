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
import pokecube.legends.recipes.DistorticRecipeSerializer.Serializer;

public class DistorticRecipeManager 
{
	
	private static final ResourceLocation ID = new ResourceLocation("pokecube_legends:distortic_recipe");
	
    public static final IRecipeType<DistorticRecipeSerializer> DISTORTIC_TYPE = IRecipeType.register(DistorticRecipeManager.ID.toString());

    public static final RegistryObject<Serializer> DISTORTIC = PokecubeLegends.DISTORTIC_SERIALIZERS.register("distortic_recipe", () ->
    	new Serializer());
    
    public static void onPlayerClickBlock (PlayerInteractEvent.RightClickBlock event) {
        
    	final RegistryKey<World> dim = event.getWorld().dimension();
    	
        if (!event.getWorld().isClientSide && event.getPlayer() != null) {
            
            final ItemStack heldItem = event.getPlayer().getItemInHand(event.getHand());
            
            for (final IRecipe<?> recipe : getRecipes(DISTORTIC_TYPE, event.getWorld().getRecipeManager()).values()) {

                if (recipe instanceof DistorticRecipeSerializer) {
                    
                    final DistorticRecipeSerializer blockRecipe = (DistorticRecipeSerializer) recipe;
                    
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
        MinecraftForge.EVENT_BUS.addListener(DistorticRecipeManager::onPlayerClickBlock);
    }
}
