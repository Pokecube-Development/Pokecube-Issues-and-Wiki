package pokecube.legends.recipes;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class LegendsLootingRecipeSerializer implements IRecipe<IInventory> {
    
    public static final SerializerLooting SERIALIZER_LOOTING = new SerializerLooting();
    
    private final Ingredient input;
    private final Block block;
    public  final ResourceLocation output;
    private final ResourceLocation id;
    
    public LegendsLootingRecipeSerializer(ResourceLocation id, Ingredient input, ResourceLocation output, Block block)
    {
        this.id = id;
        this.input = input;
        this.output = output;
        this.block = block;
    }
    
    @Override
    public String toString () {
        return "BlockRecipe [input=" + this.input +
        		", output=" + this.output + 
        		", id=" + this.id +
        		", blockid= " + this.block +
        		"]";
    }
    
    @Override
    public boolean matches (IInventory inv, World worldIn) {
        return this.input.test(inv.getItem(0));
    }
    
    @Override
    public ResourceLocation getId () {     
        return this.id;
    }
    
    @Override
    public IRecipeSerializer<?> getSerializer () {      
        return LegendsLootingRecipeManager.LEGENDS_LOOTING_RECIPE.get();
    }
    
    @Override
    public IRecipeType<?> getType () {
        
        return LegendsLootingRecipeManager.LEGENDS_LOOTING_RECIPE_TYPE;
    }
    
    public boolean isValid (ItemStack input, Block block) {
        
        return this.input.test(input) && this.block == block;
    }

    @Override
	public ItemStack assemble(IInventory inventory) {
		return ItemStack.EMPTY;
	}

	@Override
	public boolean canCraftInDimensions(int x, int y) {
		return true;
	}
	
	@Override
	public ItemStack getResultItem() {
		return ItemStack.EMPTY;
	}

    public static class SerializerLooting extends ForgeRegistryEntry<IRecipeSerializer<?>> implements
	IRecipeSerializer<LegendsLootingRecipeSerializer> 
	{	
		@Override
		public LegendsLootingRecipeSerializer fromJson(ResourceLocation recipeId, JsonObject json) {
	        
	        final JsonElement inputElement = JSONUtils.isArrayNode(json, "input") ? JSONUtils.getAsJsonArray(json, "input") : JSONUtils.getAsJsonObject(json, "input");
	
	        final Ingredient input = Ingredient.fromJson(inputElement);
	        
	        final ResourceLocation blockId = new ResourceLocation(JSONUtils.getAsString(json, "blockId"));
            final Block block = ForgeRegistries.BLOCKS.getValue(blockId);
	        
	        final ResourceLocation output = new ResourceLocation("pokecube_legends", "cramobot/cramo_drop");
	        
	        if (block == null || block == Blocks.AIR) {
                
                throw new IllegalStateException("The block " + blockId + " does not exist.");
            }

	        return new LegendsLootingRecipeSerializer(recipeId, input, output, block);
	    }
	    
	    @Override
		public LegendsLootingRecipeSerializer fromNetwork(ResourceLocation recipeId, PacketBuffer buffer) 
	    {      
	        final Ingredient input = Ingredient.fromNetwork(buffer);
	        final ResourceLocation output = buffer.readResourceLocation();
            final ResourceLocation blockId = buffer.readResourceLocation();
            final Block block = ForgeRegistries.BLOCKS.getValue(blockId);
            
            if (block == null) {
                
                throw new IllegalStateException("The block " + blockId + " does not exist.");
            }
            
	        return new LegendsLootingRecipeSerializer(recipeId, input, output, block);
	    }
	    
	    @Override
	    public void toNetwork(PacketBuffer buffer, LegendsLootingRecipeSerializer recipe) 
	    {
	        recipe.input.toNetwork(buffer);
	        buffer.writeResourceLocation(recipe.output);
	        buffer.writeResourceLocation(recipe.block.getRegistryName());
	    }
	}
}
