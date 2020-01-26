package pokecube.adventures.blocks.afa;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IWorldPosCallable;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import pokecube.core.inventory.BaseContainer;
import pokecube.core.inventory.TexturedSlot;
import thut.api.OwnableCaps;
import thut.api.block.IOwnableTE;

public class AfaContainer extends BaseContainer
{
    public static final ContainerType<AfaContainer> TYPE = new ContainerType<>(AfaContainer::new);

    private static class InvWrapper implements IInventory
    {
        final IItemHandlerModifiable wrapped;
        final IOwnableTE             ownable;

        public InvWrapper(final IItemHandlerModifiable wrapped, final IOwnableTE ownable)
        {
            this.wrapped = wrapped;
            this.ownable = ownable;
        }

        @Override
        public void clear()
        {
            for (int i = 0; i < this.wrapped.getSlots(); i++)
                this.wrapped.setStackInSlot(i, ItemStack.EMPTY);
        }

        @Override
        public int getSizeInventory()
        {
            return this.wrapped.getSlots();
        }

        @Override
        public boolean isEmpty()
        {
            return false;
        }

        @Override
        public ItemStack getStackInSlot(final int index)
        {
            return this.wrapped.getStackInSlot(index);
        }

        @Override
        public ItemStack decrStackSize(final int index, final int count)
        {
            return this.wrapped.extractItem(index, count, false);
        }

        @Override
        public ItemStack removeStackFromSlot(final int index)
        {
            return this.wrapped.extractItem(index, this.wrapped.getStackInSlot(index).getCount(), false);
        }

        @Override
        public void setInventorySlotContents(final int index, final ItemStack stack)
        {
            this.wrapped.setStackInSlot(index, stack);
        }

        @Override
        public void markDirty()
        {
        }

        @Override
        public boolean isUsableByPlayer(final PlayerEntity player)
        {
            return this.ownable.canEdit(player);
        }

    }

    IInventory inv;
    IOwnableTE ownable;

    public AfaContainer(final int id, final PlayerInventory invIn)
    {
        this(id, invIn, IWorldPosCallable.DUMMY);
    }

    public AfaContainer(final int id, final PlayerInventory invIn, final IWorldPosCallable pos)
    {
        super(AfaContainer.TYPE, id);
        pos.consume((w, p) ->
        {
            final TileEntity tile = w.getTileEntity(p);
            // Server side
            if (tile instanceof AfaTile)
            {
                this.ownable = (IOwnableTE) tile.getCapability(OwnableCaps.CAPABILITY);
                final IItemHandlerModifiable handler = (IItemHandlerModifiable) tile.getCapability(
                        CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
                this.inv = new InvWrapper(handler, this.ownable);
            }
        });
        // Client side
        if (this.ownable == null)
        {
            final AfaTile tile = new AfaTile();
            this.ownable = (IOwnableTE) tile.getCapability(OwnableCaps.CAPABILITY);
            final IItemHandlerModifiable handler = (IItemHandlerModifiable) tile.getCapability(
                    CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
            this.inv = new InvWrapper(handler, this.ownable);
        }

        final int di = 17;
        final int dj = 32;
        final int i = 0;
        final int j = 0;
        this.addSlot(new TexturedSlot(this.inv, 0, dj - 21 + j * 18, di + i * 18, "pokecube:items/slot_cube"));
        this.bindPlayerInventory(invIn, -19);
    }

    @Override
    public IInventory getInv()
    {
        return this.inv;
    }

    @Override
    public int getInventorySlotCount()
    {
        return 1;
    }

    @Override
    public boolean canInteractWith(final PlayerEntity playerIn)
    {
        return this.ownable.canEdit(playerIn);
    }
}
