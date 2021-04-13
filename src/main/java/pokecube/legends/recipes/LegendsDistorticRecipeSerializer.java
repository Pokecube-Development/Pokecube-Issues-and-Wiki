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
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class LegendsDistorticRecipeSerializer implements IRecipe<IInventory> {
    
    public static final SerializerDistortic SERIALIZER_DISTORTIC = new SerializerDistortic();
    
    private final Ingredient input;
    private final ItemStack output;
    private final Block block;
    private final ResourceLocation id;
    public  final RegistryKey<World> dimId;
    
    public LegendsDistorticRecipeSerializer(ResourceLocation id, Ingredient input, ItemStack output, Block block, ResourceLocation dimId)
    {
        this.id = id;
        this.input = input;
        this.output = output;
        this.block = block;
        this.dimId = RegistryKey.create(Registry.DIMENSION_REGISTRY, dimId);
    }
    
    @Override
    public String toString () {
        return "BlockRecipe [input=" + this.input +
        		", output=" + this.output + 
        		", block=" + this.block.getRegistryName() + 
        		", id=" + this.id + 
        		", dimID=" + this.dimId +
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
        return LegendsDistorticRecipeManager.LEGENDS_DISTORTIC_RECIPE.get();
    }
    
    @Override
    public IRecipeType<?> getType () {
        
        return LegendsDistorticRecipeManager.LEGENDS_DISTORTIC_RECIPE_TYPE;
    }
    
    public boolean isValid (ItemStack input, Block block) {
        
        return this.input.test(input) && this.block == block;
    }
    
    @Override
	public ItemStack assemble(IInventory inventory) {
		return this.output.copy();
	}

	@Override
	public boolean canCraftInDimensions(int x, int y) {
		return true;
	}
	
	@Override
	public ItemStack getResultItem() {
		return this.output;
	}
    
    public static class SerializerDistortic extends ForgeRegistryEntry<IRecipeSerializer<?>> implements
    	IRecipeSerializer<LegendsDistorticRecipeSerializer> 
    {
    	
    	@Override
		public LegendsDistorticRecipeSerializer fromJson(ResourceLocation recipeId, JsonObject json) {
            
            final JsonElement inputElement = JSONUtils.isArrayNode(json, "input") ? JSONUtils.getAsJsonArray(json, "input") : JSONUtils.getAsJsonObject(json, "input");

            final Ingredient input = Ingredient.fromJson(inputElement);
            
            final ItemStack output = ShapedRecipe.itemFromJson(JSONUtils.getAsJsonObject(json, "output"));
            
            final ResourceLocation blockId = new ResourceLocation(JSONUtils.getAsString(json, "blockId"));
            final Block block = ForgeRegistries.BLOCKS.getValue(blockId);
            
            final ResourceLocation dimID = new ResourceLocation(JSONUtils.getAsString(json, "dimId"));
            
            if (block == null || block == Blocks.AIR) {
                
                throw new IllegalStateException("The block " + blockId + " does not exist.");
            }
            
            return new LegendsDistorticRecipeSerializer(recipeId, input, output, block, dimID);
        }
        
        @Override
		public LegendsDistorticRecipeSerializer fromNetwork(ResourceLocation recipeId, PacketBuffer buffer) 
        {      
            final Ingredient input = Ingredient.fromNetwork(buffer);
            final ItemStack output = buffer.readItem();
            final ResourceLocation blockId = buffer.readResourceLocation();
            final ResourceLocation dimID = buffer.readResourceLocation();
            final Block block = ForgeRegistries.BLOCKS.getValue(blockId);
            
            if (block == null) {
                
                throw new IllegalStateException("The block " + blockId + " does not exist.");
            }
            
            return new LegendsDistorticRecipeSerializer(recipeId, input, output, block, dimID);
        }
        
        @Override
        public void toNetwork(PacketBuffer buffer, LegendsDistorticRecipeSerializer recipe) 
        {
            recipe.input.toNetwork(buffer);
            buffer.writeItem(recipe.output);
            buffer.writeResourceLocation(recipe.block.getRegistryName());
            buffer.writeResourceLocation(recipe.dimId.location());
        }
    }
}
