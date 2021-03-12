package pokecube.adventures.blocks.genetics.helper.crafting;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.RecipeItemHelper;
import pokecube.adventures.blocks.genetics.helper.recipe.IPoweredProgress;

public class PoweredCraftingInventory extends CraftingInventory
{
    public final Container        eventHandler;
    public final IPoweredProgress inventory;
    private int                   energy = 0;

    public PoweredCraftingInventory(final Container container, final IPoweredProgress inventory, final int x,
            final int y)
    {
        super(container, x, y);
        this.eventHandler = container;
        this.inventory = inventory;
    }

    @Override
    public void clearContent()
    {
        this.inventory.clearContent();
    }

    @Override
    public ItemStack removeItem(final int index, final int count)
    {
        final ItemStack ret = this.inventory.removeItem(index, count);
        if (this.eventHandler != null) this.eventHandler.slotsChanged(this);
        return ret;
    }

    @Override
    public void fillStackedContents(final RecipeItemHelper helper)
    {
        for (final ItemStack itemstack : this.inventory.getList())
            helper.accountSimpleStack(itemstack);

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
        return index >= this.getContainerSize() ? ItemStack.EMPTY : this.inventory.getList().get(index);
    }

    @Override
    public boolean isEmpty()
    {
        for (final ItemStack itemstack : this.inventory.getList())
            if (!itemstack.isEmpty()) return false;

        return true;
    }

    /**
     * Don't rename this method to canInteractWith due to conflicts with
     * Container
     */
    @Override
    public boolean stillValid(final PlayerEntity player)
    {
        return true;
    }

    /**
     * For tile entities, ensures the chunk containing the tile entity is saved
     * to disk later - the game won't think it
     * hasn't changed and skip it.
     */
    @Override
    public void setChanged()
    {
    }

    /**
     * Removes a stack from the given slot and returns it.
     */
    @Override
    public ItemStack removeItemNoUpdate(final int index)
    {
        return ItemStackHelper.takeItem(this.inventory.getList(), index);
    }

    public void setEnergy(final int in)
    {
        this.energy = in;
    }

    /**
     * Sets the given item stack to the specified slot in the inventory (can be
     * crafting or armor sections).
     */
    @Override
    public void setItem(final int index, final ItemStack stack)
    {
        this.inventory.getList().set(index, stack);
        if (this.eventHandler != null) this.eventHandler.slotsChanged(this);
    }
}
