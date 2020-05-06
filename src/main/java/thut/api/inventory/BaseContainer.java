package thut.api.inventory;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

public abstract class BaseContainer extends Container
{
    protected BaseContainer(final ContainerType<?> type, final int id)
    {
        super(type, id);
    }

    public void bindPlayerInventory(final PlayerInventory playerInv, final int yOffset)
    {
        for (int i1 = 0; i1 < 9; ++i1)
            this.addSlot(new Slot(playerInv, i1, 8 + i1 * 18, 161 + yOffset));

        for (int l = 0; l < 3; ++l)
            for (int j1 = 0; j1 < 9; ++j1)
                this.addSlot(new Slot(playerInv, j1 + l * 9 + 9, 8 + j1 * 18, 103 + l * 18 + yOffset));

        this.getInv().openInventory(playerInv.player);
    }

    public abstract IInventory getInv();

    public abstract int getInventorySlotCount();

    @Override
    public void onContainerClosed(final PlayerEntity playerIn)
    {
        super.onContainerClosed(playerIn);
        this.getInv().closeInventory(playerIn);
    }

    @Override
    public ItemStack transferStackInSlot(final PlayerEntity player, final int index)
    {
        ItemStack itemstack = ItemStack.EMPTY;
        final Slot slot = this.inventorySlots.get(index);
        if (slot != null && slot.getHasStack())
        {
            final ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();
            final int slotCount = this.getInventorySlotCount();
            if (index < slotCount)
            {
                if (!this.mergeItemStack(itemstack1, slotCount, this.inventorySlots.size(), false))
                    return ItemStack.EMPTY;
            }
            else if (!this.mergeItemStack(itemstack1, 0, slotCount, false)) return ItemStack.EMPTY;

            if (itemstack1.isEmpty()) slot.putStack(ItemStack.EMPTY);
            else slot.onSlotChanged();
        }
        return itemstack;
    }
}
