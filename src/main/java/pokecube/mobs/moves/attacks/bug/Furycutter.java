package pokecube.mobs.moves.attacks.bug;

import net.minecraft.entity.Entity;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.pokemob.moves.MovePacket;
import pokecube.core.moves.templates.Move_Basic;

public class Furycutter extends Move_Basic
{

    public Furycutter()
    {
        super("furycutter");
    }

    @Override
    public int getPWR(IPokemob attacker, Entity attacked)
    {
        final double rollOut = attacker.getMoveStats().FURYCUTTERCOUNTER;
        final int PWR = (int) Math.max(this.getPWR(), Math.min(160, rollOut * 2 * this.getPWR()));
        return PWR;
    }

    @Override
    public void onAttack(MovePacket packet)
    {
        super.onAttack(packet);
        if (packet.damageDealt == 0) packet.attacker.getMoveStats().FURYCUTTERCOUNTER = 0;
        else packet.attacker.getMoveStats().FURYCUTTERCOUNTER++;
    }
}
