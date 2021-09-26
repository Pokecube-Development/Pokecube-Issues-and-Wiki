package pokecube.legends.tileentity;

import javax.annotation.Nullable;

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
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import pokecube.core.handlers.ModTags;
import pokecube.legends.blocks.containers.GenericBookshelfEmpty;
import pokecube.legends.init.TileEntityInit;

public class GenericBookshelfEmptyTile extends LockableLootTileEntity implements ISidedInventory
{
    public NonNullList<ItemStack> itemStacks = NonNullList.withSize(9, ItemStack.EMPTY);
    private ITextComponent        name;
    public int                    bookCount;

    private GenericBookshelfEmptyTile(final TileEntityType<?> tileEntityType)
    {
        super(tileEntityType);
    }

    public GenericBookshelfEmptyTile()
    {
        this(TileEntityInit.GENERIC_BOOKSHELF_EMPTY_TILE.get());
        this.itemStacks = NonNullList.withSize(9, ItemStack.EMPTY);
    }

    @Override
    public CompoundNBT save(final CompoundNBT saveCompoundNBT)
    {
        super.save(saveCompoundNBT);
        this.saveMetadataAndItems(saveCompoundNBT);
        if (!this.trySaveLootTable(saveCompoundNBT)) ItemStackHelper.saveAllItems(saveCompoundNBT, this.itemStacks);
        if (this.name != null) saveCompoundNBT.putString("CustomName", ITextComponent.Serializer.toJson(this.name));
        return saveCompoundNBT;
    }

    @Override
    public void load(final BlockState state, final CompoundNBT loadCompoundNBT)
    {
        super.load(state, loadCompoundNBT);
        this.itemStacks = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        if (!this.tryLoadLootTable(loadCompoundNBT)) ItemStackHelper.loadAllItems(loadCompoundNBT, this.itemStacks);
        if (loadCompoundNBT.contains("CustomName", 8)) this.name = ITextComponent.Serializer.fromJson(loadCompoundNBT
                .getString("CustomName"));
    }

    @Override
    protected ITextComponent getDefaultName()
    {
        return null;
    }

    @Override
    public int getContainerSize()
    {
        return 9;
    }

    @Override
    protected Container createMenu(final int i, final PlayerInventory playerInventory)
    {
        return null;
    }

    public CompoundNBT saveMetadataAndItems(final CompoundNBT nbt)
    {
        super.save(nbt);
        ItemStackHelper.saveAllItems(nbt, this.itemStacks, true);
        return nbt;
    }

    @Override
    public NonNullList<ItemStack> getItems()
    {
        return this.itemStacks;
    }

    @Override
    protected void setItems(final NonNullList<ItemStack> items)
    {
        this.itemStacks = items;
    }

    @Override
    public int getMaxStackSize()
    {
        return 1;
    }

    public ActionResultType interact(final PlayerEntity player, final Hand hand, final World world)
    {
        final ItemStack playerHand = player.getItemInHand(hand);
        int number = 0;
        for (final ItemStack stack : this.getItems())
            if (!stack.isEmpty()) number++;
        // remove book
        if (playerHand.isEmpty() && hand == Hand.MAIN_HAND)
        {
            final int slot = number - 1;
            final ItemStack stack = this.removeItemNoUpdate(slot);
            if (!world.isClientSide() && number > 0)
            {
                world.playSound(null, this.worldPosition, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundCategory.BLOCKS,
                        1.0F, 1.0F);
                player.addItem(stack);
                this.setChanged();
                return ActionResultType.SUCCESS;
            }
        }
        // place book
        else if (!playerHand.isEmpty() && this.canPlaceItem(number, playerHand) && hand == Hand.MAIN_HAND && !(number >= 9))
        {
            final ItemStack stack = playerHand.copy();
            stack.setCount(1);
            if (!world.isClientSide())
            {
                this.setItem(number, stack);
                if (!player.isCreative()) playerHand.shrink(1);
                world.playSound(null, this.worldPosition, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundCategory.BLOCKS, 1.0F,
                        1.0F);
                this.setChanged();
                return ActionResultType.SUCCESS;
            }
        }
        return ActionResultType.PASS;
    }

    @Override
    public boolean canPlaceItem(final int index, final ItemStack stack)
    {
        final Item book = stack.getItem();
        return book instanceof BookItem || book instanceof EnchantedBookItem || book.is(ItemTags.LECTERN_BOOKS) || book
                .is(ModTags.BOOKS) || book.is(ModTags.BOOKSHELF_ITEMS);
    }

    @Override
    public int[] getSlotsForFace(final Direction direction)
    {
        return new int[0];
    }

    @Override
    public boolean canPlaceItemThroughFace(final int i, final ItemStack itemStack, @Nullable final Direction direction)
    {
        return false;
    }

    @Override
    public boolean canTakeItemThroughFace(final int i, final ItemStack itemStack, final Direction direction)
    {
        return false;
    }

    @Override
    public void setChanged()
    {
        if (this.hasLevel())
        {
            int number = 0;
            for (final ItemStack stack : this.getItems())
                if (!stack.isEmpty()) number++;
            this.level.setBlock(this.getBlockPos(), this.getBlockState().setValue(GenericBookshelfEmpty.BOOKS, number),
                    3);
        }
        super.setChanged();
        // Model also depends on state, so mark for update here as well!
        this.requestModelDataUpdate();
    }
}