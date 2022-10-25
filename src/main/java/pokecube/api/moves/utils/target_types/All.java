package pokecube.api.moves.utils.target_types;

import pokecube.api.moves.utils.MoveApplication;

public class All implements IMoveTargetter
{
    public static final IMoveTargetter INSTANCE = new All();

    @Override
    public boolean test(MoveApplication move)
    {
        return true;
    }
}
