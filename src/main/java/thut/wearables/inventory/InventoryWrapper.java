package thut.wearables.inventory;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import thut.wearables.EnumWearable;

public class InventoryWrapper extends Inventory
{
    final PlayerWearables wearable;

    public InventoryWrapper(final PlayerWearables inventoryIn)
    {
        super(13);
        this.wearable = inventoryIn;
    }

    @Override
    public void clear()
    {
    }

    @Override
    public void closeInventory(final PlayerEntity player)
    {
    }

    @Override
    public ItemStack decrStackSize(final int index, final int count)
    {
        return this.removeStackFromSlot(index);
    }

    @Override
    public int getInventoryStackLimit()
    {
        return 1;
    }

    @Override
    public int getSizeInventory()
    {
        return 13;
    }

    @Override
    public ItemStack getStackInSlot(final int index)
    {
        return this.wearable.getStackInSlot(index);
    }

    @Override
    public boolean isEmpty()
    {
        return false;
    }

    @Override
    public boolean isItemValidForSlot(final int index, final ItemStack stack)
    {
        return EnumWearable.getSlot(stack) == EnumWearable.getWearable(index);
    }

    @Override
    public boolean isUsableByPlayer(final PlayerEntity player)
    {
        return true;
    }

    @Override
    public void markDirty()
    {
    }

    @Override
    public void openInventory(final PlayerEntity player)
    {
    }

    @Override
    public ItemStack removeStackFromSlot(final int index)
    {
        return this.wearable.extractItem(index, 1, false);
    }

    @Override
    public void setInventorySlotContents(final int index, final ItemStack stack)
    {
        this.wearable.setStackInSlot(index, stack);
    }
}
