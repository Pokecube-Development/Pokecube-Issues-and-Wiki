package thut.concrete.recipe;

import java.util.Map;
import java.util.function.Supplier;

import com.google.common.collect.Maps;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.Tags;
import thut.concrete.Concrete;
import thut.concrete.item.PaintBrush;
import thut.lib.RegHelper;

public class PaintBrushRecipe extends CustomRecipe
{

    public static <T extends CraftingRecipe> Supplier<SimpleCraftingRecipeSerializer<T>> brushDye(
            final SimpleCraftingRecipeSerializer.Factory<T> create)
    {
        return () -> new SimpleCraftingRecipeSerializer<>(create);
    }

    private static final Map<DyeColor, TagKey<Item>> DYETAGS = Maps.newHashMap();

    public static Map<DyeColor, TagKey<Item>> getDyeTagMap()
    {
        if (DYETAGS.isEmpty()) for (final DyeColor colour : DyeColor.values())
        {
            final ResourceLocation tag = ResourceLocation.fromNamespaceAndPath("c", "dyes/" + colour.getName());
            DYETAGS.put(colour, TagKey.create(RegHelper.ITEM_REGISTRY, tag));
        }
        return DYETAGS;
    }

    public PaintBrushRecipe(CraftingBookCategory bookCategory)
    {
        super(bookCategory);
    }

    @Override
    public boolean matches(CraftingInput input, Level level)
    {
        boolean brush = false;
        boolean dye = false;
        for (int i = 0; i < input.size(); i++)
        {
            final ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) continue;
            boolean isBrush = stack.getItem() instanceof PaintBrush;
            if (isBrush && brush) return false;
            else if (isBrush)
            {
                brush = true;
                continue;
            }
            final TagKey<Item> dyeTag = Tags.Items.DYES;
            if (stack.is(dyeTag))
            {
                if (dye) return false;
                dye = true;
                continue;
            }
            return false;
        }
        return dye && brush;
    }

    @Override
    public ItemStack assemble(CraftingInput input, Provider registries)
    {
        ItemStack dye = ItemStack.EMPTY;
        for (int i = 0; i < input.size(); i++)
        {
            final ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) continue;
            boolean isBrush = stack.getItem() instanceof PaintBrush;
            if (isBrush) continue;
            final TagKey<Item> dyeTag = Tags.Items.DYES;
            if (stack.is(dyeTag))
            {
                dye = stack;
                break;
            }
            return ItemStack.EMPTY;
        }
        DyeColor dyeColour = null;
        final Map<DyeColor, TagKey<Item>> tags = getDyeTagMap();
        for (final DyeColor colour : DyeColor.values()) if (dye.is(tags.get(colour)))
        {
            dyeColour = colour;
            break;
        }
        if (dyeColour == null) return ItemStack.EMPTY;
        return new ItemStack(Concrete.BRUSHES[dyeColour.ordinal()].get());
    }

    @Override
    public boolean canCraftInDimensions(int x, int y)
    {
        return x * y > 1;
    }

    @Override
    public RecipeSerializer<?> getSerializer()
    {
        return Concrete.BRUSH_DYE_RECIPE.get();
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(final CraftingInput inv)
    {
        final NonNullList<ItemStack> nonnulllist = NonNullList.withSize(inv.size(),
                ItemStack.EMPTY);
        for (int i = 0; i < nonnulllist.size(); ++i)
        {
            final ItemStack itemstack = inv.getItem(i);
            nonnulllist.set(i, this.toKeep(i, itemstack, inv));
        }
        return nonnulllist;
    }

    public ItemStack toKeep(final int slot, final ItemStack stackIn, final CraftingInput inv)
    {
        return CommonHooks.getCraftingRemainingItem(stackIn);
    }
}
