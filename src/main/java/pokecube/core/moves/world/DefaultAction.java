package pokecube.core.moves.world;

import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.IMoveAction;
import pokecube.api.moves.Move_Base;
import thut.api.maths.Vector3;

public class DefaultAction  implements IMoveAction
{
    Move_Base move;

    public DefaultAction(final Move_Base move)
    {
        this.move = move;
    }
    
    @Override
    public boolean applyEffect(IPokemob user, Vector3 location)
    {
        return false;
    }

    @Override
    public String getMoveName()
    {
        return this.move.name;
    }
}
