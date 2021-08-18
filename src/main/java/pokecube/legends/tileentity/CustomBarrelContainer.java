package pokecube.legends.tileentity;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class CustomBarrelContainer extends Container {
	private final IInventory container;
	private final int containerRows;

	private CustomBarrelContainer(ContainerType<?> containerType, int windowId, PlayerInventory playerInventory, int slot) {
	    this(containerType, windowId, playerInventory, new Inventory(9 * slot), slot);
	}

	public static CustomBarrelContainer threeRows(int slots, PlayerInventory playerInventory) {
		return new CustomBarrelContainer(ContainerType.GENERIC_9x3, slots, playerInventory, 3);
	}

	public static CustomBarrelContainer threeRows(int slots, PlayerInventory playerInventory, IInventory inventory) {
		return new CustomBarrelContainer(ContainerType.GENERIC_9x3, slots, playerInventory, inventory, 3);
	}
	public CustomBarrelContainer(ContainerType<?> containerType, int windowId, PlayerInventory playerInventory, IInventory inventory, int slot) {
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

	public boolean stillValid(PlayerEntity playerEntity) {
		return this.container.stillValid(playerEntity);
	}

	public ItemStack quickMoveStack(PlayerEntity playerEntity, int index) {
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

	public void removed(PlayerEntity playerEntity) {
		super.removed(playerEntity);
		this.container.stopOpen(playerEntity);
	}

	public IInventory getContainer() {
		return this.container;
	}

	@OnlyIn(Dist.CLIENT)
	public int getRowCount() {
		return this.containerRows;
	}
}