package pokecube.compat.cct.modules;

import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.tileentity.TileEntity;

public abstract class BasePeripheral<T extends TileEntity> implements IPeripheral
{
    private final String   name;
    private final String[] methods;
    public final T         tile;

    public BasePeripheral(final T tile, final String name, final String... methods)
    {
        this.name = name;
        this.methods = methods;
        this.tile = tile;
    }

    @Override
    public String getType()
    {
        return this.name;
    }

    @Override
    public String[] getMethodNames()
    {
        return this.methods;
    }

    @Override
    public boolean equals(final IPeripheral other)
    {
        return other instanceof BasePeripheral<?> && ((BasePeripheral<?>) other).tile == this.tile;
    }

}
