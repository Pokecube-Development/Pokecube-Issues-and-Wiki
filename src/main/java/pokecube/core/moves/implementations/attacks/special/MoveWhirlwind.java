package pokecube.core.moves.implementations.attacks.special;

import net.minecraft.entity.MobEntity;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.interfaces.pokemob.moves.MovePacket;
import pokecube.core.moves.templates.Move_Basic;

public class MoveWhirlwind extends Move_Basic
{

    public MoveWhirlwind()
    {
        super("whirlwind");
    }

    public MoveWhirlwind(String string)
    {
        super(string);
    }

    @Override
    public void postAttack(MovePacket packet)
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
        if (packet.attacked instanceof MobEntity) ((MobEntity) packet.attacked).setAttackTarget(null);
        packet.attacker.setCombatState(CombatStates.ANGRY, false);
        packet.attacker.getEntity().setAttackTarget(null);
    }
}
