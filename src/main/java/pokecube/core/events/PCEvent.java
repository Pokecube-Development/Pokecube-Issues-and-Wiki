package pokecube.core.events;

import java.util.UUID;

import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public class PCEvent extends Event
{
    public final World     world;
    public final ItemStack toPC;
    public final boolean   players;
    public final UUID      owner;

    public PCEvent(final World world, final ItemStack stack, final UUID owner, final boolean player)
    {
        this.toPC = stack;
        this.owner = owner;
        this.players = player;
        this.world = world;
    }
}
