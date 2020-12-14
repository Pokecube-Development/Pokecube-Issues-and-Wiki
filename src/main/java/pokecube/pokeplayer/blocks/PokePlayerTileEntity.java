package pokecube.pokeplayer.blocks;

import java.util.stream.IntStream;
import javax.annotation.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.container.ChestContainer;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.LockableLootTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
import pokecube.pokeplayer.init.TileEntityInit;

public class PokePlayerTileEntity extends LockableLootTileEntity implements ISidedInventory
{		
	private NonNullList<ItemStack> pokeContens = NonNullList.<ItemStack>withSize(1, ItemStack.EMPTY);

	public PokePlayerTileEntity(TileEntityType<?> typeIn) {
		super(typeIn);
	}
	
	public PokePlayerTileEntity() {
		this(TileEntityInit.POKEPLAYER_TILE.get());
	}
	
	@Override
	public void read(BlockState state, CompoundNBT nbt) {
		super.read(state, nbt);
		if(!this.checkLootAndRead(nbt)) {
			this.pokeContens = NonNullList.withSize(this.getSizeInventory(), ItemStack.EMPTY);
		}
		ItemStackHelper.loadAllItems(nbt, this.pokeContens);
	}
	
	@Override
	public CompoundNBT write(CompoundNBT compound) {
		super.write(compound);
		if(!this.checkLootAndWrite(compound)) {
			ItemStackHelper.saveAllItems(compound, this.pokeContens);
		}
		return compound;
	}
	
	@Override
	public SUpdateTileEntityPacket getUpdatePacket() {
		return new SUpdateTileEntityPacket(this.pos, 0, this.getUpdateTag());
	}
	
	@Override
	public CompoundNBT getUpdateTag() {
		return this.write(new CompoundNBT());
	}
	
	@Override
	public int getSizeInventory() {
		return pokeContens.size();
	}
	
	@Override
	public boolean isEmpty() {
		for(ItemStack itemStack : this.pokeContens)
			if(!itemStack.isEmpty())
				return false;
		return true;
	}
	
	@Override
	protected ITextComponent getDefaultName() {
		return new StringTextComponent("AAAA");
	}
	
	@Override
	public int getInventoryStackLimit() {
		return 1;
	}
	
	@Override
	protected Container createMenu(int id, PlayerInventory player) {
		return ChestContainer.createGeneric9X3(id, player, this);
	}
	
	@Override
	public ITextComponent getDisplayName() {
		return new StringTextComponent("AAAA");
	}
	
	@Override
	protected NonNullList<ItemStack> getItems() {
		return this.pokeContens;
	}
	
	@Override
	protected void setItems(NonNullList<ItemStack> itemsIn) {
		this.pokeContens = itemsIn;
	}
	
	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack) {
		return true;
	}
	
	@Override
	public int[] getSlotsForFace(Direction side) {
		return IntStream.range(0, this.getSizeInventory()).toArray();
	}
	
	@Override
	public boolean canInsertItem(int index, ItemStack itemStackIn, Direction direction) {
		return this.isItemValidForSlot(index, itemStackIn);
	}
	
	@Override
	public boolean canExtractItem(int index, ItemStack stack, Direction direction) {
		return false;
	}
	
	private final LazyOptional<? extends IItemHandler>[] handlers = SidedInvWrapper.create(this, Direction.values());
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction facing) {
		if (!this.removed && facing != null && capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
			return handlers[facing.ordinal()].cast();
		return super.getCapability(capability, facing);
	}

	@Override
	public void remove() {
		super.remove();
		for (LazyOptional<? extends IItemHandler> handler : handlers)
			handler.invalidate();
	}
}