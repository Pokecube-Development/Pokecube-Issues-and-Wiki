package pokecube.pokeplayer.block;

import java.util.Objects;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IWorldPosCallable;
import pokecube.pokeplayer.init.BlockInit;
import pokecube.pokeplayer.init.ContainerInit;
import pokecube.pokeplayer.tileentity.TileEntityTransformer;

public class PokeTransformContainer extends Container {
	
	public final TileEntityTransformer tileEntity;
	private final IWorldPosCallable canInteract;
	
	public PokeTransformContainer(final int windowID, final PlayerInventory playerInv, final TileEntityTransformer tileEntityIn) {
		super(ContainerInit.TRANSFORM_CONTAINER.get(), windowID);
		this.tileEntity = tileEntityIn;
		this.canInteract = IWorldPosCallable.of(tileEntityIn.getWorld(), tileEntityIn.getPos());
		
		this.addSlot(new Slot(tileEntityIn, 0, 81, 36));
		
		//Main Inventory
		int startX = 8;
		int startY = 84;
		int slotSizePlus2 = 18;
		for (int row = 0; row < 3; row++) {
			for (int column = 0; column < 9; column++) {
				this.addSlot(new Slot(playerInv, 9 + (row * 9) + column, startX + (column * slotSizePlus2),
								startY + (row * slotSizePlus2)));
			}
		}
		
		//Hotbar
		for(int column = 0; column < 9; column++) {
			this.addSlot(new Slot(playerInv, column, startX + (column * slotSizePlus2), 142));
		}
	}
	
	public PokeTransformContainer (final int windowID, final PlayerInventory playerInv, final PacketBuffer data) {
		this(windowID, playerInv, getTileEntity(playerInv, data));		
	}
	
	private static TileEntityTransformer getTileEntity(final PlayerInventory playerIn, final PacketBuffer data) {
		Objects.requireNonNull(playerIn, "");
		Objects.requireNonNull(data, "");
		final TileEntity tileAtPos = playerIn.player.world.getTileEntity(data.readBlockPos());
		if(tileAtPos instanceof TileEntityTransformer) {
			return (TileEntityTransformer) tileAtPos;
		}
		throw new IllegalStateException("" + tileAtPos);
	}
	
	@Override
	public boolean canInteractWith(PlayerEntity playerIn) {
		return isWithinUsableDistance(canInteract, playerIn, BlockInit.TRANSFORM.get());
	}
	
	@Override
	public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
		ItemStack itemStack = ItemStack.EMPTY;
		Slot slot = this.inventorySlots.get(index);
		if(slot != null && slot.getHasStack()) {
			ItemStack itemStack1 = slot.getStack();
			itemStack = itemStack1.copy();
			if(index < 1) {
				if (!this.mergeItemStack(itemStack1, 1, this.inventorySlots.size(), true)) {
					return ItemStack.EMPTY;
				}
			}else if (!this.mergeItemStack(itemStack1, 0, 1, false)) {
				return ItemStack.EMPTY;
			}
			
			if(itemStack1.isEmpty()) {
				slot.putStack(ItemStack.EMPTY);
			}else {
				slot.onSlotChanged();
			}
		}
		
		return itemStack;
	}
}
