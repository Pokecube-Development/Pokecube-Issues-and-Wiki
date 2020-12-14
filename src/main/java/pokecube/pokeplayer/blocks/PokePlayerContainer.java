//package pokecube.pokeplayer.blocks;
//
//import java.util.Objects;
//
//import net.minecraft.entity.player.PlayerEntity;
//import net.minecraft.entity.player.PlayerInventory;
//import net.minecraft.inventory.container.Container;
//import net.minecraft.inventory.container.Slot;
//import net.minecraft.item.ItemStack;
//import net.minecraft.network.PacketBuffer;
//import net.minecraft.tileentity.TileEntity;
//import net.minecraft.util.IWorldPosCallable;
//import pokecube.pokeplayer.init.BlockInit;
//import pokecube.pokeplayer.init.ContainerInit;
//
//public class PokePlayerContainer extends Container
//{
//	public final PokePlayerTileEntity tileEntity;
//	private final IWorldPosCallable canInteractWithCallable;
//	
//	public PokePlayerContainer(final int windowId, final PlayerInventory playerInv, final PokePlayerTileEntity tileEntity) {
//		super(ContainerInit.POKEPLAYER_CONTAINER.get(), windowId);
//		this.tileEntity = tileEntity;
//		this.canInteractWithCallable = IWorldPosCallable.of(tileEntity.getWorld(), tileEntity.getPos());
//		
//		//Main Inventory
//		int startX = 8;
//		int startY = 18;
//		this.addSlot(new Slot(tileEntity, 1, startX, startY));
//		
//		//Main Player Inventory
//		int StartPlayerInv = startX * 5 + 12;
//		int slotSizePlus2 = 18;
//		for(int row = 0; row < 3; ++row) {
//			for(int column = 0; column < 9; ++column) {
//			   this.addSlot(new Slot(playerInv, 9 + (row * 9) + column, startX + (column * slotSizePlus2), StartPlayerInv + (row * slotSizePlus2)));
//			}
//		}
//		
//		//HotBar
//		int hotbarY = StartPlayerInv + (StartPlayerInv/2) + 7;
//		for(int column = 0; column < 9; ++column) {
//			this.addSlot(new Slot(playerInv, column, startX + (column * slotSizePlus2), hotbarY));
//		}
//	}
//	
//	private static PokePlayerTileEntity getTileEntity(final PlayerInventory playerInv, final PacketBuffer data) {
//		Objects.requireNonNull(playerInv, "playerInventory cannot be null");
//		Objects.requireNonNull(data, "data cannot be null");
//		final TileEntity tileAtPos = playerInv.player.world.getTileEntity(data.readBlockPos());
//		if(tileAtPos instanceof PokePlayerTileEntity) {
//			return (PokePlayerTileEntity)tileAtPos;
//		}
//		throw new IllegalStateException("Tile Entity os not corret!" + tileAtPos);
//	}
//	
//	public PokePlayerContainer(final int windowId, final PlayerInventory playerInv, final PacketBuffer data) {
//		this(windowId, playerInv, getTileEntity(playerInv, data));
//	}
//	
//	@Override
//	public boolean canInteractWith(PlayerEntity playerIn) {
//		return isWithinUsableDistance(canInteractWithCallable, playerIn, BlockInit.POKEPLAYER_BLOCK.get());
//	}
//	
//	@Override
//	public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
//		ItemStack itemStack = ItemStack.EMPTY;
//		Slot slot = this.inventorySlots.get(index);
//		if(slot != null && slot.getHasStack()) {
//			ItemStack itemStack1 = slot.getStack();
//			itemStack = itemStack1.copy();
//			if(index < 2) {
//				if(!this.mergeItemStack(itemStack1, 1, this.inventorySlots.size(), true)) {
//					return ItemStack.EMPTY;
//				}
//			}else if(!this.mergeItemStack(itemStack1, 0, 1, false)) {
//				return ItemStack.EMPTY;
//			}
//			
//			if(itemStack1.isEmpty()) {
//				slot.putStack(ItemStack.EMPTY);
//			}else {
//				slot.onSlotChanged();
//			}
//		}
//		return itemStack;
//	}
//}
