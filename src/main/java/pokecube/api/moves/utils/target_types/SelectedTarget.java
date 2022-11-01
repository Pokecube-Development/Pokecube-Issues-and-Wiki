package pokecube.api.moves.utils.target_types;

import pokecube.api.data.moves.MoveApplicationRegistry;
import pokecube.api.moves.utils.MoveApplication;

public class SelectedTarget implements IMoveTargetter
{
    public static final IMoveTargetter INSTANCE = new SelectedTarget();

    @Override
    public boolean test(MoveApplication move)
    {
        if (move.getTarget() == null || move.getTarget() == move.getUser().getEntity()) return false;
        boolean targetsAllyIfPossible = MoveApplicationRegistry.targetsAllyIfPossible(move);
        boolean valid = false;
        // Check if we target ally first, if so, then return true if the target
        // is the ally
        if (targetsAllyIfPossible)
        {
            boolean isTargetAlly = move.getTarget() == move.getUser().getMoveStats().targetAlly;
            if (isTargetAlly) valid = true;
        }

        if (!valid)
        {
            boolean noAllyTarget = move.getUser().getMoveStats().targetAlly == null
                    || move.getUser().getMoveStats().targetAlly == move.getUser().getEntity();

            // If if doesn't target allies, or there is no selected ally, then
            // let
            // the move apply to enemy.
            if (!targetsAllyIfPossible || noAllyTarget)
            {
                valid = move.getTarget() == move.getUser().getMoveStats().targetEnemy;
            }
        }
        return valid;
    }
}
