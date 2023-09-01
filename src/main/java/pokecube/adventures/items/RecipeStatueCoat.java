package pokecube.adventures.items;

import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.utils.RecipePokeAdv;
import thut.lib.RegHelper;

public class RecipeStatueCoat extends CustomRecipe
{

    public RecipeStatueCoat(ResourceLocation idIn, CraftingBookCategory category)
    {
        super(idIn, category);
    }

    @Override
    public boolean matches(CraftingContainer inv, Level world)
    {
        ItemStack statue = ItemStack.EMPTY;
        ItemStack block = ItemStack.EMPTY;
        for (int i = 0; i < inv.getContainerSize(); i++)
        {
            final ItemStack stack = inv.getItem(i);
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
    public ItemStack assemble(CraftingContainer inv, RegistryAccess access)
    {
        ItemStack statue = ItemStack.EMPTY;
        ItemStack block = ItemStack.EMPTY;
        for (int i = 0; i < inv.getContainerSize(); i++)
        {
            final ItemStack stack = inv.getItem(i);

            if (!stack.isEmpty())
            {
                if (statue.isEmpty() && stack.getItem() == PokecubeAdv.STATUE.get().asItem()) statue = stack.copy();
                else if (block.isEmpty() && stack.getItem() instanceof BlockItem) block = stack;
            }
        }

        CompoundTag blockTag = statue.getOrCreateTagElement("BlockEntityTag");
        CompoundTag modelTag = blockTag.getCompound("custom_model");
        modelTag.putString("over_tex", RegHelper.getKey(block.getItem()).toString());

        blockTag.put("custom_model", modelTag);

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
