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
import pokecube.core.handlers.ModTags;
import pokecube.legends.blocks.containers.GenericBookshelfEmpty;
import pokecube.legends.init.TileEntityInit;

import javax.annotation.Nullable;

public class GenericBookshelfEmptyTile extends LockableLootTileEntity implements ISidedInventory
{
	public NonNullList<ItemStack> itemStacks = NonNullList.withSize(9, ItemStack.EMPTY);
	private ITextComponent name;
	public int bookCount;

	private GenericBookshelfEmptyTile(TileEntityType<?> tileEntityType) {
		super(tileEntityType);
	}

	public GenericBookshelfEmptyTile()
	{
		this(TileEntityInit.GENERIC_BOOKSHELF_EMPTY_TILE.get());
		itemStacks = NonNullList.withSize(9, ItemStack.EMPTY);
	}

	@Override
	public CompoundNBT save(CompoundNBT saveCompoundNBT)
	{
		super.save(saveCompoundNBT);
		this.saveMetadataAndItems(saveCompoundNBT);
		if (!this.trySaveLootTable(saveCompoundNBT)) {
			ItemStackHelper.saveAllItems(saveCompoundNBT, this.itemStacks);
		}
		if (this.name != null) {
			saveCompoundNBT.putString("CustomName", ITextComponent.Serializer.toJson(this.name));
		}
		return saveCompoundNBT;
	}

	@Override
	public void load(BlockState state, CompoundNBT loadCompoundNBT)
	{
		super.load(state, loadCompoundNBT);
		this.itemStacks = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
		if (!this.tryLoadLootTable(loadCompoundNBT)) {
			ItemStackHelper.loadAllItems(loadCompoundNBT, this.itemStacks);
		}
		if (loadCompoundNBT.contains("CustomName", 8)) {
			this.name = ITextComponent.Serializer.fromJson(loadCompoundNBT.getString("CustomName"));
		}
	}

	@Override
	protected ITextComponent getDefaultName() {
		return null;
	}

	@Override
	public int getContainerSize() {
		return 9;
	}

	@Override
	protected Container createMenu(int i, PlayerInventory playerInventory) {
		return null;
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

	public ActionResultType interact(PlayerEntity player, Hand hand, BlockState state, BlockPos pos, World world)
	{
		ItemStack playerHand = player.getItemInHand(hand);
		int number = 0;
		for(ItemStack stack: this.getItems())
		{
			if (!stack.isEmpty())
			{
				number++;
				world.setBlock(pos, state.setValue(GenericBookshelfEmpty.BOOKS, number), 1);
			}
		}
		int slot = number - 1;
		if (this.bookCount < 0) {
			this.bookCount = 0;
		}
		//remove book
		if (playerHand.isEmpty() && hand == Hand.MAIN_HAND)
		{
			ItemStack stack = this.removeItemNoUpdate(slot);
			if (!world.isClientSide() && number > 0)
			{
				world.setBlock(pos, state.setValue(GenericBookshelfEmpty.BOOKS, number - 1), 1);
				world.playSound(null, this.worldPosition, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundCategory.BLOCKS, 1.0F, 1.0F);
				player.addItem(stack);
				--this.bookCount;
				this.setChanged();
				return ActionResultType.SUCCESS;
			}
		}
		//place book
		else if (!playerHand.isEmpty() && this.canPlaceItem(number, playerHand) && hand == Hand.MAIN_HAND)
		{
			ItemStack stack = playerHand.copy();
			stack.setCount(1);
			if (!world.isClientSide()) {
				this.setItem(number, stack);
				if (!player.isCreative()) {
					playerHand.shrink(1);
				}
				world.setBlock(pos, state.setValue(GenericBookshelfEmpty.BOOKS, number + 1), 1);
				world.playSound(null, this.worldPosition, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundCategory.BLOCKS, 1.0F, 1.0F);
				++this.bookCount;
				this.setChanged();
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