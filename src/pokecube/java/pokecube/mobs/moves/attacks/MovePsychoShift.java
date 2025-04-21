package pokecube.mobs.moves.attacks;

import pokecube.api.data.moves.MoveProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.moves.utils.MoveApplication;
import pokecube.api.moves.utils.MoveApplication.Damage;
import pokecube.api.moves.utils.MoveApplication.PostMoveUse;
import pokecube.core.moves.MovesUtils;
import pokecube.core.moves.damage.effects.StatusEffects;

@MoveProvider(name = "psycho-shift")
public class MovePsychoShift implements PostMoveUse
{
    @Override
    public void applyPostMove(Damage t)
    {
        MoveApplication packet = t.move();
        if (packet.canceled || packet.failed) return;
        IPokemob attacker = packet.getUser();

        boolean failed = !StatusEffects.hasAnyStatusEffects(attacker.getEntity());
        final IPokemob hit = PokemobCaps.getPokemobFor(packet.getTarget());
        if (hit != null && !failed)
        {
            var existing = StatusEffects.getStatusEffect(attacker.getEntity());
            if (StatusEffects.hasAnyStatusEffects(hit.getEntity())) failed = true;
            else if (StatusEffects.setStatus(hit.getEntity(), attacker.getEntity(), existing)) attacker.healStatus();
            else failed = true;
        }
        if (failed) MovesUtils.displayEfficiencyMessages(attacker, packet.getTarget(), -2, 0);
    }

}