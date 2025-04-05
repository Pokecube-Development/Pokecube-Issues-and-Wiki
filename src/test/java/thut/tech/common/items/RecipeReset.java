package thut.tech.common.items;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import thut.tech.common.TechCore;
import thut.tech.common.util.RecipeSerializers;

public class RecipeReset extends CustomRecipe
{
    public RecipeReset(CraftingBookCategory bookCategory)
    {
        super(bookCategory);
    }

    @Override
    public ItemStack assemble(CraftingInput inv, Provider access)
    {
        int n = 0;
        boolean matched = false;

        // Try to match a device linker
        ItemStack linker = ItemStack.EMPTY;
        for (int i = 0; i < inv.size(); i++)
        {
            final ItemStack stack = inv.getItem(i);
            if (stack.isEmpty()) continue;
            link:
            if (stack.getItem() == TechCore.LINKER.get())
            {
                CompoundTag data = stack.has(DataComponents.CUSTOM_DATA)
                        ? stack.get(DataComponents.CUSTOM_DATA).copyTag()
                        : null;
                if (data == null) break link;
                if (!data.contains("lift")) break link;
                matched = true;
                linker = stack;
            }
            n++;
        }
        if (n != 1) matched = false;
        if (matched)
        {
            final ItemStack ret = linker.copy();
            CompoundTag data = ret.get(DataComponents.CUSTOM_DATA).copyTag();
            data.remove("lift");
            ret.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
            return ret;
        }

        // Try to match an elevator item
        n = 0;
        linker = ItemStack.EMPTY;
        for (int i = 0; i < inv.size(); i++)
        {
            final ItemStack stack = inv.getItem(i);
            if (stack.isEmpty()) continue;
            link:
            if (stack.getItem() == TechCore.LIFT.get())
            {
                CompoundTag data = stack.has(DataComponents.CUSTOM_DATA)
                        ? stack.get(DataComponents.CUSTOM_DATA).copyTag()
                        : null;
                if (data == null) break link;
                if (!data.contains("min")) break link;
                matched = true;
                linker = stack;
            }
            n++;
        }
        if (n != 1) matched = false;
        if (matched)
        {
            final ItemStack ret = linker.copy();
            CompoundTag data = ret.get(DataComponents.CUSTOM_DATA).copyTag();
            data.remove("min");
            data.remove("time");
            ret.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
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
    public boolean matches(final CraftingInput inv, final Level worldIn)
    {
        return !this.assemble(inv, worldIn.registryAccess()).isEmpty();
    }

    @Override
    public boolean canCraftInDimensions(final int width, final int height)
    {
        return width * height > 0;
    }

}
