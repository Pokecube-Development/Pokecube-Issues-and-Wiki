package pokecube.adventures.blocks.genetics.helper.crafting;

import java.util.List;

import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import pokecube.adventures.blocks.genetics.helper.recipe.IPoweredProgress;

//TODO: Check if this works
public class PoweredCraftingInventory implements CraftingContainer {
    public final AbstractContainerMenu eventHandler;
    public final IPoweredProgress inventory;
    private int                   energy = 0;

    public PoweredCraftingInventory(final AbstractContainerMenu container, final IPoweredProgress inventory, final int x,
            final int y)
    {
        super();
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
    public void fillStackedContents(final StackedContents helper)
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
    public int getContainerSize() {
        return this.inventory.getList().size();
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
    public boolean stillValid(final Player player)
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
        return ContainerHelper.takeItem(this.inventory.getList(), index);
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

    @Override
    public int getWidth() {
        return this.inventory.getList().size();
    }

    @Override
    public int getHeight() {
        return 1;
    }

    @Override
    public List<ItemStack> getItems() {
        return this.inventory.getList();
    }
}
