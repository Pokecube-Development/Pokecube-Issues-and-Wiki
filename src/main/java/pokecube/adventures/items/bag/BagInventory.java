package pokecube.adventures.items.bag;

import java.util.UUID;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import pokecube.adventures.PokecubeAdv;
import thut.api.inventory.big.BigInventory;
import thut.api.inventory.big.Manager;

public class BagInventory extends BigInventory
{

    public BagInventory(final Manager<? extends BigInventory> manager, final UUID id)
    {
        super(manager, id);
    }

    public BagInventory(final Manager<? extends BigInventory> manager, final CompoundNBT tag)
    {
        super(manager, tag);
    }

    public BagInventory(final Manager<? extends BigInventory> manager, final PacketBuffer buffer)
    {
        super(manager, buffer);
    }

    @Override
    public int boxCount()
    {
        return PokecubeAdv.config.bagPages;
    }
}
