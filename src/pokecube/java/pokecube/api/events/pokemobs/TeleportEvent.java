package pokecube.api.events.pokemobs;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;
import thut.core.common.ThutCore;

public class TeleportEvent extends EntityTeleportEvent implements ICancellableEvent
{

    public TeleportEvent(final Entity entity, final double targetX, final double targetY, final double targetZ)
    {
        super(entity, targetX, targetY, targetZ);
    }

    public static TeleportEvent onUseTeleport(final LivingEntity entity, final double targetX, final double targetY,
            final double targetZ)
    {
        final TeleportEvent event = new TeleportEvent(entity, targetX, targetY, targetZ);
        ThutCore.FORGE_BUS.post(event);
        return event;
    }
}
