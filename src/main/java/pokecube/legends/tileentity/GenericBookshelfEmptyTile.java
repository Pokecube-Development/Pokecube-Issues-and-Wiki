package pokecube.legends.tileentity;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.BookItem;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tags.ItemTags;
import net.minecraft.tileentity.LockableLootTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import pokecube.core.blocks.GenericBookshelfEmpty;
import pokecube.core.handlers.ModTags;
import pokecube.legends.init.TileEntityInit;

import javax.annotation.Nullable;
import java.util.Objects;

public class GenericBookshelfEmptyTile extends LockableLootTileEntity implements ISidedInventory
{
	private NonNullList<ItemStack> itemStacks = NonNullList.withSize(9, ItemStack.EMPTY);

	private GenericBookshelfEmptyTile(TileEntityType<?> tileEntityType) {
		super(tileEntityType);
	}

	public GenericBookshelfEmptyTile()
	{
		this(TileEntityInit.GENERIC_BOOKSHELF_EMPTY_TILE.get());
		itemStacks = NonNullList.withSize(9, ItemStack.EMPTY);
	}

	@Override
	public int getContainerSize() {
		return 9;
	}

	@Override
	public CompoundNBT save(CompoundNBT nbt)
	{
		super.save(nbt);
		this.saveMetadataAndItems(nbt);
		if (!this.trySaveLootTable(nbt)) {
			ItemStackHelper.saveAllItems(nbt, this.itemStacks);
		}
		return nbt;
	}

	@Override
	protected ITextComponent getDefaultName() {
		return null;
	}

	@Override
	protected Container createMenu(int i, PlayerInventory playerInventory) {
		return null;
	}

	@Override
	public void load(BlockState state, CompoundNBT nbt)
	{
		super.load(state, nbt);
		this.itemStacks = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
		if (!this.tryLoadLootTable(nbt)) {
			ItemStackHelper.loadAllItems(nbt, this.itemStacks);
		}
	}

	public CompoundNBT saveMetadataAndItems(CompoundNBT nbt)
	{
		super.save(nbt);
		ItemStackHelper.saveAllItems(nbt, this.itemStacks, true);
		return nbt;
	}

	public NonNullList<ItemStack> getItems() {
		return this.itemStacks;
	}

	protected void setItems(NonNullList<ItemStack> items)
	{
		this.itemStacks = items;
	}

	@Override
	public int getMaxStackSize() {
		return 1;
	}

	public int getBooks(BlockState state) {
		return (Integer)state.getValue(GenericBookshelfEmpty.BOOKS);
	}

	public ActionResultType interact(PlayerEntity player, Hand handIn, BlockState state, BlockPos pos, World world){
		return this.interact(player, handIn, 0, state, pos, world);
	}

	public ActionResultType interact(PlayerEntity player, Hand hand, int slot, BlockState state, BlockPos pos, World world)
	{
		ItemStack item = player.getItemInHand(hand);
		int i = this.getBooks(state);
		slot = i - 1;
		//remove book
		if (item.isEmpty() && i >= 0 && hand == Hand.MAIN_HAND)
		{
			ItemStack stack = this.removeItemNoUpdate(slot);

			if (!world.isClientSide() && i > 0)
			{
				world.setBlock(pos, state.setValue(GenericBookshelfEmpty.BOOKS, i - 1), 1);
				world.playSound(null, this.worldPosition, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundCategory.BLOCKS, 1.0F, 1.0F);
				player.addItem(stack);
				System.out.println("Removed a book");
				this.setChanged();
				return ActionResultType.SUCCESS;
			}
		}
		//place book
		else if (!item.isEmpty() && this.canPlaceItem(slot, item) && i <= 9 && hand == Hand.MAIN_HAND)
		{
			ItemStack stack = item.copy();
			stack.setCount(1);

			if (!world.isClientSide() && i < 9) {
				this.setItem(slot + 1, stack);
				if (!player.isCreative()) {
					item.shrink(1);
				}
				world.setBlock(pos, state.setValue(GenericBookshelfEmpty.BOOKS, i + 1), 1);
				world.playSound(null, this.worldPosition, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundCategory.BLOCKS, 1.0F, 1.0F);
				this.setChanged();
				System.out.println("Shelved a book");
				return ActionResultType.SUCCESS;
			}
		}
		return ActionResultType.PASS;
	}

	@Override
	public boolean canPlaceItem(int index, ItemStack stack)
	{
		Item book = stack.getItem();
		return book instanceof BookItem || book instanceof EnchantedBookItem ||
			book.is(ItemTags.LECTERN_BOOKS) || book.is(ModTags.BOOKS) || book.is(ModTags.BOOKSHELF_ITEMS);
	}

	public void markUpdated()
	{
		this.setChanged();
		Objects.requireNonNull(this.getLevel()).sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 9);
	}

	@Override
	public int[] getSlotsForFace(Direction direction) {
		return new int[0];
	}

	@Override
	public boolean canPlaceItemThroughFace(int i, ItemStack itemStack, @Nullable Direction direction) {
		return false;
	}

	@Override
	public boolean canTakeItemThroughFace(int i, ItemStack itemStack, Direction direction) {
		return false;
	}
}