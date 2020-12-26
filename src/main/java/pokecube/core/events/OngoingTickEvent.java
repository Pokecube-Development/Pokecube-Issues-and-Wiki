package pokecube.core.events;

import net.minecraft.entity.Entity;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.eventbus.api.Cancelable;
import pokecube.core.interfaces.entity.IOngoingAffected.IOngoingEffect;

/**
 * This event is fired on the MinecraftForge.EVENT_BUS. If canceled, the
 * ongoing effect for the entity will not be ticked, and will not have its
 * duration dropped, or be removed when duration reaches 0
 */
@Cancelable
public class OngoingTickEvent extends EntityEvent
{
    public final IOngoingEffect effect;

    private int duration;

    public OngoingTickEvent(final Entity entity, final IOngoingEffect effect)
    {
        super(entity);
        this.effect = effect;
        this.duration = effect.getDuration();
        if (this.duration > 0) this.duration = this.duration - 1;
    }

    public int getDuration()
    {
        return this.duration;
    }

    public void setDuration(final int remainingDuration)
    {
        this.duration = remainingDuration;
    }

}
