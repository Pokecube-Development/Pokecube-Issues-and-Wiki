package pokecube.legends.recipes;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class LegendsDistorticRecipeSerializer implements Recipe<Container> {
    
    public static final SerializerDistortic SERIALIZER_DISTORTIC = new SerializerDistortic();
    
    private final Ingredient input;
    private final ItemStack output;
    private final Block block;
    private final ResourceLocation id;
    public  final ResourceKey<Level> dimId;
    
    public LegendsDistorticRecipeSerializer(ResourceLocation id, Ingredient input, ItemStack output, Block block, ResourceLocation dimId)
    {
        this.id = id;
        this.input = input;
        this.output = output;
        this.block = block;
        this.dimId = ResourceKey.create(Registry.DIMENSION_REGISTRY, dimId);
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
    public boolean matches (Container inv, Level worldIn) {
        return this.input.test(inv.getItem(0));
    }
    
    @Override
    public ResourceLocation getId () {     
        return this.id;
    }
    
    @Override
    public RecipeSerializer<?> getSerializer () {      
        return LegendsDistorticRecipeManager.LEGENDS_DISTORTIC_RECIPE.get();
    }
    
    @Override
    public RecipeType<?> getType () {
        
        return LegendsDistorticRecipeManager.LEGENDS_DISTORTIC_RECIPE_TYPE;
    }
    
    public boolean isValid (ItemStack input, Block block) {
        
        return this.input.test(input) && this.block == block;
    }
    
    @Override
	public ItemStack assemble(Container inventory) {
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
    
    public static class SerializerDistortic extends ForgeRegistryEntry<RecipeSerializer<?>> implements
    	RecipeSerializer<LegendsDistorticRecipeSerializer> 
    {
    	
    	@Override
		public LegendsDistorticRecipeSerializer fromJson(ResourceLocation recipeId, JsonObject json) {
            
            final JsonElement inputElement = GsonHelper.isArrayNode(json, "input") ? GsonHelper.getAsJsonArray(json, "input") : GsonHelper.getAsJsonObject(json, "input");

            final Ingredient input = Ingredient.fromJson(inputElement);
            
            final ItemStack output = ShapedRecipe.itemFromJson(GsonHelper.getAsJsonObject(json, "output"));
            
            final ResourceLocation blockId = new ResourceLocation(GsonHelper.getAsString(json, "blockId"));
            final Block block = ForgeRegistries.BLOCKS.getValue(blockId);
            
            final ResourceLocation dimID = new ResourceLocation(GsonHelper.getAsString(json, "dimId"));
            
            if (block == null || block == Blocks.AIR) {
                
                throw new IllegalStateException("The block " + blockId + " does not exist.");
            }
            
            return new LegendsDistorticRecipeSerializer(recipeId, input, output, block, dimID);
        }
        
        @Override
		public LegendsDistorticRecipeSerializer fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) 
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
        public void toNetwork(FriendlyByteBuf buffer, LegendsDistorticRecipeSerializer recipe) 
        {
            recipe.input.toNetwork(buffer);
            buffer.writeItem(recipe.output);
            buffer.writeResourceLocation(recipe.block.getRegistryName());
            buffer.writeResourceLocation(recipe.dimId.location());
        }
    }
}
