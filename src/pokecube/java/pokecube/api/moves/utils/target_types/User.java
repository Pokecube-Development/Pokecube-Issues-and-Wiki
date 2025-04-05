package pokecube.api.moves.utils.target_types;

import pokecube.api.moves.utils.MoveApplication;

public class User implements IMoveTargetter
{
    public static final IMoveTargetter INSTANCE = new User();

    @Override
    public boolean test(MoveApplication move)
    {
        return move.getTarget() == move.getUser().getEntity();
    }
}
