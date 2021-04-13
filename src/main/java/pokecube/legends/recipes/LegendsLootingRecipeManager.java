package pokecube.legends.recipes;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootTable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.items.ItemHandlerHelper;
import pokecube.legends.PokecubeLegends;
import pokecube.legends.recipes.LegendsLootingRecipeSerializer.SerializerLooting;

public class LegendsLootingRecipeManager 
{	
  	private static final ResourceLocation ID_LOOTING = new ResourceLocation("pokecube_legends:legends_looting");
	public static final IRecipeType<LegendsLootingRecipeSerializer> LEGENDS_LOOTING_RECIPE_TYPE = IRecipeType.register(LegendsLootingRecipeManager.ID_LOOTING.toString());
	public static final RegistryObject<SerializerLooting> LEGENDS_LOOTING_RECIPE = PokecubeLegends.LEGENDS_SERIALIZERS.register("legends_looting", () ->
	    new SerializerLooting());
    
    public static void onPlayerClickBlock (PlayerInteractEvent.RightClickBlock event) 
    {  
        if (!event.getWorld().isClientSide && event.getPlayer() != null) {
            
            final ItemStack heldItem = event.getPlayer().getItemInHand(event.getHand());
            
            for (final IRecipe<?> recipe : getRecipes(LEGENDS_LOOTING_RECIPE_TYPE, event.getWorld().getRecipeManager()).values()) {

                if (recipe instanceof LegendsLootingRecipeSerializer) {
                                    	
                    final LegendsLootingRecipeSerializer blockRecipe = (LegendsLootingRecipeSerializer) recipe;
                                  
                    if (blockRecipe.isValid(heldItem, event.getWorld().getBlockState(event.getPos()).getBlock())) {
                        
                    	final LootTable loottable = event.getEntity().getServer().getLootTables().get(blockRecipe.output);
                        final LootContext.Builder lootcontext$builder = new LootContext.Builder((ServerWorld) event.getEntity()
                                .getCommandSenderWorld()).withRandom(event.getEntityLiving().getRandom());
                        
                        final List<ItemStack> list = loottable.getRandomItems(lootcontext$builder.create(loottable.getParamSet()));
                    	
                    	if (!list.isEmpty()) Collections.shuffle(list);

                        for (final ItemStack itemstack : list)
                        {
                        	ItemHandlerHelper.giveItemToPlayer(event.getPlayer(), itemstack);
                            break;
                        }
                        
                        heldItem.shrink(1);                 
                        ItemHandlerHelper.giveItemToPlayer(event.getPlayer(), blockRecipe.getResultItem());
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
        MinecraftForge.EVENT_BUS.addListener(LegendsLootingRecipeManager::onPlayerClickBlock);
    }
}
