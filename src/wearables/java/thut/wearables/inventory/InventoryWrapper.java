package thut.wearables.inventory;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import thut.wearables.EnumWearable;

public class InventoryWrapper extends SimpleContainer
{
    final PlayerWearables wearable;

    public InventoryWrapper(final PlayerWearables inventoryIn)
    {
        super(13);
        this.wearable = inventoryIn;
    }

    @Override
    public void clearContent()
    {
    }

    @Override
    public void stopOpen(final Player player)
    {
    }

    @Override
    public ItemStack removeItem(final int index, final int count)
    {
        return this.removeItemNoUpdate(index);
    }

    @Override
    public int getMaxStackSize()
    {
        return 1;
    }

    @Override
    public int getContainerSize()
    {
        return 13;
    }

    @Override
    public ItemStack getItem(final int index)
    {
        return this.wearable.getStackInSlot(index);
    }

    @Override
    public boolean isEmpty()
    {
        return false;
    }

    @Override
    public boolean canPlaceItem(final int index, final ItemStack stack)
    {
        return EnumWearable.getSlot(stack) == EnumWearable.getWearable(index);
    }

    @Override
    public boolean stillValid(final Player player)
    {
        return true;
    }

    @Override
    public void setChanged()
    {
    }

    @Override
    public void startOpen(final Player player)
    {
    }

    @Override
    public ItemStack removeItemNoUpdate(final int index)
    {
        return this.wearable.extractItem(index, 1, false);
    }

    @Override
    public void setItem(final int index, final ItemStack stack)
    {
        this.wearable.setStackInSlot(index, stack);
    }
}
