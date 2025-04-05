package pokecube.mobs.moves.attacks;

import pokecube.api.data.moves.MoveProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.utils.IMoveConstants;
import pokecube.api.moves.utils.MoveApplication;
import pokecube.api.moves.utils.MoveApplication.Damage;
import pokecube.api.moves.utils.MoveApplication.HealProvider;

@MoveProvider(name = "rest")
public class Rest implements HealProvider
{
    @Override
    public void applyHealing(Damage t)
    {
        MoveApplication packet = t.move();
        if (packet.canceled || packet.failed) return;
        IPokemob attacker = packet.getUser();
        attacker.healStatus();
        attacker.healChanges();
        attacker.setStatus(attacker, IMoveConstants.STATUS_SLP, 2);
    }
}
