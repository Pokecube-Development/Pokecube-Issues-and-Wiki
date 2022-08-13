package pokecube.mobs.moves.attacks.special;

import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.utils.PokeType;
import pokecube.core.moves.templates.Move_Basic;

public class MultiAttack extends Move_Basic
{

    public MultiAttack()
    {
        super("multiattack");
    }

    /**
     * Type getter
     *
     * @return the type of this move
     */
    @Override
    public PokeType getType(IPokemob user)
    {
        if (user == null) return this.move.type;
        return user.getType1();
    }

}
