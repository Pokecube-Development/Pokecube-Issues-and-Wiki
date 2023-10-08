package thut.bling;

import java.util.function.Supplier;

import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import thut.api.item.ItemList;

public class GemRecipe extends CustomRecipe
{
    public static final ResourceLocation BLING_TAG = new ResourceLocation("thut_bling", "bling");
    public static final ResourceLocation GEM_TAG = new ResourceLocation("thut_bling", "gems");
    public static final ResourceLocation APPLY_GEM_TAG = new ResourceLocation("thut_bling", "apply_gem");

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister
            .create(ForgeRegistries.RECIPE_SERIALIZERS, ThutBling.MODID);

    public static final RegistryObject<SimpleCraftingRecipeSerializer<GemRecipe>> SERIALIZER = GemRecipe.RECIPE_SERIALIZERS
            .register("apply_gem", GemRecipe.special(GemRecipe::new));

    private static <T extends CraftingRecipe> Supplier<SimpleCraftingRecipeSerializer<T>> special(
            final SimpleCraftingRecipeSerializer.Factory<T> create)
    {
        return () -> new SimpleCraftingRecipeSerializer<>(create);
    }

    public GemRecipe(final ResourceLocation location, CraftingBookCategory bookCategory)
    {
        super(location, bookCategory);
    }

    @Override
    public boolean matches(final CraftingContainer inv, final Level worldIn)
    {
        ItemStack bling = ItemStack.EMPTY;
        ItemStack gem = ItemStack.EMPTY;
        int n = 0;
        for (int i = 0; i < inv.getContainerSize(); i++)
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
        if (n == 1) return bling.hasTag() && bling.getTag().contains("gemTag");

        // Otherwise is a gem addition recipe
        return !bling.isEmpty() && !gem.isEmpty();
    }

    @Override
    public ItemStack assemble(final CraftingContainer inv, RegistryAccess access)
    {
        ItemStack bling = ItemStack.EMPTY;
        ItemStack gem = ItemStack.EMPTY;
        int n = 0;
        for (int i = 0; i < inv.getContainerSize(); i++)
        {
            final ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty())
            {
                n++;
                if (ItemList.is(GemRecipe.BLING_TAG, stack.getItem())) bling = stack;
                if (ItemList.is(GemRecipe.GEM_TAG, stack.getItem())) gem = stack;
            }
        }
        final ItemStack newBling = bling.copy();

        // This is a gem removal recipe
        if (n == 1)
        {
            final CompoundTag tag = newBling.getTag().getCompound("gemTag");
            return ItemStack.of(tag);
        }
        else
        {
            final CompoundTag tag = gem.save(new CompoundTag());
            if (!newBling.hasTag()) newBling.setTag(new CompoundTag());
            newBling.getTag().put("gemTag", tag);
        }
        return newBling;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(final CraftingContainer inv)
    {
        final NonNullList<ItemStack> nonnulllist = NonNullList.withSize(inv.getContainerSize(), ItemStack.EMPTY);

        ItemStack bling = ItemStack.EMPTY;
        ItemStack gem = ItemStack.EMPTY;
        int blingIndex = 0;
        for (int i = 0; i < inv.getContainerSize(); i++)
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
            bling.removeTagKey("gemTag");
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
