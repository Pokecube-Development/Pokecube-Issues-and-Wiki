package pokecube.api.moves.utils.target_types;

import pokecube.api.moves.utils.MoveApplication;
import pokecube.core.ai.brain.BrainUtils;

public class SelectedTarget implements IMoveTargetter
{
    public static final IMoveTargetter INSTANCE = new SelectedTarget();

    @Override
    public boolean test(MoveApplication move)
    {
        if (move.getTarget() == null) return false;
        return move.getTarget() == BrainUtils.getAttackTarget(move.getUser().getEntity());
    }
}
