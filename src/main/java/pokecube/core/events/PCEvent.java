package pokecube.core.events;

import java.util.UUID;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public class PCEvent extends Event
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
