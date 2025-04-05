package pokecube.mobs.moves.attacks;

import pokecube.api.data.moves.LoadedMove.PreProcessor;
import pokecube.api.data.moves.MoveProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.stats.DefaultModifiers;
import pokecube.api.moves.utils.IMoveConstants;
import pokecube.api.moves.utils.MoveApplication;

@MoveProvider(name = "acupressure")
public class Acupressure implements PreProcessor
{
    @Override
    public void preProcess(MoveApplication t)
    {
        IPokemob attacker = t.getUser();

        final DefaultModifiers modifiers = attacker.getModifiers().getDefaultMods();

        var r = attacker.getEntity().getRandom();
        int rand = r.nextInt(7);
        for (int i = 0; i < 8; i++)
        {
            final int stat = rand;
            boolean valid = modifiers.values[stat] < 6;
            if (valid)
            {
                t.stat_effects[stat] = IMoveConstants.SHARP;
                return;
            }
            rand = (rand + 1) % 7;
        }

    }
}
