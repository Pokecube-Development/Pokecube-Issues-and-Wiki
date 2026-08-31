package pokecube.api.events.combat;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

/**
 * Cancelling this event will prevent population of the memory for valid combat targets.
 * This is called while populating the list of valid targets for either a pokemob or an npc
 * <p>
 * This event is fired on the ThutCore.FORGE_BUS, which should be the regular game bus for Neoforge.
 */
public class ValidBattleTarget extends Event implements ICancellableEvent
{
    public final LivingEntity agressor;
    public final LivingEntity target;

    public ValidBattleTarget(LivingEntity agressor, LivingEntity target)
    {
        this.agressor = agressor;
        this.target = target;
    }
}
