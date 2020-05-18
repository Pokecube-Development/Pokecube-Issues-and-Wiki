package pokecube.mobs.moves.attacks.special;

import pokecube.core.ai.tasks.combat.AIFindTarget;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.interfaces.pokemob.moves.MovePacket;
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
        final IPokemob attacked = CapabilityPokemob.getPokemobFor(packet.attacked);
        if (attacked != null)
        {
            if (attacked.getLevel() > packet.attacker.getLevel()) // TODO
                                                                  // message
                                                                  // here for
                                                                  // move
                                                                  // failing;
                return;
            if (attacked.getGeneralState(GeneralStates.TAMED)) attacked.onRecall();
            attacked.setCombatState(CombatStates.ANGRY, false);
        }
        // ends the battle
        AIFindTarget.deagro(packet.attacker.getEntity());
    }
}
