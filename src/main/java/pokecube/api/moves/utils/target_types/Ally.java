package pokecube.api.moves.utils.target_types;

import net.minecraft.world.entity.Mob;
import pokecube.api.entity.TeamManager;
import pokecube.api.moves.Battle;
import pokecube.api.moves.utils.MoveApplication;

public class Ally implements IMoveTargetter
{
    public static final IMoveTargetter INSTANCE = new Ally();

    @Override
    public boolean test(MoveApplication move)
    {
        Mob mob = move.getUser().getEntity();
        Battle battle = Battle.getBattle(mob);
        // Check battle allies (incase not on same team)
        if (battle != null && battle.getAllies(mob).contains(move.getTarget())) return true;
        // And also check team members.
        return TeamManager.sameTeam(move.getUser().getEntity(), move.getTarget());
    }
}
