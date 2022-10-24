package pokecube.mobs.moves.attacks.special;

import org.apache.commons.lang3.NotImplementedException;

import pokecube.api.PokecubeAPI;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.entity.pokemob.moves.MovePacket;
import pokecube.api.moves.utils.IMoveConstants;
import pokecube.api.utils.PokeType;
import pokecube.core.moves.MovesUtils;
import pokecube.core.moves.templates.Move_Basic;

public class Curse extends Move_Basic
{

    public Curse()
    {
        super("curse");
    }

    @Override
    public void postAttack(MovePacket packet)
    {
        super.postAttack(packet);
        if (packet.canceled || packet.failed) return;
        if (packet.attacker.isType(PokeType.getType("ghost")))
        {
            final IPokemob target = PokemobCaps.getPokemobFor(packet.attacked);
            boolean apply = true;
            if (target != null)
            {
                apply = false;
                if ((target.getChanges() & IMoveConstants.CHANGE_CURSE) == 0) apply = true;
            }
            if (apply)
            {
                PokecubeAPI.LOGGER.error(new NotImplementedException("curse"));
//                final MovePacket move = new MovePacket(packet.attacker, packet.attacked, this.getName(),
//                        PokeType.getType("ghost"), 0, 0, 0, IMoveConstants.CHANGE_CURSE, true);
//                if (target != null) target.onMoveUse(move);
//                if (!move.canceled)
//                {
//                    MovesUtils.addChange(packet.attacked, packet.attacker, IMoveConstants.CHANGE_CURSE);
//                    packet.attacker.getEntity().hurt(DamageSource.MAGIC,
//                            packet.attacker.getEntity().getMaxHealth() / 2);
//                }
            }
        }
        else if (packet.attacked != packet.attacker && packet.attacked != null)
        {
            packet = new MovePacket(packet.attacker, packet.attacked, this.move);
            MovesUtils.handleStats(packet.attacker, packet.attacked, packet, true);
        }
    }
}
