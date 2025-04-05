package pokecube.api.moves.utils.target_types;

import pokecube.api.moves.utils.MoveApplication;

public class AllOther implements IMoveTargetter
{
    public static final IMoveTargetter INSTANCE = new AllOther();

    @Override
    public boolean test(MoveApplication move)
    {
        if (move.getTarget() == null) return false;
        return move.getTarget() != move.getUser().getEntity();
    }
}
