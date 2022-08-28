package pokecube.mobs.moves.attacks.special;

import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.entity.pokemob.ai.CombatStates;
import pokecube.api.entity.pokemob.ai.GeneralStates;
import pokecube.api.entity.pokemob.moves.MovePacket;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.moves.MovesUtils;
import pokecube.core.moves.templates.Move_Basic;

public class Whirlwind extends Move_Basic
{

    public Whirlwind()
    {
        super("whirlwind");
    }

    public Whirlwind(final String string)
    {
        super(string);
    }

    @Override
    public void postAttack(final MovePacket packet)
    {
        super.postAttack(packet);
        if (packet.canceled || packet.failed) return;
        final IPokemob attacked = PokemobCaps.getPokemobFor(packet.attacked);
        if (attacked != null)
        {
            if (attacked.getLevel() > packet.attacker.getLevel())
            {
                MovesUtils.displayEfficiencyMessages(packet.attacker, packet.attacked, -2, 0);
                return;
            }
            if (attacked.getGeneralState(GeneralStates.TAMED)) attacked.onRecall();
            attacked.setCombatState(CombatStates.ANGRY, false);
        }
        // ends the battle
        BrainUtils.deagro(packet.attacker.getEntity());
    }
}
