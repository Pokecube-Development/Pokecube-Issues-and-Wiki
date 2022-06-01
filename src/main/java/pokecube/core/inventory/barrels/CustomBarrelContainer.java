package pokecube.core.inventory.barrels;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class CustomBarrelContainer extends AbstractContainerMenu {
	private final Container container;
	private final int containerRows;

	private CustomBarrelContainer(MenuType<?> containerType, int windowId, Inventory playerInventory, int slot) {
	    this(containerType, windowId, playerInventory, new SimpleContainer(9 * slot), slot);
	}

	public static CustomBarrelContainer threeRows(int slots, Inventory playerInventory) {
		return new CustomBarrelContainer(MenuType.GENERIC_9x3, slots, playerInventory, 3);
	}

	public static CustomBarrelContainer threeRows(int slots, Inventory playerInventory, Container inventory) {
		return new CustomBarrelContainer(MenuType.GENERIC_9x3, slots, playerInventory, inventory, 3);
	}
	public CustomBarrelContainer(MenuType<?> containerType, int windowId, Inventory playerInventory, Container inventory, int slot) {
	      super(containerType, windowId);
	      checkContainerSize(inventory, slot * 9);
	      this.container = inventory;
	      this.containerRows = slot;
	      inventory.startOpen(playerInventory.player);
	      int i = (this.containerRows - 4) * 18;

	      for(int j = 0; j < this.containerRows; ++j) {
	         for(int k = 0; k < 9; ++k) {
	            this.addSlot(new Slot(inventory, k + j * 9, 8 + k * 18, 18 + j * 18));
	         }
	      }

	      for(int l = 0; l < 3; ++l) {
	         for(int j1 = 0; j1 < 9; ++j1) {
	            this.addSlot(new Slot(playerInventory, j1 + l * 9 + 9, 8 + j1 * 18, 103 + l * 18 + i));
	         }
	      }

	      for(int i1 = 0; i1 < 9; ++i1) {
	         this.addSlot(new Slot(playerInventory, i1, 8 + i1 * 18, 161 + i));
	      }

	   }

	public boolean stillValid(Player playerEntity) {
		return this.container.stillValid(playerEntity);
	}

	public ItemStack quickMoveStack(Player playerEntity, int index) {
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);
		if (slot != null && slot.hasItem()) {
			ItemStack itemstack1 = slot.getItem();
			itemstack = itemstack1.copy();
			if (index < this.containerRows * 9) {
				if (!this.moveItemStackTo(itemstack1, this.containerRows * 9, this.slots.size(), true)) {
					return ItemStack.EMPTY;
				}
			} else if (!this.moveItemStackTo(itemstack1, 0, this.containerRows * 9, false)) {
				return ItemStack.EMPTY;
			}

			if (itemstack1.isEmpty()) {
				slot.set(ItemStack.EMPTY);
			} else {
				slot.setChanged();
			}
		}

		return itemstack;
	}

	public void removed(Player playerEntity) {
		super.removed(playerEntity);
		this.container.stopOpen(playerEntity);
	}

	public Container getContainer() {
		return this.container;
	}

	@OnlyIn(Dist.CLIENT)
	public int getRowCount() {
		return this.containerRows;
	}
}