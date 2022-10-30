package pokecube.mobs.moves.attacks;

import pokecube.api.data.moves.MoveProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.entity.pokemob.ai.GeneralStates;
import pokecube.api.moves.utils.MoveApplication;
import pokecube.api.moves.utils.MoveApplication.Damage;
import pokecube.api.moves.utils.MoveApplication.PostMoveUse;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.moves.MovesUtils;

@MoveProvider(name =
{ "whirlwind", "roar" })
public class Whirlwind implements PostMoveUse
{
    @Override
    public void applyPostMove(Damage t)
    {
        MoveApplication packet = t.move();
        if (packet.canceled || packet.failed) return;
        final IPokemob attacked = PokemobCaps.getPokemobFor(packet.getTarget());
        if (attacked != null)
        {
            if (attacked.getLevel() > packet.getUser().getLevel())
            {
                MovesUtils.displayEfficiencyMessages(packet.getUser(), packet.getTarget(), -2, 0);
                return;
            }
            if (attacked.getGeneralState(GeneralStates.TAMED)) attacked.onRecall();
        }
        // ends the battle
        BrainUtils.deagro(packet.getUser().getEntity());
    }
}
