package thut.tech.common.items;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import thut.tech.common.TechCore;
import thut.tech.common.util.RecipeSerializers;

public class RecipeReset extends CustomRecipe
{
    public RecipeReset(final ResourceLocation location, CraftingBookCategory bookCategory)
    {
        super(location, bookCategory);
    }

    @Override
    public ItemStack assemble(final CraftingContainer inv, RegistryAccess access)
    {
        int n = 0;
        boolean matched = false;

        // Try to match a device linker
        ItemStack linker = ItemStack.EMPTY;
        for (int i = 0; i < inv.getContainerSize(); i++)
        {
            final ItemStack stack = inv.getItem(i);
            if (stack.isEmpty()) continue;
            link:
            if (stack.getItem() == TechCore.LINKER.get())
            {
                if (!stack.hasTag()) break link;
                if (!stack.getTag().contains("lift")) break link;
                matched = true;
                linker = stack;
            }
            n++;
        }
        if (n != 1) matched = false;
        if (matched)
        {
            final ItemStack ret = linker.copy();
            ret.getTag().remove("lift");
            return ret;
        }

        // Try to match an elevator item
        n = 0;
        linker = ItemStack.EMPTY;
        for (int i = 0; i < inv.getContainerSize(); i++)
        {
            final ItemStack stack = inv.getItem(i);
            if (stack.isEmpty()) continue;
            link:
            if (stack.getItem() == TechCore.LIFT.get())
            {
                if (!stack.hasTag()) break link;
                if (!stack.getTag().contains("min")) break link;
                matched = true;
                linker = stack;
            }
            n++;
        }
        if (n != 1) matched = false;
        if (matched)
        {
            final ItemStack ret = linker.copy();
            ret.getTag().remove("min");
            ret.getTag().remove("time");
            return ret;
        }

        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<?> getSerializer()
    {
        return RecipeSerializers.RECIPE_RESET_SERIALIZER.get();
    }

    @Override
    public boolean matches(final CraftingContainer inv, final Level worldIn)
    {
        return !this.assemble(inv, worldIn.registryAccess()).isEmpty();
    }

    @Override
    public boolean canCraftInDimensions(final int width, final int height)
    {
        return width * height > 0;
    }

}
