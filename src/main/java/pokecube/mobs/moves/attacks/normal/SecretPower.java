package pokecube.mobs.moves.attacks.normal;

import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.moves.MovePacket;
import pokecube.api.moves.IMoveAnimation;
import pokecube.core.moves.templates.Move_Basic;

public class SecretPower extends Move_Basic
{
    public SecretPower()
    {
        super("secretpower");
    }

    @Override
    public IMoveAnimation getAnimation(IPokemob user)
    {
        // TODO make this return animations for the relevant attacks based on
        // location instead.
        return super.getAnimation();
    }

    @Override
    public void preAttack(MovePacket packet)
    {
        // TODO before super call, add in the needed stats/status/change effects
        // based on terrain.
        super.preAttack(packet);
    }
}
