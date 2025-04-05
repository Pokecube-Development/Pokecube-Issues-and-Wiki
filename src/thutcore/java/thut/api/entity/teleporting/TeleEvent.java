package thut.api.entity.teleporting;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;
import thut.core.common.ThutCore;

public class TeleEvent extends EntityTeleportEvent implements ICancellableEvent
{
    public TeleEvent(final Entity entity, final double targetX, final double targetY, final double targetZ)
    {
        super(entity, targetX, targetY, targetZ);
    }

    public static TeleEvent onUseTeleport(final LivingEntity entity, final double targetX, final double targetY,
            final double targetZ)
    {
        final TeleEvent event = new TeleEvent(entity, targetX, targetY, targetZ);
        ThutCore.FORGE_BUS.post(event);
        return event;
    }
}