package pokecube.api.events.combat;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.Event;
import pokecube.api.moves.Battle;

/**
 * Called when a mob is removed from battle. This event is not cancellable, and
 * mostly serves as a notification that the mob was removed from battle. The mob
 * may be dead or discarded when this occurs.
 * 
 * @author Thutmose
 *
 */
public class ExitBattleEvent extends Event
{
    public final LivingEntity mob;
    public final Battle battle;

    public ExitBattleEvent(LivingEntity mob, Battle battle)
    {
        this.mob = mob;
        this.battle = battle;
    }
}
