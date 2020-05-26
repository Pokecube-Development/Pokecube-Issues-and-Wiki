package pokecube.adventures.blocks.afa;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.IInventoryChangedListener;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IWorldPosCallable;
import net.minecraftforge.items.IItemHandlerModifiable;
import pokecube.core.PokecubeCore;
import pokecube.core.inventory.TexturedSlot;
import thut.api.ThutCaps;
import thut.api.block.IOwnableTE;
import thut.api.inventory.BaseContainer;

public class AfaContainer extends BaseContainer
{
    public static final ContainerType<AfaContainer> TYPE = new ContainerType<>(AfaContainer::new);

    public static class InvWrapper implements IInventory
    {
        final IItemHandlerModifiable wrapped;
        final IOwnableTE             ownable;

        private List<IInventoryChangedListener> listeners;

        public InvWrapper(final IItemHandlerModifiable wrapped, final IOwnableTE ownable)
        {
            this.wrapped = wrapped;
            this.ownable = ownable;
        }

        /**
         * Add a listener that will be notified when any item in this inventory
         * is modified.
         */
        public void addListener(final IInventoryChangedListener listener)
        {
            if (this.listeners == null) this.listeners = Lists.newArrayList();
            this.listeners.add(listener);
        }

        /**
         * removes the specified IInvBasic from receiving further change notices
         */
        public void removeListener(final IInventoryChangedListener listener)
        {
            this.listeners.remove(listener);
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
            this.markDirty();
            return this.wrapped.extractItem(index, count, false);
        }

        @Override
        public ItemStack removeStackFromSlot(final int index)
        {
            this.markDirty();
            return this.wrapped.extractItem(index, this.wrapped.getStackInSlot(index).getCount(), false);
        }

        @Override
        public void setInventorySlotContents(final int index, final ItemStack stack)
        {
            this.markDirty();
            this.wrapped.setStackInSlot(index, stack);
        }

        @Override
        public void markDirty()
        {
            if (this.listeners != null) for (final IInventoryChangedListener iinventorychangedlistener : this.listeners)
                iinventorychangedlistener.onInventoryChanged(this);
        }

        @Override
        public boolean isUsableByPlayer(final PlayerEntity player)
        {
            return this.ownable.canEdit(player);
        }

    }

    IInventory inv;
    IOwnableTE ownable;

    public AfaTile tile;

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
                this.ownable = (IOwnableTE) tile.getCapability(ThutCaps.OWNABLE_CAP).orElse(null);
                this.tile = (AfaTile) tile;
                this.inv = ((AfaTile) tile).inventory;
            }
        });
        // Client side
        if (this.ownable == null)
        {
            this.tile = new AfaTile();
            this.ownable = (IOwnableTE) this.tile.getCapability(ThutCaps.OWNABLE_CAP).orElse(null);
            this.inv = this.tile.inventory;
            this.tile.setWorldAndPos(PokecubeCore.proxy.getWorld(), invIn.player.getPosition());
        }

        final int di = 12;
        final int dj = 36;
        final int i = 0;
        final int j = 0;
        this.addSlot(new TexturedSlot(this.inv, 0, dj - 21 + j * 18, di + i * 18, "pokecube:items/slot_cube"));
        this.bindPlayerInventory(invIn, -19);

        this.trackIntArray(this.tile.syncValues);
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
