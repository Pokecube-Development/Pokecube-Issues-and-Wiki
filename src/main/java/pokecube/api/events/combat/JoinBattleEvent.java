package pokecube.api.events.combat;

import javax.annotation.Nullable;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;
import pokecube.api.moves.Battle;

/**
 * This event is fired when 2 mobs try to get added to a battle. If this is
 * cancelled, the battle addition does not occur. The existing battles for the
 * two are also passed in, and may be null. This is fired on the
 * ThutCore.FORGE_BUS.
 * 
 * An example use of this event: Preventing always friendly npcs from trying to
 * engage in battle.
 * 
 * @author Thutmose
 *
 */
@Cancelable
public class JoinBattleEvent extends Event
{
    public final LivingEntity mobA;
    public final LivingEntity mobB;

    public final @Nullable Battle existingA;
    public final @Nullable Battle existingB;

    public JoinBattleEvent(LivingEntity mobA, LivingEntity mobB, @Nullable Battle existingA, @Nullable Battle existingB)
    {
        this.mobA = mobA;
        this.mobB = mobB;
        this.existingA = existingA;
        this.existingB = existingB;
    }
}
