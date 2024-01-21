package thut.api.inventory.big;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class BigSlot extends Slot
{
    private int actualSlot = -1;
    public boolean release = false;

    public BigSlot(final Container inventory, final int slotIndex, final int xDisplay, final int yDisplay)
    {
        super(inventory, slotIndex, xDisplay, yDisplay);
    }

    /** Return whether this slot's stack can be taken from this slot. */
    @Override
    public boolean mayPickup(final Player par1PlayerEntity)
    {
        return !this.release;
    }

    @Override
    public boolean mayPlace(final ItemStack itemstack)
    {
        return this.container.canPlaceItem(this.getSlotIndex(), itemstack);
    }

    /** Called when the stack in a Slot changes */
    @Override
    public void setChanged()
    {
        if (this.getItem().isEmpty()) this.container.setItem(this.getSlotIndex(), ItemStack.EMPTY);
        this.container.setChanged();
    }

    @Override
    public ItemStack getItem()
    {
        return this.container.getItem(this.getContainerSlot());
    }

    @Override
    public void set(final ItemStack par1ItemStack)
    {
        this.container.setItem(this.getSlotIndex(), par1ItemStack);
        this.setChanged();
    }

    @Override
    public ItemStack remove(int amount)
    {
        return this.container.removeItem(this.getContainerSlot(), amount);
    }

    @Override
    public int getContainerSlot()
    {
        return actualSlot;
    }

    @Override
    public int getSlotIndex()
    {
        return actualSlot;
    }

    public void setSlotIndex(int slot)
    {
        actualSlot = slot;
    }
}
