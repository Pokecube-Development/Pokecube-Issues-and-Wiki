package pokecube.core.inventory.pc;

import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import pokecube.core.blocks.pc.PCTile;
import thut.api.ThutCaps;

public class PCWrapper implements ICapabilityProvider, Container
{
    private final LazyOptional<IItemHandler> holder;
    final PCTile                             tile;

    public PCWrapper(final PCTile tileIn)
    {
        this.tile = tileIn;
        this.holder = LazyOptional.of(() -> new InvWrapper(this));
    }

    @Override
    public int getContainerSize()
    {
        return this.tile.inventory.boxCount() * 54;
    }

    @Override
    public void clearContent()
    {
        this.tile.inventory.clearContent();
    }

    @Override
    public boolean isEmpty()
    {
        return this.tile.inventory.isEmpty();
    }

    @Override
    public ItemStack getItem(final int index)
    {
        return this.tile.inventory.getItem(index);
    }

    @Override
    public ItemStack removeItem(final int index, final int count)
    {
        return this.tile.inventory.removeItem(index, count);
    }

    @Override
    public ItemStack removeItemNoUpdate(final int index)
    {
        return this.tile.inventory.removeItemNoUpdate(index);
    }

    @Override
    public void setItem(final int index, final ItemStack stack)
    {
        this.tile.inventory.setItem(index, stack);
    }

    @Override
    public void setChanged()
    {
        this.tile.inventory.setChanged();
    }

    @Override
    public boolean stillValid(final Player player)
    {
        return this.tile.isBound() ? player.getUUID().equals(this.tile.boundId) : true;
    }

    @Override
    public boolean canPlaceItem(final int index, final ItemStack stack)
    {
        return this.tile.inventory.canPlaceItem(index, stack);
    }

    @Override
    public <T> LazyOptional<T> getCapability(final Capability<T> cap, final Direction side)
    {
        return ThutCaps.ITEM_HANDLER.orEmpty(cap, this.holder);
    }

}
