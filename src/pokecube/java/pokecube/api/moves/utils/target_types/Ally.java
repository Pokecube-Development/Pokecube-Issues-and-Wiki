package pokecube.api.moves.utils.target_types;

import pokecube.api.entity.TeamManager;
import pokecube.api.moves.Battle;
import pokecube.api.moves.utils.MoveApplication;

public class Ally implements IMoveTargetter
{
    public static final IMoveTargetter INSTANCE = new Ally();

    @Override
    public boolean test(MoveApplication move)
    {
        var mob = move.getUserEntity();
        Battle battle = Battle.getBattle(mob);
        // Check battle allies (incase not on same team)
        if (battle != null && battle.getAllies(mob).contains(move.getTarget())) return true;
        // And also check team members.
        return TeamManager.sameTeam(mob, move.getTarget());
    }
}
