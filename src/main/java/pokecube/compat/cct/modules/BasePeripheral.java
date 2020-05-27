package pokecube.compat.cct.modules;

import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.tileentity.TileEntity;

public abstract class BasePeripheral<T extends TileEntity> implements IPeripheral
{
    private final String name;
    public final T       tile;

    public BasePeripheral(final T tile, final String name)
    {
        this.name = name;
        this.tile = tile;
    }

    @Override
    public String getType()
    {
        return this.name;
    }

    @Override
    public boolean equals(final IPeripheral other)
    {
        return other instanceof BasePeripheral<?> && ((BasePeripheral<?>) other).tile == this.tile;
    }

}
