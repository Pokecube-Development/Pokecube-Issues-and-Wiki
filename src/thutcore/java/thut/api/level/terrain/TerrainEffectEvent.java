package thut.api.level.terrain;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.event.entity.EntityEvent;

public class TerrainEffectEvent extends EntityEvent implements ICancellableEvent
{
    public final String  identifier;
    public final boolean entry;

    public TerrainEffectEvent(LivingEntity entity, String identifier, boolean entry)
    {
        super(entity);
        this.identifier = identifier;
        this.entry = entry;
    }

}
