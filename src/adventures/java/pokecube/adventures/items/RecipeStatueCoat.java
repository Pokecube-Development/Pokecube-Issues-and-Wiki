package pokecube.adventures.items;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.utils.RecipePokeAdv;

public class RecipeStatueCoat extends CustomRecipe
{

    public RecipeStatueCoat(CraftingBookCategory category)
    {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, Level level)
    {
        ItemStack statue = ItemStack.EMPTY;
        ItemStack block = ItemStack.EMPTY;
        for (int i = 0; i < input.size(); i++)
        {
            final ItemStack stack = input.getItem(i);
            if (!stack.isEmpty())
            {
                if (statue.isEmpty() && stack.getItem() == PokecubeAdv.STATUE.get().asItem()) statue = stack;
                else if (block.isEmpty()) block = stack;
                else return false;
            }
        }
        return !block.isEmpty() && !statue.isEmpty();
    }

    @Override
    public ItemStack assemble(CraftingInput input, Provider registries)
    {
        ItemStack statue = ItemStack.EMPTY;
        ItemStack block = ItemStack.EMPTY;
        for (int i = 0; i < input.size(); i++)
        {
            final ItemStack stack = input.getItem(i);

            if (!stack.isEmpty())
            {
                if (statue.isEmpty() && stack.getItem() == PokecubeAdv.STATUE.get().asItem()) statue = stack.copy();
                else if (block.isEmpty() && stack.getItem() instanceof BlockItem) block = stack;
            }
        }
        // TODO a data component for this
        Thread.dumpStack();
//        CompoundTag blockTag = statue.getOrCreateTagElement("BlockEntityTag");
//        CompoundTag modelTag = blockTag.getCompound("custom_model");
//        modelTag.putString("over_tex", RegHelper.getKey(block.getItem()).toString());
//
//        blockTag.put("custom_model", modelTag);

        return statue;
    }

    @Override
    public boolean canCraftInDimensions(int x, int y)
    {
        return x * y > 1;
    }

    @Override
    public RecipeSerializer<?> getSerializer()
    {
        return RecipePokeAdv.STATUECOAT.get();
    }

}
