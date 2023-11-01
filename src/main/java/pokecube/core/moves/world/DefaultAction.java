package pokecube.core.moves.world;

import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.MoveEntry;
import pokecube.api.moves.utils.IMoveWorldEffect;
import thut.api.maths.Vector3;

public class DefaultAction implements IMoveWorldEffect
{
    MoveEntry move;

    public DefaultAction(final MoveEntry move)
    {
        this.move = move;
    }

    @Override
    public boolean applyOutOfCombat(IPokemob user, Vector3 location)
    {
        return false;
    }

    @Override
    public String getMoveName()
    {
        return this.move.name;
    }
}
