package pokecube.mobs.moves.attacks.rock;

import net.minecraft.world.entity.Entity;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.moves.MovePacket;
import pokecube.core.moves.templates.Move_Basic;

public class Rollout extends Move_Basic
{

    public Rollout()
    {
        super("rollout");
    }

    public Rollout(String string)
    {
        super(string);
    }

    @Override
    public int getPWR(IPokemob attacker, Entity attacked)
    {
        final double defCurl = attacker.getMoveStats().DEFENSECURLCOUNTER > 0 ? 2 : 1;
        double rollOut = attacker.getMoveStats().ROLLOUTCOUNTER;
        if (rollOut > 4) rollOut = attacker.getMoveStats().ROLLOUTCOUNTER = 0;
        rollOut = Math.max(0, rollOut);
        return (int) (Math.pow(2, rollOut) * this.getPWR() * defCurl);
    }

    @Override
    public void onAttack(MovePacket packet)
    {
        super.onAttack(packet);
        if (packet.damageDealt == 0) packet.attacker.getMoveStats().ROLLOUTCOUNTER = 0;
        else packet.attacker.getMoveStats().ROLLOUTCOUNTER++;
    }

}
