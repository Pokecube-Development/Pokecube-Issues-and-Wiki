package thut.bling.bag.small;

import java.util.UUID;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import thut.api.inventory.big.BigInventory;
import thut.api.inventory.big.Manager;

public class SmallInventory extends BigInventory
{

    public SmallInventory(final Manager<? extends BigInventory> manager, final UUID id)
    {
        super(manager, id);
    }

    public SmallInventory(final Manager<? extends BigInventory> manager, final CompoundTag tag)
    {
        super(manager, tag);
    }

    public SmallInventory(final Manager<? extends BigInventory> manager, final FriendlyByteBuf buffer)
    {
        super(manager, buffer);
    }

    @Override
    public int boxCount()
    {
        return 1;
    }
}
