package pokecube.mobs.moves.attacks;

import pokecube.api.data.moves.MoveProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.moves.utils.MoveApplication;
import pokecube.api.moves.utils.MoveApplication.Damage;
import pokecube.api.moves.utils.MoveApplication.PostMoveUse;
import pokecube.core.PokecubeCore;
import pokecube.core.moves.MovesUtils;

@MoveProvider(name = "protect")
public class Protect implements PostMoveUse
{
    @Override
    public void applyPostMove(Damage t) {
        MoveApplication packet = t.move();

        IPokemob attacker = packet.getUser();
        final IPokemob target = PokemobCaps.getPokemobFor(packet.getTarget());

        if (packet.canceled || packet.failed || target == null) return;

    }
}
