package thut.bling;

import java.util.function.Supplier;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.registries.DeferredRegister;
import thut.api.item.ItemList;
import thut.bling.data.GemData;

public class GemRecipe extends CustomRecipe
{
    public static final ResourceLocation BLING_TAG = ResourceLocation.fromNamespaceAndPath("thut_bling", "bling");
    public static final ResourceLocation GEM_TAG = ResourceLocation.fromNamespaceAndPath("thut_bling", "gems");
    public static final ResourceLocation APPLY_GEM_TAG = ResourceLocation.fromNamespaceAndPath("thut_bling",
            "apply_gem");

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister
            .create(BuiltInRegistries.RECIPE_SERIALIZER, ThutBling.MODID);

    public static final Supplier<SimpleCraftingRecipeSerializer<GemRecipe>> SERIALIZER = GemRecipe.RECIPE_SERIALIZERS
            .register("apply_gem", GemRecipe.special(GemRecipe::new));

    private static <T extends CraftingRecipe> Supplier<SimpleCraftingRecipeSerializer<T>> special(
            final SimpleCraftingRecipeSerializer.Factory<T> create)
    {
        return () -> new SimpleCraftingRecipeSerializer<>(create);
    }

    public GemRecipe(CraftingBookCategory bookCategory)
    {
        super(bookCategory);
    }

    @Override
    public boolean matches(final CraftingInput inv, final Level worldIn)
    {
        ItemStack bling = ItemStack.EMPTY;
        ItemStack gem = ItemStack.EMPTY;
        int n = 0;
        for (int i = 0; i < inv.size(); i++)
        {
            final ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty())
            {
                n++;
                if (ItemList.is(GemRecipe.BLING_TAG, stack.getItem())) bling = stack;
                if (ItemList.is(GemRecipe.GEM_TAG, stack.getItem())) gem = stack;
            }
        }
        if (n > 2) return false;

        // This is a gem removal recipe
        if (n == 1) return bling.has(ThutBling.BLING_GEM_DATA);

        // Otherwise is a gem addition recipe
        return !bling.isEmpty() && !gem.isEmpty();
    }

    @Override
    public ItemStack assemble(CraftingInput input, Provider registries)
    {
        ItemStack bling = ItemStack.EMPTY;
        ItemStack gem = ItemStack.EMPTY;
        int n = 0;
        for (int i = 0; i < input.size(); i++)
        {
            final ItemStack stack = input.getItem(i);
            if (!stack.isEmpty())
            {
                n++;
                if (ItemList.is(GemRecipe.BLING_TAG, stack.getItem())) bling = stack;
                if (ItemList.is(GemRecipe.GEM_TAG, stack.getItem())) gem = stack;
            }
        }
        final ItemStack newBling = bling.copy();

        GemData data = bling.get(ThutBling.BLING_GEM_DATA);
        // This is a gem removal recipe
        if (n == 1)
        {
            return ItemStack.parseOptional(registries, data.gemTag());
        }
        else
        {
            data = new GemData(255, gem, true, registries);
        }
        return newBling;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(final CraftingInput inv)
    {
        final NonNullList<ItemStack> nonnulllist = NonNullList.withSize(inv.size(), ItemStack.EMPTY);

        ItemStack bling = ItemStack.EMPTY;
        ItemStack gem = ItemStack.EMPTY;
        int blingIndex = 0;
        for (int i = 0; i < inv.size(); i++)
        {
            final ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty())
            {
                if (ItemList.is(GemRecipe.BLING_TAG, stack.getItem()))
                {
                    bling = stack;
                    blingIndex = i;
                }
                if (ItemList.is(GemRecipe.GEM_TAG, stack.getItem())) gem = stack;
            }
        }
        for (int i = 0; i < nonnulllist.size(); ++i)
        {
            final ItemStack item = inv.getItem(i);
            if (item.hasCraftingRemainingItem()) nonnulllist.set(i, item.getCraftingRemainingItem());
        }
        if (gem.isEmpty())
        {
            bling.remove(ThutBling.BLING_GEM_DATA);
            nonnulllist.set(blingIndex, bling.copy());
        }
        return nonnulllist;
    }

    @Override
    public boolean canCraftInDimensions(final int width, final int height)
    {
        return width * height > 1;
    }

    @Override
    public RecipeSerializer<?> getSerializer()
    {
        return GemRecipe.SERIALIZER.get();
    }
}
