package thut.wearables.events;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.Event;
import thut.wearables.inventory.PlayerWearables;

public class WearablesLoadedEvent extends Event
{
    public final PlayerWearables loaded;
    public final LivingEntity    wearer;

    public WearablesLoadedEvent(final LivingEntity wearer, final PlayerWearables loaded)
    {
        this.loaded = loaded;
        this.wearer = wearer;
    }

}
