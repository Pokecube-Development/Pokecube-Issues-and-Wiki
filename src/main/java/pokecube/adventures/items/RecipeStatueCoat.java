package pokecube.adventures.items;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.utils.RecipePokeAdv;
import pokecube.core.PokecubeItems;
import pokecube.core.interfaces.IPokecube.PokecubeBehavior;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.items.revive.RecipeRevive;
import thut.api.item.ItemList;

public class RecipeStatueCoat extends CustomRecipe
{

    public RecipeStatueCoat(ResourceLocation idIn)
    {
        super(idIn);
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
    public ItemStack assemble(CraftingContainer inv)
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
        modelTag.putString("over_tex", block.getItem().getRegistryName().toString());

        blockTag.put("custom_model", modelTag);

        System.out.println(statue.getTag());

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
