package thut.api.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public abstract class BaseContainer extends AbstractContainerMenu
{
    protected BaseContainer(final MenuType<?> type, final int id)
    {
        super(type, id);
    }

    public void bindPlayerInventory(final Inventory playerInv, final int yOffset)
    {
        for (int i1 = 0; i1 < 9; ++i1) this.addSlot(new Slot(playerInv, i1, 8 + i1 * 18, 161 + yOffset));

        for (int l = 0; l < 3; ++l) for (int j1 = 0; j1 < 9; ++j1)
            this.addSlot(new Slot(playerInv, j1 + l * 9 + 9, 8 + j1 * 18, 103 + l * 18 + yOffset));

        this.getInv().startOpen(playerInv.player);
    }

    public abstract Container getInv();

    @Override
    public boolean stillValid(Player player)
    {
        return getInv().stillValid(player);
    }

    public int getInventorySlotCount()
    {
        return this.getInv().getContainerSize();
    }

    @Override
    public void removed(final Player playerIn)
    {
        super.removed(playerIn);
        this.getInv().stopOpen(playerIn);
    }

    @Override
    public ItemStack quickMoveStack(final Player player, final int index)
    {
        ItemStack itemstack = ItemStack.EMPTY;
        final Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem())
        {
            final ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            final int slotCount = this.getInventorySlotCount();
            if (index < slotCount)
            {
                if (!this.moveItemStackTo(itemstack1, slotCount, this.slots.size(), false)) return ItemStack.EMPTY;
            }
            else if (!this.moveItemStackTo(itemstack1, 0, slotCount, false)) return ItemStack.EMPTY;

            if (itemstack1.isEmpty()) slot.set(ItemStack.EMPTY);
            else slot.setChanged();
        }
        return itemstack;
    }
}
