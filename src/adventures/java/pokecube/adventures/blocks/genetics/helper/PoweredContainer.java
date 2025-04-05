package pokecube.adventures.blocks.genetics.helper;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import thut.api.inventory.BaseContainer;

public abstract class PoweredContainer<T extends BaseGeneticsTile> extends BaseContainer
{
    public static interface TileProvider<T extends BaseGeneticsTile>
    {
        T getTile(PoweredContainer<T> container);
    }

    public T          tile;
    public Container inv;

    protected PoweredContainer(final MenuType<?> type, final int id, final TileProvider<T> provider)
    {
        super(type, id);
        this.tile = provider.getTile(this);
        this.inv = this.tile;

        this.addDataSlots(this.tile.syncValues);
    }

    @Override
    public boolean stillValid(final Player playerIn)
    {
        return true;
    }

    @Override
    public Container getInv()
    {
        return this.inv;
    }

    @Override
    public int getInventorySlotCount()
    {
        return this.tile.getContainerSize();
    }

}
