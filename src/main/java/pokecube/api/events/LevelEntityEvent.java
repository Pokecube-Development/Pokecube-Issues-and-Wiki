package pokecube.api.events;

import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.entity.EntityEvent;

public class LevelEntityEvent extends EntityEvent
{
    private final byte key;

    public LevelEntityEvent(Entity entity, byte key)
    {
        super(entity);
        this.key = key;
    }

    public byte getKey()
    {
        return key;
    }

}
