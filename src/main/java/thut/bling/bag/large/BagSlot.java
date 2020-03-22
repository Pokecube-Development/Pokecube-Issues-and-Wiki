package thut.bling.bag.large;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

public class BagSlot extends Slot
{

    public boolean release = false;

    public BagSlot(final IInventory inventory, final int slotIndex, final int xDisplay, final int yDisplay)
    {
        super(inventory, slotIndex, xDisplay, yDisplay);
    }

    /** Return whether this slot's stack can be taken from this slot. */
    @Override
    public boolean canTakeStack(final PlayerEntity par1PlayerEntity)
    {
        return !this.release;
    }

    @Override
    public boolean isItemValid(final ItemStack itemstack)
    {
        return this.inventory.isItemValidForSlot(this.getSlotIndex(), itemstack);
    }

    /** Called when the stack in a Slot changes */
    @Override
    public void onSlotChanged()
    {
        if (this.getStack().isEmpty()) this.inventory.setInventorySlotContents(this.getSlotIndex(), ItemStack.EMPTY);
        this.inventory.markDirty();
    }

    /** Helper method to put a stack in the slot. */
    @Override
    public void putStack(final ItemStack par1ItemStack)
    {
        this.inventory.setInventorySlotContents(this.getSlotIndex(), par1ItemStack);
        this.onSlotChanged();
    }
}
