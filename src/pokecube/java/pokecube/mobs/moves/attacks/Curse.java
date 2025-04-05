package pokecube.mobs.moves.attacks;

import pokecube.api.data.moves.LoadedMove.PreProcessor;
import pokecube.api.data.moves.MoveProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.IPokemob.Stats;
import pokecube.api.moves.MoveEntry.CategoryProvider;
import pokecube.api.moves.utils.IMoveConstants;
import pokecube.api.moves.utils.IMoveConstants.ContactCategory;
import pokecube.api.moves.utils.MoveApplication;
import pokecube.api.utils.PokeType;

@MoveProvider(name = "curse")
public class Curse implements PreProcessor
{

    public CategoryProvider categoryProvider = user -> ContactCategory.RANGED;

    @Override
    public void preProcess(MoveApplication t)
    {
        IPokemob attacker = t.getUser();

        // Convert category if we are ghost type.
        if (attacker.isType(PokeType.getType("ghost")))
        {
            t.getMove().categoryProvider = categoryProvider;
            t.status_chance = 1;
            t.status_effects |= IMoveConstants.CHANGE_CURSE;
        }
        else
        {
            t.getMove().categoryProvider = t.getMove()._categoryProvider;
            // If not a ghost user, this just does stats stuff
            if (!attacker.isType(PokeType.getType("ghost")))
            {
                t.stat_chance = 1;
                t.stat_effects[Stats.VIT.ordinal()] = -1;
                t.stat_effects[Stats.ATTACK.ordinal()] = 1;
                t.stat_effects[Stats.DEFENSE.ordinal()] = 1;
            }
        }
    }
}
