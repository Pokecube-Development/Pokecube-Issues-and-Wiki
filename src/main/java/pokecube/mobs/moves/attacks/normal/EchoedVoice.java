package pokecube.mobs.moves.attacks.normal;

import net.minecraft.world.entity.Entity;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.moves.MovePacket;
import pokecube.core.moves.templates.Move_Basic;

public class EchoedVoice extends Move_Basic
{

    public EchoedVoice()
    {
        super("echoedvoice");
    }

    @Override
    public int getPWR(IPokemob attacker, Entity attacked)
    {
        final double rollOut = attacker.getMoveStats().FURYCUTTERCOUNTER;
        final int PWR = (int) Math.max(this.getPWR(), Math.min(200, rollOut * 2 * this.getPWR()));
        return PWR;
    }

    @Override
    public void onAttack(MovePacket packet)
    {
        super.onAttack(packet);
        packet.attacker.getMoveStats().FURYCUTTERCOUNTER++;
    }
}