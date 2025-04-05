package pokecube.core.inventory.pc;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import pokecube.core.blocks.pc.PCTile;
import thut.api.inventory.InvHelper.ItemCap;
import thut.api.inventory.InvWrapper;

public class PCWrapper extends ItemCap
{
    final PCTile tile;
    public Container container;

    public PCWrapper(final PCTile tileIn)
    {
        super(54);
        this.container = new InvWrapper(this, this::stillValid);
        this.tile = tileIn;
    }

    @Override
    public void deserializeNBT(Provider provider, CompoundTag nbt)
    {}

    @Override
    public CompoundTag serializeNBT(Provider provider)
    {
        return new CompoundTag();
    }

    @Override
    public int getSlots()
    {
        return this.tile.inventory.boxCount() * 54;
    }

    @Override
    public ItemStack getStackInSlot(final int index)
    {
        return this.tile.inventory.getItem(index);
    }

    @Override
    public ItemStack extractItem(final int index, final int count, boolean simulate)
    {
        return this.tile.inventory.removeItem(index, count);
    }

    @Override
    public void setStackInSlot(final int index, final ItemStack stack)
    {
        this.tile.inventory.setItem(index, stack);
    }

    public boolean stillValid(Player player)
    {
        return this.tile.isBound() ? player.getUUID().equals(this.tile.boundId) : true;
    }

    @Override
    public boolean isItemValid(final int index, final ItemStack stack)
    {
        return this.tile.inventory.canPlaceItem(index, stack);
    }
}
