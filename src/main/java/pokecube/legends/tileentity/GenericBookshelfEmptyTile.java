package pokecube.legends.tileentity;

import javax.annotation.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.BookItem;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import pokecube.core.handlers.ModTags;
import pokecube.legends.blocks.containers.GenericBookshelfEmpty;
import pokecube.legends.init.TileEntityInit;

public class GenericBookshelfEmptyTile extends RandomizableContainerBlockEntity implements WorldlyContainer
{
    public NonNullList<ItemStack> itemStacks = NonNullList.withSize(9, ItemStack.EMPTY);
    private Component        name;
    public int                    bookCount;

    private GenericBookshelfEmptyTile(final BlockEntityType<?> tileEntityType)
    {
        super(tileEntityType);
    }

    public GenericBookshelfEmptyTile()
    {
        this(TileEntityInit.GENERIC_BOOKSHELF_EMPTY_TILE.get());
        this.itemStacks = NonNullList.withSize(9, ItemStack.EMPTY);
    }

    @Override
    public CompoundTag save(final CompoundTag saveCompoundNBT)
    {
        super.save(saveCompoundNBT);
        this.saveMetadataAndItems(saveCompoundNBT);
        if (!this.trySaveLootTable(saveCompoundNBT)) ContainerHelper.saveAllItems(saveCompoundNBT, this.itemStacks);
        if (this.name != null) saveCompoundNBT.putString("CustomName", Component.Serializer.toJson(this.name));
        return saveCompoundNBT;
    }

    @Override
    public void load(final BlockState state, final CompoundTag loadCompoundNBT)
    {
        super.load(state, loadCompoundNBT);
        this.itemStacks = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        if (!this.tryLoadLootTable(loadCompoundNBT)) ContainerHelper.loadAllItems(loadCompoundNBT, this.itemStacks);
        if (loadCompoundNBT.contains("CustomName", 8)) this.name = Component.Serializer.fromJson(loadCompoundNBT
                .getString("CustomName"));
    }

    @Override
    protected Component getDefaultName()
    {
        return null;
    }

    @Override
    public int getContainerSize()
    {
        return 9;
    }

    @Override
    protected AbstractContainerMenu createMenu(final int i, final Inventory playerInventory)
    {
        return null;
    }

    public CompoundTag saveMetadataAndItems(final CompoundTag nbt)
    {
        super.save(nbt);
        ContainerHelper.saveAllItems(nbt, this.itemStacks, true);
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

    public InteractionResult interact(final Player player, final InteractionHand hand, final Level world)
    {
        final ItemStack playerHand = player.getItemInHand(hand);
        int number = 0;
        for (final ItemStack stack : this.getItems())
            if (!stack.isEmpty()) number++;
        // remove book
        if (playerHand.isEmpty() && hand == InteractionHand.MAIN_HAND)
        {
            final int slot = number - 1;
            final ItemStack stack = this.removeItemNoUpdate(slot);
            if (!world.isClientSide() && number > 0)
            {
                world.playSound(null, this.worldPosition, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS,
                        1.0F, 1.0F);
                player.addItem(stack);
                this.setChanged();
                return InteractionResult.SUCCESS;
            }
        }
        // place book
        else if (!playerHand.isEmpty() && this.canPlaceItem(number, playerHand) && hand == InteractionHand.MAIN_HAND && number < 9)
        {
            final ItemStack stack = playerHand.copy();
            stack.setCount(1);
            if (!world.isClientSide())
            {
                this.setItem(number, stack);
                if (!player.isCreative()) playerHand.shrink(1);
                world.playSound(null, this.worldPosition, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 1.0F,
                        1.0F);
                this.setChanged();
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
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