package pokecube.mobs.moves.attacks;

import pokecube.api.data.moves.MoveProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.utils.MoveApplication;
import pokecube.api.moves.utils.MoveApplication.Damage;
import pokecube.api.moves.utils.MoveApplication.PostMoveUse;

@MoveProvider(name = {"rollout", "ice-ball"})
public class Rollout implements PostMoveUse
{
    @Override
    public void applyPostMove(Damage t)
    {
        MoveApplication packet = t.move();
        IPokemob attacker = packet.getUser();
        if (!packet.hit) attacker.getMoveStats().ROLLOUTCOUNTER = 0;
        else attacker.getMoveStats().ROLLOUTCOUNTER++;
    }
}
