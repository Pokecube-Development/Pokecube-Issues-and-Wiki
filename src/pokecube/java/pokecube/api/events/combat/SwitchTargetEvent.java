package pokecube.api.events.combat;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import pokecube.api.entity.pokemob.IPokemob;

/**
 * This event is fired when a pokemob attempts to switch target in battle.
 * This can either be from the owner sending the command for index, or when the
 * pokemob is checking if the combat order has re-arranged.
 *
 * This is fired on the ThutCore.FORGE_BUS.
 * 
 * An example use of this event: Preventing wild mobs re-agressing the player
 *
 * If cancelled, the target is not changed. otherwise, target changes to whatever is in newTarget
 * 
 * @author Thutmose
 *
 */
public class SwitchTargetEvent extends Event implements ICancellableEvent
{
    public final LivingEntity agressor, oldTarget;
    public final IPokemob agroMob;
    private LivingEntity newTarget;

    public SwitchTargetEvent(IPokemob agressor, LivingEntity oldTarget, LivingEntity newTarget)
    {
        this.agressor = agressor.getEntity();
        this.agroMob = agressor;
        this.oldTarget = oldTarget;
        setNewTarget(newTarget);
    }

    public LivingEntity getNewTarget()
    {
        return newTarget;
    }

    public void setNewTarget(LivingEntity newTarget)
    {
        this.newTarget = newTarget;
    }
}
