package pokecube.core.blocks.bookshelves;

import com.mojang.logging.LogUtils;
import java.util.Objects;
import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;
import pokecube.core.PokecubeCore;
import pokecube.core.handlers.ModTags;
import pokecube.core.inventory.bookshelves.LargeChiseledBookshelfMenu;
import thut.lib.TComponent;

public class LargeChiseledBookshelfBlockEntity extends RandomizableContainerBlockEntity implements WorldlyContainer
{
    public static RegistryObject<BlockEntityType<LargeChiseledBookshelfBlockEntity>> LARGE_SHELF_TYPE;
    public static final int MAX_BOOKS_IN_STORAGE = 12;
    public NonNullList<ItemStack> items = NonNullList.withSize(12, ItemStack.EMPTY);
    private static final Logger LOGGER = LogUtils.getLogger();
    private int lastInteractedSlot = -1;
    private Component name;

    private LargeChiseledBookshelfBlockEntity(final BlockEntityType<?> tileEntityType, final BlockPos pos,
                                              final BlockState state)
    {
        super(tileEntityType, pos, state);
        this.items = NonNullList.withSize(12, ItemStack.EMPTY);
    }

    public LargeChiseledBookshelfBlockEntity(final BlockPos pos, final BlockState state)
    {
        this(LARGE_SHELF_TYPE.get(), pos, state);
    }

    private void updateState(int j) {
        if (j >= 0 && j < MAX_BOOKS_IN_STORAGE) {
            this.lastInteractedSlot = j;
            BlockState blockstate = this.getBlockState();

            for(int i = 0; i < LargeChiseledBookshelf.SLOT_OCCUPIED_PROPERTIES.size(); ++i) {
                boolean flag = !this.getItem(i).isEmpty();
                BooleanProperty booleanproperty = LargeChiseledBookshelf.SLOT_OCCUPIED_PROPERTIES.get(i);
                blockstate = blockstate.setValue(booleanproperty, flag);
            }

            Objects.requireNonNull(this.level).setBlock(this.worldPosition, blockstate, 3);
        } else {
            LOGGER.error("Expected slot 0-11, got {}", (int)j);
        }
    }

    @Override
    public void saveAdditional(final CompoundTag saveCompoundNBT)
    {
        super.saveAdditional(saveCompoundNBT);
        if (!this.trySaveLootTable(saveCompoundNBT)) ContainerHelper.saveAllItems(saveCompoundNBT, this.items);
        if (this.name != null) saveCompoundNBT.putString("CustomName", Component.Serializer.toJson(this.name));
        ContainerHelper.saveAllItems(saveCompoundNBT, this.items, true);
        saveCompoundNBT.putInt("last_interacted_slot", this.lastInteractedSlot);
    }

    @Override
    public void load(final CompoundTag loadCompoundNBT)
    {
        super.load(loadCompoundNBT);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        if (!this.tryLoadLootTable(loadCompoundNBT)) ContainerHelper.loadAllItems(loadCompoundNBT, this.items);
        if (loadCompoundNBT.contains("CustomName", 8))
            this.name = Component.Serializer.fromJson(loadCompoundNBT.getString("CustomName"));
        this.items.clear();
        ContainerHelper.loadAllItems(loadCompoundNBT, this.items);
        this.lastInteractedSlot = loadCompoundNBT.getInt("last_interacted_slot");
    }

    @Override
    protected Component getDefaultName()
    {
        return TComponent.translatable("container." + PokecubeCore.MODID + ".large_chiseled_bookshelf");
    }

    @Override
    public int getContainerSize()
    {
        return MAX_BOOKS_IN_STORAGE;
    }

    public int addItem(ItemStack stack)
    {
        for(int i = 0; i < this.items.size(); ++i)
        {
            if (this.items.get(i).isEmpty())
            {
                this.setItem(i, stack);
                return i;
            }
        }
        return -1;
    }

    protected AbstractContainerMenu createMenu(final int i, Inventory playerInventory)
    {
        return LargeChiseledBookshelfMenu.twoRows(i, playerInventory, this);
    }

    @Override
    public NonNullList<ItemStack> getItems()
    {
        return this.items;
    }

    @Override
    protected void setItems(final NonNullList<ItemStack> items)
    {
        this.items = items;
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
        for (final ItemStack stack : this.getItems()) if (!stack.isEmpty()) number++;
        // remove book
        if (playerHand.isEmpty() && hand == InteractionHand.MAIN_HAND)
        {
            final int slot = number - 1;
            final ItemStack stack = this.removeItemNoUpdate(slot);
            if (!world.isClientSide() && number > 0)
            {
                world.playSound(null, this.worldPosition, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 1.0F,
                        1.0F);
                player.addItem(stack);
                this.setChanged();
                return InteractionResult.SUCCESS;
            }
        }
        // place book
        else if (!playerHand.isEmpty() && this.canPlaceItem(number, playerHand) && hand == InteractionHand.MAIN_HAND
                && number < MAX_BOOKS_IN_STORAGE)
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
        return (stack.is(ItemTags.BOOKSHELF_BOOKS) || stack.is(ModTags.BOOKS) ||
                stack.is(ModTags.BOOKSHELF_ITEMS) && this.items.isEmpty());
    }

    public ItemStack getItem(int i) {
        return this.items.get(i);
    }

    public ItemStack removeItem(int i, int j) {
        ItemStack itemstack = Objects.requireNonNullElse(this.items.get(i), ItemStack.EMPTY);
        this.items.set(i, ItemStack.EMPTY);
        if (!itemstack.isEmpty()) {
            this.updateState(i);
        }

        return itemstack;
    }

    public ItemStack removeItemNoUpdate(int i) {
        return this.removeItem(i, 1);
    }

    public void setItem(int i, ItemStack stack)
    {
        this.items.set(i, stack);
        this.updateState(i);
    }

    public boolean canTakeItem(Container container, int i, ItemStack stack)
    {
        return container.hasAnyMatching((itemStack) ->
        {
            if (itemStack.isEmpty())
            {
                return true;
            } else {
                return ItemStack.isSameItemSameTags(stack, itemStack)
                        && itemStack.getCount() + stack.getCount() <= Math.min(itemStack.getMaxStackSize(), container.getMaxStackSize());
            }
        });
    }

    public int getLastInteractedSlot() {
        return this.lastInteractedSlot;
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
            for (final ItemStack stack : this.getItems()) if (!stack.isEmpty()) number++;
            if (this.lootTable != null) number = MAX_BOOKS_IN_STORAGE;
            this.level.setBlock(this.getBlockPos(), this.getBlockState(),
                    3);
        }
        super.setChanged();
        // Model also depends on state, so mark for update here as well!
        this.requestModelDataUpdate();
    }
}