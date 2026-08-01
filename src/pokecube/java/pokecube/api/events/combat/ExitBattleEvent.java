package pokecube.api.events.combat;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import pokecube.api.moves.Battle;

/**
 * Called when a mob is removed from battle. This event during normal use ignore cancelation, and
 * mostly serves as a notification that the mob was removed from battle. The mob
 * may be dead or discarded when this occurs. Context is null in this case.
 *
 * When this event is fired from PacketBattleTargets, it is the player trying to exit a battle, so
 * will be treated as an attempt to flee.
 * 
 * @author Thutmose
 *
 */
public class ExitBattleEvent extends Event implements ICancellableEvent
{
    public final LivingEntity context;
    public final LivingEntity mob;
    public final Battle battle;

    public ExitBattleEvent(LivingEntity context, LivingEntity mob, Battle battle)
    {
        this.mob = mob;
        this.battle = battle;
        this.context = context;
    }

    public ExitBattleEvent(LivingEntity mob, Battle battle)
    {
        this(null, mob, battle);
    }
}
