package pokecube.core.inventory.bookshelves;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class GenericBookshelfMenu extends AbstractContainerMenu
{
	private final Container container;

	public GenericBookshelfMenu(int windowId, Inventory playerInventory, Container inventory)
	{
		super(MenuType.GENERIC_3x3, windowId);
		checkContainerSize(inventory, 9);
		this.container = inventory;
		inventory.startOpen(playerInventory.player);

		for(int i = 0; i < 3; ++i)
		{
			for(int j = 0; j < 3; ++j)
			{
				this.addSlot(new Slot(inventory, j + i * 3, 62 + j * 18, 17 + i * 18));
			}
		}

		for(int k = 0; k < 3; ++k)
		{
			for(int i1 = 0; i1 < 9; ++i1)
			{
				this.addSlot(new Slot(playerInventory, i1 + k * 9 + 9, 8 + i1 * 18, 84 + k * 18));
			}
		}

		for(int l = 0; l < 9; ++l)
		{
			this.addSlot(new Slot(playerInventory, l, 8 + l * 18, 142));
		}
	}

	public boolean stillValid(Player playerEntity)
	{
		return this.container.stillValid(playerEntity);
	}

	public ItemStack quickMoveStack(Player playerEntity, int index)
	{
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);
		if (slot != null && slot.hasItem())
		{
			ItemStack itemstack1 = slot.getItem();
			itemstack = itemstack1.copy();
			if (index < 9)
			{
				if (!this.moveItemStackTo(itemstack1, 9, this.slots.size(), true))
				{
					return ItemStack.EMPTY;
				}
			} else if (!this.moveItemStackTo(itemstack1, 0, 9, false))
			{
				return ItemStack.EMPTY;
			}

			if (itemstack1.isEmpty())
			{
				slot.set(ItemStack.EMPTY);
			} else
			{
				slot.setChanged();
			}
		}

		return itemstack;
	}

	public void removed(Player playerEntity)
	{
		super.removed(playerEntity);
		this.container.stopOpen(playerEntity);
	}
}