package pokecube.api.events.pokemobs.combat;

import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.eventbus.api.Cancelable;
import pokecube.api.entity.IOngoingAffected.IOngoingEffect;

/**
 * This event is fired on the ThutCore.FORGE_BUS. If canceled, the ongoing
 * effect for the entity will not be ticked, and will not have its duration
 * dropped, or be removed when duration reaches 0
 */
@Cancelable
public class OngoingTickEvent extends EntityEvent
{
    public final IOngoingEffect effect;

    private int duration;
    private final boolean onDamage;

    public OngoingTickEvent(final Entity entity, final IOngoingEffect effect, boolean onDamage)
    {
        super(entity);
        this.effect = effect;
        this.duration = effect.getDuration();
        this.onDamage = onDamage;
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

    /**
     * @return if this is called during tickDamage, this is true, otherwise
     *         false.
     */
    public boolean isOnDamage()
    {
        return onDamage;
    }

}
