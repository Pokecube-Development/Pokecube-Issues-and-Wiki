package pokecube.mobs.moves.attacks.special;

import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.entity.pokemob.moves.MovePacket;
import pokecube.core.PokecubeCore;
import pokecube.core.moves.MovesUtils;
import pokecube.core.moves.templates.Move_Basic;

public class Disable extends Move_Basic
{

    public Disable()
    {
        super("disable");
    }

    @Override
    public void onAttack(MovePacket packet)
    {
        super.onAttack(packet);
        if (!(packet.canceled || packet.failed || packet.denied))
        {
            final IPokemob target = PokemobCaps.getPokemobFor(packet.attacked);
            if (target != null)
            {
                final int index = packet.attacker.getEntity().getRandom().nextInt(4);
                final int timer = packet.attacker.getEntity().getRandom().nextInt(7);
                if (target.getDisableTimer(index) <= 0 && timer > 0)
                    target.setDisableTimer(index, PokecubeCore.getConfig().attackCooldown * timer);
                else MovesUtils.displayEfficiencyMessages(packet.attacker, packet.attacked, -2, 0);
            }
        }
    }
}