package pokecube.api.moves.utils.target_types;

import pokecube.api.entity.TeamManager;
import pokecube.api.moves.utils.MoveApplication;

public class Ally implements IMoveTargetter
{
    public static final IMoveTargetter INSTANCE = new Ally();

    @Override
    public boolean test(MoveApplication move)
    {
        return TeamManager.sameTeam(move.getUser().getEntity(), move.getTarget());
    }
}
