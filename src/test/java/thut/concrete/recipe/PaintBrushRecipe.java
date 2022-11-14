package thut.concrete.recipe;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import com.google.common.collect.Maps;

import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleRecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.Tags;
import thut.concrete.Concrete;
import thut.concrete.item.PaintBrush;

public class PaintBrushRecipe extends CustomRecipe
{

    public static <T extends Recipe<?>> Supplier<SimpleRecipeSerializer<T>> brushDye(
            final Function<ResourceLocation, T> create)
    {
        return () -> new SimpleRecipeSerializer<>(create);
    }

    private static Map<DyeColor, TagKey<Item>> DYETAGS = Maps.newHashMap();

    public static Map<DyeColor, TagKey<Item>> getDyeTagMap()
    {
        if (DYETAGS.isEmpty()) for (final DyeColor colour : DyeColor.values())
        {
            final ResourceLocation tag = new ResourceLocation("forge", "dyes/" + colour.getName());
            DYETAGS.put(colour, TagKey.create(Registry.ITEM_REGISTRY, tag));
        }
        return DYETAGS;
    }

    public PaintBrushRecipe(ResourceLocation loc)
    {
        super(loc);
    }

    @Override
    public boolean matches(CraftingContainer container, Level level)
    {
        boolean brush = false;
        boolean dye = false;
        for (int i = 0; i < container.getContainerSize(); i++)
        {
            final ItemStack stack = container.getItem(i);
            if (stack.isEmpty()) continue;
            boolean isBrush = stack.getItem() instanceof PaintBrush br;
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
    public ItemStack assemble(CraftingContainer container)
    {
        ItemStack dye = ItemStack.EMPTY;
        for (int i = 0; i < container.getContainerSize(); i++)
        {
            final ItemStack stack = container.getItem(i);
            if (stack.isEmpty()) continue;
            boolean isBrush = stack.getItem() instanceof PaintBrush br;
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
        ItemStack brush = new ItemStack(Concrete.BRUSHES[dyeColour.ordinal()].get());
        return brush;
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
    public NonNullList<ItemStack> getRemainingItems(final CraftingContainer inv)
    {
        final NonNullList<ItemStack> nonnulllist = NonNullList.<ItemStack>withSize(inv.getContainerSize(),
                ItemStack.EMPTY);
        for (int i = 0; i < nonnulllist.size(); ++i)
        {
            final ItemStack itemstack = inv.getItem(i);
            nonnulllist.set(i, this.toKeep(i, itemstack, inv));
        }
        return nonnulllist;
    }

    public ItemStack toKeep(final int slot, final ItemStack stackIn, final CraftingContainer inv)
    {
        return net.minecraftforge.common.ForgeHooks.getContainerItem(stackIn);
    }
}
