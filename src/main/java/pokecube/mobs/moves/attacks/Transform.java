/**
 *
 */
package pokecube.mobs.moves.attacks;

import net.minecraft.world.entity.LivingEntity;
import pokecube.api.data.moves.LoadedMove.PreProcessor;
import pokecube.api.data.moves.MoveProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.MoveEntry;
import pokecube.api.moves.utils.IMoveAnimation;
import pokecube.api.moves.utils.IMoveConstants;
import pokecube.api.moves.utils.MoveApplication;
import pokecube.api.moves.utils.MoveApplication.Damage;
import pokecube.api.moves.utils.MoveApplication.PostMoveUse;
import pokecube.core.ai.brain.BrainUtils;

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
        IPokemob attacker = packet.getUser();
        LivingEntity attacked = packet.getTarget();
        if (attacked instanceof LivingEntity && attacked != attacker.getTransformedTo())
        {
            BrainUtils.initiateCombat(attacker.getEntity(), attacked);
            attacker.setTransformedTo(attacked);
        }
    }

    @Override
    public void preProcess(MoveApplication t)
    {
        if (t.getUser().getTransformedTo() != null)
        {
            // Re-direct to default move if this is the case.
            t.setMove(MoveEntry.get(IMoveConstants.DEFAULT_MOVE));
        }
    }
}
