package pokecube.api.events;

import java.util.UUID;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

public class PCEvent extends Event implements ICancellableEvent
{
    public final Level     world;
    public final ItemStack toPC;
    public final boolean   players;
    public final UUID      owner;

    public PCEvent(final Level world, final ItemStack stack, final UUID owner, final boolean player)
    {
        this.toPC = stack;
        this.owner = owner;
        this.players = player;
        this.world = world;
    }
}
