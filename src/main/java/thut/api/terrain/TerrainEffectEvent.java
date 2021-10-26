package thut.api.terrain;

import net.minecraft.entity.LivingEntity;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.eventbus.api.Cancelable;

@Cancelable
public class TerrainEffectEvent extends EntityEvent
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
