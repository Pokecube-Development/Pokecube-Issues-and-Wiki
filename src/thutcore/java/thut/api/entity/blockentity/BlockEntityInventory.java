package thut.api.entity.blockentity;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.wrapper.EmptyItemHandler;

public class BlockEntityInventory implements IItemHandlerModifiable
{

    IItemHandlerModifiable empty = new EmptyItemHandler();

    List<IItemHandlerModifiable> handlers;
    List<Integer> starts = Lists.newArrayList();

    int size = -1;
    final IBlockEntity base;

    public BlockEntityInventory(final IBlockEntity base)
    {
        this.base = base;
    }

    void init()
    {
        if (this.handlers == null)
        {
            this.handlers = Lists.newArrayList();
            this.size = 0;
            final int sizeX = this.base.getTiles().length;
            final int sizeY = this.base.getTiles()[0].length;
            final int sizeZ = this.base.getTiles()[0][0].length;
            for (int i = 0; i < sizeX; i++) for (int k = 0; k < sizeY; k++) for (int j = 0; j < sizeZ; j++)
            {
                // TODO inventories for block entities.
//                final BlockEntity tile = this.base.getTiles()[i][j][k];
//                IItemHandler opt;
//                if (tile != null && (opt = ThutCaps.getInventory(tile)) != null
//                        && opt instanceof IItemHandlerModifiable handler)
//                {
//                    this.handlers.add(handler);
//                    this.starts.add(this.size);
//                    this.size += handler.getSlots();
//                }
            }
        }
    }

    protected int getSubSlot(final int slot)
    {
        for (int i = 0; i < this.starts.size() - 1; i++)
            if (this.starts.get(i + 1) > slot) return slot - this.starts.get(i);
        return 0;
    }

    protected int getIndex(final int slot)
    {
        for (int i = 0; i < this.starts.size() - 1; i++) if (this.starts.get(i + 1) > slot) return i;
        return 0;
    }

    protected IItemHandlerModifiable getFromSlot(final int slot)
    {
        final int index = this.getIndex(slot);
        if (index < this.handlers.size()) return this.handlers.get(index);
        return this.empty;
    }

    @Override
    public int getSlots()
    {
        return this.size;
    }

    @Override
    public ItemStack getStackInSlot(int slot)
    {
        final IItemHandlerModifiable sub = this.getFromSlot(slot);
        slot = this.getSubSlot(slot);
        return sub.getStackInSlot(slot);
    }

    @Override
    public ItemStack insertItem(int slot, final ItemStack stack, final boolean simulate)
    {
        final IItemHandlerModifiable sub = this.getFromSlot(slot);
        slot = this.getSubSlot(slot);
        return sub.insertItem(slot, stack, simulate);
    }

    @Override
    public ItemStack extractItem(int slot, final int amount, final boolean simulate)
    {
        final IItemHandlerModifiable sub = this.getFromSlot(slot);
        slot = this.getSubSlot(slot);
        return sub.extractItem(slot, amount, simulate);
    }

    @Override
    public int getSlotLimit(int slot)
    {
        final IItemHandlerModifiable sub = this.getFromSlot(slot);
        slot = this.getSubSlot(slot);
        return sub.getSlotLimit(slot);
    }

    @Override
    public boolean isItemValid(int slot, final ItemStack stack)
    {
        final IItemHandlerModifiable sub = this.getFromSlot(slot);
        slot = this.getSubSlot(slot);
        return sub.isItemValid(slot, stack);
    }

    @Override
    public void setStackInSlot(int slot, final ItemStack stack)
    {
        final IItemHandlerModifiable sub = this.getFromSlot(slot);
        slot = this.getSubSlot(slot);
        sub.setStackInSlot(slot, stack);
    }

}
