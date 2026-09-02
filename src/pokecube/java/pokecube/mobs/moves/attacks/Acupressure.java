package pokecube.mobs.moves.attacks;

import pokecube.api.data.moves.LoadedMove.PreProcessor;
import pokecube.api.data.moves.MoveProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.utils.IMoveConstants;
import pokecube.api.moves.utils.MoveApplication;
import pokecube.core.moves.damage.attributes.PokecubeAttributes;

@MoveProvider(name = "acupressure")
public class Acupressure implements PreProcessor
{
    @Override
    public void preProcess(MoveApplication t)
    {
        IPokemob attacker = t.getUser();

        var r = attacker.getEntity().getRandom();
        int rand = r.nextInt(7);
        for (int i = 0; i < 8; i++)
        {
            IPokemob.Stats stat = IPokemob.Stats.values()[rand];
            if (stat != IPokemob.Stats.HP)
            {
                boolean valid = PokecubeAttributes.getModifier(t.getUserEntity(), stat) < 6;
                if (valid)
                {
                    t.stat_chance = 1;
                    t.stat_effects[rand] = IMoveConstants.SHARP;
                    return;
                }
            }
            rand = (rand + 1) % 7;
        }

    }
}
