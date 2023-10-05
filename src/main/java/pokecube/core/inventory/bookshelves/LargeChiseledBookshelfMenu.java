package pokecube.core.inventory.bookshelves;

import net.minecraft.tags.ItemTags;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import pokecube.core.handlers.ModTags;
import pokecube.core.init.MenuTypes;

public class LargeChiseledBookshelfMenu extends AbstractContainerMenu
{
	private final Container container;

	private LargeChiseledBookshelfMenu(MenuType<?> containerType, int windowId, Inventory playerInventory, int slot) {
		this(containerType, windowId, playerInventory, new SimpleContainer(9 * slot), slot);
	}
	public static LargeChiseledBookshelfMenu twoRows(int slots, Inventory playerInventory) {
		return new LargeChiseledBookshelfMenu(MenuTypes.LARGE_CHISELED_BOOKSHELF.get(), slots, playerInventory, 3);
	}

	public static LargeChiseledBookshelfMenu twoRows(int slots, Inventory playerInventory, Container inventory) {
		return new LargeChiseledBookshelfMenu(MenuTypes.LARGE_CHISELED_BOOKSHELF.get(), slots, playerInventory, inventory, 3);
	}

	public LargeChiseledBookshelfMenu(MenuType<?> containerType, int id, Inventory playerInventory, Container inventory, int slot) {
		super(containerType, id);
		checkContainerSize(inventory, 9);
		this.container = inventory;
		inventory.startOpen(playerInventory.player);

		for(int i = 0; i < 2; ++i)
		{
			for(int j = 0; j < 3; ++j)
			{
				this.addSlot(new Slot(inventory, j + i * 3, 26 + j * 18, 24 + i * 18) {
					public boolean mayPlace(ItemStack stack) {
						return stack.is(ItemTags.BOOKSHELF_BOOKS) || stack.is(ModTags.BOOKS) ||
								stack.is(ModTags.BOOKSHELF_ITEMS);
					}
					public int getMaxStackSize() {
						return 1;
					}
				});
			}
		}

		for(int i = 0; i < 2; ++i)
		{
			for(int j = 0; j < 3; ++j)
			{
				this.addSlot(new Slot(inventory, j + i * 3 + 6, 98 + j * 18, 24 + i * 18) {
					public boolean mayPlace(ItemStack stack) {
						return stack.is(ItemTags.BOOKSHELF_BOOKS) || stack.is(ModTags.BOOKS) ||
								stack.is(ModTags.BOOKSHELF_ITEMS);
					}
					public int getMaxStackSize() {
						return 1;
					}
				});
			}
		}

		for(int k = 0; k < 3; ++k)
		{
			for(int i = 0; i < 9; ++i)
			{
				this.addSlot(new Slot(playerInventory, i + k * 9 + 9, 8 + i * 18, 80 + k * 18));
			}
		}

		for(int l = 0; l < 9; ++l)
		{
			this.addSlot(new Slot(playerInventory, l, 8 + l * 18, 138));
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