package pokecube.legends.recipes;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class LegendsLootingRecipeSerializer implements Recipe<Container>
{

    public static final SerializerLooting SERIALIZER_LOOTING = new SerializerLooting();

    private final Ingredient input;
    private final Block block;
    public final ResourceLocation output;
    private final ResourceLocation id;

    public LegendsLootingRecipeSerializer(ResourceLocation id, Ingredient input, ResourceLocation output, Block block)
    {
        this.id = id;
        this.input = input;
        this.output = output;
        this.block = block;
    }

    @Override
    public String toString()
    {
        return "BlockRecipe [input=" + this.input + ", output=" + this.output + ", id=" + this.id + ", blockid= "
                + this.block + "]";
    }

    @Override
    public boolean matches(Container inv, Level worldIn)
    {
        return this.input.test(inv.getItem(0));
    }

    @Override
    public ResourceLocation getId()
    {
        return this.id;
    }

    @Override
    public RecipeSerializer<?> getSerializer()
    {
        return LegendsLootingRecipeManager.LEGENDS_LOOTING_RECIPE.get();
    }

    @Override
    public RecipeType<?> getType()
    {

        return LegendsLootingRecipeManager.LEGENDS_LOOTING_RECIPE_TYPE.get();
    }

    public boolean isValid(ItemStack input, Block block)
    {

        return this.input.test(input) && this.block == block;
    }

    @Override
    public ItemStack assemble(Container inventory)
    {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int x, int y)
    {
        return true;
    }

    @Override
    public ItemStack getResultItem()
    {
        return ItemStack.EMPTY;
    }

    public static class SerializerLooting extends ForgeRegistryEntry<RecipeSerializer<?>>
            implements RecipeSerializer<LegendsLootingRecipeSerializer>
    {
        @Override
        public LegendsLootingRecipeSerializer fromJson(ResourceLocation recipeId, JsonObject json)
        {

            final JsonElement inputElement = GsonHelper.isArrayNode(json, "input")
                    ? GsonHelper.getAsJsonArray(json, "input")
                    : GsonHelper.getAsJsonObject(json, "input");

            final Ingredient input = Ingredient.fromJson(inputElement);

            final ResourceLocation blockId = new ResourceLocation(GsonHelper.getAsString(json, "blockId"));
            final Block block = ForgeRegistries.BLOCKS.getValue(blockId);

            final ResourceLocation output = new ResourceLocation("pokecube_legends", "cramobot/cramo_drop");

            if (block == null || block == Blocks.AIR)
            {

                throw new IllegalStateException("The block " + blockId + " does not exist.");
            }

            return new LegendsLootingRecipeSerializer(recipeId, input, output, block);
        }

        @Override
        public LegendsLootingRecipeSerializer fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer)
        {
            final Ingredient input = Ingredient.fromNetwork(buffer);
            final ResourceLocation output = buffer.readResourceLocation();
            final ResourceLocation blockId = buffer.readResourceLocation();
            final Block block = ForgeRegistries.BLOCKS.getValue(blockId);

            if (block == null)
            {

                throw new IllegalStateException("The block " + blockId + " does not exist.");
            }

            return new LegendsLootingRecipeSerializer(recipeId, input, output, block);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, LegendsLootingRecipeSerializer recipe)
        {
            recipe.input.toNetwork(buffer);
            buffer.writeResourceLocation(recipe.output);
            buffer.writeResourceLocation(recipe.block.getRegistryName());
        }
    }
}
