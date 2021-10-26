package pokecube.core.events;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.EntityTeleportEvent;
import net.minecraftforge.eventbus.api.Cancelable;

@Cancelable
public class TeleportEvent extends EntityTeleportEvent
{

    public TeleportEvent(final Entity entity, final double targetX, final double targetY, final double targetZ)
    {
        super(entity, targetX, targetY, targetZ);
    }

    public static TeleportEvent onUseTeleport(final LivingEntity entity, final double targetX, final double targetY,
            final double targetZ)
    {
        final TeleportEvent event = new TeleportEvent(entity, targetX, targetY, targetZ);
        MinecraftForge.EVENT_BUS.post(event);
        return event;
    }
}
