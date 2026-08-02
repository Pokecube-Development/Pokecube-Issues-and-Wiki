package pokecube.legends.recipes;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
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

public class LegendsDistorticRecipeImpl implements Recipe<CraftingInput>
{

    public static final SerializerDistortic SERIALIZER_DISTORTIC = new SerializerDistortic();

    public final Ingredient input;
    public final ItemStack output;
    public final Block block;
    public final ResourceKey<Level> dimId;

    public LegendsDistorticRecipeImpl(final Ingredient input, final ItemStack output, final ResourceLocation blockId,
            final ResourceLocation dimId)
    {
        this.input = input;
        this.output = output;
        this.block = BuiltInRegistries.BLOCK.get(blockId);
        this.dimId = ResourceKey.create(RegHelper.DIMENSION_REGISTRY, dimId);
    }

    @Override
    public String toString()
    {
        return "BlockRecipe [input=" + this.input + ", output=" + this.output + ", block=" + RegHelper.getKey(
                this.block) + ", dimID=" + this.dimId + "]";
    }

    @Override
    public boolean matches(final CraftingInput inv, final Level worldIn)
    {
        return this.input.test(inv.getItem(0));
    }

    @Override
    public RecipeSerializer<?> getSerializer()
    {
        return LegendsDistorticRecipeManager.LEGENDS_DISTORTIC_RECIPE.get();
    }

    @Override
    public RecipeType<?> getType()
    {

        return LegendsDistorticRecipeManager.LEGENDS_DISTORTIC_RECIPE_TYPE.get();
    }

    public boolean isValid(final ItemStack input, final Block block)
    {

        return this.input.test(input) && this.block == block;
    }

    @Override
    public ItemStack assemble(final CraftingInput inventory, HolderLookup.Provider access)
    {
        return this.output.copy();
    }

    @Override
    public boolean canCraftInDimensions(final int x, final int y)
    {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries)
    {
        return this.output;
    }

    public static class SerializerDistortic implements RecipeSerializer<LegendsDistorticRecipeImpl>
    {
        public static final MapCodec<LegendsDistorticRecipeImpl> CODEC = RecordCodecBuilder.mapCodec(
                i -> i.group(Ingredient.CODEC.fieldOf("input").forGetter(m -> m.input),
                        ItemStack.CODEC.fieldOf("output").forGetter(m -> m.output),
                        ResourceLocation.CODEC.fieldOf("blockId")
                                .forGetter(m -> BuiltInRegistries.BLOCK.getKey(m.block)),
                        ResourceLocation.CODEC.fieldOf("dimId")
                                .forGetter(m -> m.dimId.location())
                ).apply(i, LegendsDistorticRecipeImpl::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, LegendsDistorticRecipeImpl> STREAM_CODEC = StreamCodec.of(
                SerializerDistortic::toNetwork, SerializerDistortic::fromNetwork);

        private static LegendsDistorticRecipeImpl fromNetwork(final RegistryFriendlyByteBuf buffer)
        {
            final Ingredient input = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
            final ItemStack output = ItemStack.STREAM_CODEC.decode(buffer);
            final ResourceLocation blockId = buffer.readResourceLocation();
            final ResourceLocation dimID = buffer.readResourceLocation();
            final Block block = BuiltInRegistries.BLOCK.get(blockId);
            if (block == null) throw new IllegalStateException("The block " + blockId + " does not exist.");
            return new LegendsDistorticRecipeImpl(input, output, blockId, dimID);
        }

        private static void toNetwork(final RegistryFriendlyByteBuf buffer,
                final LegendsDistorticRecipeImpl recipe)
        {
            Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.input);
            ItemStack.STREAM_CODEC.encode(buffer, recipe.output);
            buffer.writeResourceLocation(RegHelper.getKey(recipe.block));
            buffer.writeResourceLocation(recipe.dimId.location());
        }

        @Override
        public MapCodec<LegendsDistorticRecipeImpl> codec()
        {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, LegendsDistorticRecipeImpl> streamCodec()
        {
            return STREAM_CODEC;
        }
    }
}
