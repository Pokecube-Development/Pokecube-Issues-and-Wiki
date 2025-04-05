package pokecube.adventures.blocks.genetics.helper.crafting;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import pokecube.adventures.blocks.genetics.helper.recipe.IPoweredProgress;

public class PoweredCraftingInventory implements RecipeInput
{
    public final AbstractContainerMenu eventHandler;
    public final IPoweredProgress inventory;
    private int energy = 0;

    public PoweredCraftingInventory(final AbstractContainerMenu container, final IPoweredProgress inventory,
            final int x, final int y)
    {
        this.eventHandler = container;
        this.inventory = inventory;
    }

    public int getEnergy()
    {
        return this.energy;
    }

    /**
     * Returns the stack in the given slot.
     */
    @Override
    public ItemStack getItem(final int index)
    {
        return index >= this.size() ? ItemStack.EMPTY : this.inventory.getList().get(index);
    }

    @Override
    public boolean isEmpty()
    {
        for (final ItemStack itemstack : this.inventory.getList()) if (!itemstack.isEmpty()) return false;
        return true;
    }

    public void setEnergy(final int in)
    {
        this.energy = in;
    }

    @Override
    public int size()
    {
        return this.inventory.getContainerSize();
    }
}
