package pokecube.core.inventory.pc;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import pokecube.core.blocks.pc.PCTile;

public class PCWrapper implements ICapabilityProvider, IInventory
{
    private final LazyOptional<IItemHandler> holder;
    final PCTile                             tile;

    public PCWrapper(final PCTile tileIn)
    {
        this.tile = tileIn;
        this.holder = LazyOptional.of(() -> new InvWrapper(this));
    }

    @Override
    public int getSizeInventory()
    {
        return PCInventory.PAGECOUNT * 56;
    }

    @Override
    public void clear()
    {
        this.tile.inventory.clear();
    }

    @Override
    public boolean isEmpty()
    {
        return this.tile.inventory.isEmpty();
    }

    @Override
    public ItemStack getStackInSlot(final int index)
    {
        return this.tile.inventory.getStackInSlot(index);
    }

    @Override
    public ItemStack decrStackSize(final int index, final int count)
    {
        return this.tile.inventory.decrStackSize(index, count);
    }

    @Override
    public ItemStack removeStackFromSlot(final int index)
    {
        return this.tile.inventory.removeStackFromSlot(index);
    }

    @Override
    public void setInventorySlotContents(final int index, final ItemStack stack)
    {
        this.tile.inventory.setInventorySlotContents(index, stack);
    }

    @Override
    public void markDirty()
    {
        this.tile.inventory.markDirty();
    }

    @Override
    public boolean isUsableByPlayer(final PlayerEntity player)
    {
        return this.tile.isBound() ? player.getUniqueID().equals(this.tile.boundId) : true;
    }

    @Override
    public boolean isItemValidForSlot(final int index, final ItemStack stack)
    {
        return this.tile.inventory.isItemValidForSlot(index, stack);
    }

    @Override
    public <T> LazyOptional<T> getCapability(final Capability<T> cap, final Direction side)
    {
        return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.orEmpty(cap, this.holder);
    }

}
