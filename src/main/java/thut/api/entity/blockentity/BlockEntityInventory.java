package thut.api.entity.blockentity;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.EmptyHandler;

public class BlockEntityInventory implements IItemHandlerModifiable, ICapabilityProvider
{
    private final LazyOptional<IItemHandler> holder = LazyOptional.of(() -> this);

    IItemHandlerModifiable empty = new EmptyHandler();

    List<IItemHandlerModifiable> handlers;
    List<Integer>                starts = Lists.newArrayList();

    int                size = -1;
    final IBlockEntity base;

    public BlockEntityInventory(final IBlockEntity base)
    {
        this.base = base;
    }

    @Override
    public <T> LazyOptional<T> getCapability(final Capability<T> cap, final Direction side)
    {
        return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.orEmpty(cap, this.holder);
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
            for (int i = 0; i < sizeX; i++)
                for (int k = 0; k < sizeY; k++)
                    for (int j = 0; j < sizeZ; j++)
                    {
                        final TileEntity tile = this.base.getTiles()[i][j][k];
                        LazyOptional<IItemHandler> opt;
                        if (tile != null && (opt = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY))
                                .isPresent() && opt.orElse(null) instanceof IItemHandlerModifiable)
                        {
                            final IItemHandlerModifiable handler = (IItemHandlerModifiable) opt.orElse(null);
                            this.handlers.add(handler);
                            this.starts.add(this.size);
                            this.size += handler.getSlots();
                        }
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
        for (int i = 0; i < this.starts.size() - 1; i++)
            if (this.starts.get(i + 1) > slot) return i;
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
