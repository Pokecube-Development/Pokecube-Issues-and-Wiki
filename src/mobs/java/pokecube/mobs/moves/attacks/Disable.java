package pokecube.mobs.moves.attacks;

import pokecube.api.data.moves.MoveProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.moves.utils.MoveApplication;
import pokecube.api.moves.utils.MoveApplication.Damage;
import pokecube.api.moves.utils.MoveApplication.PostMoveUse;
import pokecube.core.PokecubeCore;
import pokecube.core.moves.MovesUtils;

@MoveProvider(name = "disable")
public class Disable implements PostMoveUse
{
    @Override
    public void applyPostMove(Damage t)
    {
        MoveApplication packet = t.move();
        if (packet.canceled || packet.failed) return;
        IPokemob attacker = packet.getUser();
        if (!(packet.canceled || packet.failed))
        {
            final IPokemob target = PokemobCaps.getPokemobFor(packet.getTarget());
            if (target != null)
            {
                final int index = attacker.getEntity().getRandom().nextInt(4);
                final int timer = attacker.getEntity().getRandom().nextInt(7);
                if (target.getDisableTimer(index) <= 0 && timer > 0)
                    target.setDisableTimer(index, PokecubeCore.getConfig().attackCooldown * timer);
                else MovesUtils.displayEfficiencyMessages(attacker, packet.getTarget(), -2, 0);
            }
        }
    }
}