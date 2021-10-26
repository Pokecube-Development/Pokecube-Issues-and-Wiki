package thut.bling.bag.large;

import java.util.UUID;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import thut.api.inventory.big.BigInventory;
import thut.api.inventory.big.Manager;
import thut.bling.ThutBling;

public class LargeInventory extends BigInventory
{

    public LargeInventory(final Manager<? extends BigInventory> manager, final UUID id)
    {
        super(manager, id);
    }

    public LargeInventory(final Manager<? extends BigInventory> manager, final CompoundTag tag)
    {
        super(manager, tag);
    }

    public LargeInventory(final Manager<? extends BigInventory> manager, final FriendlyByteBuf buffer)
    {
        super(manager, buffer);
    }

    @Override
    public int boxCount()
    {
        return ThutBling.config.enderBagPages;
    }
}
