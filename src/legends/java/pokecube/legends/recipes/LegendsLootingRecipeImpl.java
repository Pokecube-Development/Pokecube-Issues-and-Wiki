package pokecube.legends.recipes;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import thut.lib.RegHelper;

public class LegendsLootingRecipeImpl implements Recipe<CraftingInput>
{
    public static final SerializerLooting SERIALIZER_LOOTING = new SerializerLooting();

    private final Ingredient input;
    private final Block block;
    public final ResourceLocation output;

    public LegendsLootingRecipeImpl(Ingredient input, ResourceLocation output, ResourceLocation blockId)
    {
        this.input = input;
        this.output = output;
        this.block = BuiltInRegistries.BLOCK.get(blockId);
    }

    @Override
    public String toString()
    {
        return "BlockRecipe [input=" + this.input + ", output=" + this.output + ", blockid= " + this.block + "]";
    }

    @Override
    public boolean matches(CraftingInput inv, Level worldIn)
    {
        return this.input.test(inv.getItem(0));
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
    public ItemStack assemble(CraftingInput inventory, HolderLookup.Provider access)
    {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int x, int y)
    {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries)
    {
        return ItemStack.EMPTY;
    }

    public static class SerializerLooting implements RecipeSerializer<LegendsLootingRecipeImpl>
    {
        public static final MapCodec<LegendsLootingRecipeImpl> CODEC = RecordCodecBuilder.mapCodec(
                i -> i.group(Ingredient.CODEC.fieldOf("input").forGetter(m -> m.input),
                        ResourceLocation.CODEC.fieldOf("output")
                                .forGetter(m -> m.output),
                        ResourceLocation.CODEC.fieldOf("blockId")
                                .forGetter(m -> BuiltInRegistries.BLOCK.getKey(m.block))
                ).apply(i, LegendsLootingRecipeImpl::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, LegendsLootingRecipeImpl> STREAM_CODEC = StreamCodec.of(
                SerializerLooting::toNetwork, SerializerLooting::fromNetwork);

        private static LegendsLootingRecipeImpl fromNetwork(final RegistryFriendlyByteBuf buffer)
        {
            final Ingredient input = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
            final ResourceLocation output = buffer.readResourceLocation();
            final ResourceLocation blockId = buffer.readResourceLocation();
            final Block block = BuiltInRegistries.BLOCK.get(blockId);
            if (block == null) throw new IllegalStateException("The block " + blockId + " does not exist.");
            return new LegendsLootingRecipeImpl(input, output, blockId);
        }

        private static void toNetwork(final RegistryFriendlyByteBuf buffer,
                final LegendsLootingRecipeImpl recipe)
        {
            Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.input);
            buffer.writeResourceLocation(recipe.output);
            buffer.writeResourceLocation(RegHelper.getKey(recipe.block));
        }

        @Override
        public MapCodec<LegendsLootingRecipeImpl> codec()
        {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, LegendsLootingRecipeImpl> streamCodec()
        {
            return STREAM_CODEC;
        }
    }
}
