package pokecube.adventures.blocks.genetics.helper;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.ContainerType;
import pokecube.core.inventory.BaseContainer;

public abstract class PoweredContainer<T extends BaseGeneticsTile> extends BaseContainer
{
    public static interface TileProvider<T extends BaseGeneticsTile>
    {
        T getTile(PoweredContainer<T> container);
    }

    public T          tile;
    public IInventory inv;

    protected PoweredContainer(final ContainerType<?> type, final int id, final TileProvider<T> provider)
    {
        super(type, id);
        this.tile = provider.getTile(this);
        this.inv = this.tile;

        this.trackInt(this.tile.progress);
        this.trackInt(this.tile.total);
    }

    @Override
    public boolean canInteractWith(final PlayerEntity playerIn)
    {
        return true;
    }

    @Override
    public IInventory getInv()
    {
        return this.inv;
    }

    @Override
    public int getInventorySlotCount()
    {
        return this.tile.getSizeInventory();
    }

}
