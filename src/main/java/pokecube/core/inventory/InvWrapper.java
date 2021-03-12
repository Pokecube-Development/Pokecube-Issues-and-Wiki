package pokecube.core.inventory;

import java.util.List;
import java.util.function.Predicate;

import com.google.common.collect.Lists;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.IInventoryChangedListener;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;

public class InvWrapper implements IInventory
{
    final IItemHandlerModifiable  wrapped;
    final Predicate<PlayerEntity> usable;

    private List<IInventoryChangedListener> listeners;

    public InvWrapper(final IItemHandlerModifiable wrapped, final Predicate<PlayerEntity> usable)
    {
        this.wrapped = wrapped;
        this.usable = usable;
    }

    public InvWrapper(final IItemHandlerModifiable wrapped)
    {
        this(wrapped, p -> true);
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
    public void clearContent()
    {
        for (int i = 0; i < this.wrapped.getSlots(); i++)
            this.wrapped.setStackInSlot(i, ItemStack.EMPTY);
    }

    @Override
    public int getContainerSize()
    {
        return this.wrapped.getSlots();
    }

    @Override
    public boolean isEmpty()
    {
        return false;
    }

    @Override
    public ItemStack getItem(final int index)
    {
        return this.wrapped.getStackInSlot(index);
    }

    @Override
    public ItemStack removeItem(final int index, final int count)
    {
        this.setChanged();
        return this.wrapped.extractItem(index, count, false);
    }

    @Override
    public ItemStack removeItemNoUpdate(final int index)
    {
        this.setChanged();
        return this.wrapped.extractItem(index, this.wrapped.getStackInSlot(index).getCount(), false);
    }

    @Override
    public void setItem(final int index, final ItemStack stack)
    {
        this.setChanged();
        this.wrapped.setStackInSlot(index, stack);
    }

    @Override
    public void setChanged()
    {
        if (this.listeners != null) for (final IInventoryChangedListener iinventorychangedlistener : this.listeners)
            iinventorychangedlistener.containerChanged(this);
    }

    @Override
    public boolean stillValid(final PlayerEntity player)
    {
        return this.usable.test(player);
    }
}