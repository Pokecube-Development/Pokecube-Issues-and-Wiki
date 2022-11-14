/**
 *
 */
package pokecube.mobs.moves.attacks;

import net.minecraft.world.entity.LivingEntity;
import pokecube.api.data.moves.LoadedMove.PreProcessor;
import pokecube.api.data.moves.MoveProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.moves.MoveEntry;
import pokecube.api.moves.utils.IMoveAnimation;
import pokecube.api.moves.utils.IMoveConstants;
import pokecube.api.moves.utils.MoveApplication;
import pokecube.api.moves.utils.MoveApplication.Damage;
import pokecube.api.moves.utils.MoveApplication.PostMoveUse;

/** @author Manchou */
@MoveProvider(name = "transform")
public class Transform implements PostMoveUse, PreProcessor
{

    public static class Animation implements IMoveAnimation
    {
        @Override
        public int getApplicationTick()
        {
            return 0;
        }

        @Override
        public int getDuration()
        {
            return 0;
        }

        @Override
        public void setDuration(final int arg0)
        {}

    }

    @Override
    public void applyPostMove(Damage t)
    {
        MoveApplication packet = t.move();
        if (packet.canceled || packet.failed) return;

        if (packet.getTarget() != packet.getUser().getMoveStats().targetEnemy) return;

        IPokemob attacker = packet.getUser();
        LivingEntity attacked = packet.getTarget();

        if (attacked instanceof LivingEntity && attacked != attacker.getTransformedTo())
            attacker.setTransformedTo(attacked);
    }

    @Override
    public void preProcess(MoveApplication t)
    {
        if (t.getUser().getTransformedTo() != null)
        {
            IPokemob mob = PokemobCaps.getPokemobFor(t.getUser().getTransformedTo());
            // Re-direct to default move if this is the case.
            if (mob == null) t.setMove(MoveEntry.get(IMoveConstants.DEFAULT_MOVE));
            else t.failed = true;
        }
    }
}
