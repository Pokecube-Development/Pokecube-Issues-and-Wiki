package thut.api.entity.event;

import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityEvent;

/**
 * Called when the vanilla broadcastEntityEvent occurs. This is called both
 * server and client side. this event is fired on the {@link MinecraftForge#EVENT_BUS}
 */
public class LevelEntityEvent extends EntityEvent
{
    private final byte key;

    public LevelEntityEvent(Entity entity, byte key)
    {
        super(entity);
        this.key = key;
    }

    /**
     * See {@link net.minecraft.world.entity.EntityEvent} for what these keys
     * are.
     * 
     * @return event type
     */
    public byte getKey()
    {
        return key;
    }

}
