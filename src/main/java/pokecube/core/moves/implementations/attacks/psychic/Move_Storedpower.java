package pokecube.core.moves.implementations.attacks.psychic;

import net.minecraft.entity.Entity;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.Stats;
import pokecube.core.interfaces.pokemob.stats.DefaultModifiers;
import pokecube.core.moves.templates.Move_Basic;

public class Move_Storedpower extends Move_Basic
{

    public Move_Storedpower()
    {
        super("storedpower");
    }

    @Override
    public int getPWR(IPokemob user, Entity target)
    {
        int pwr = 20;
        final DefaultModifiers mods = user.getModifiers().getDefaultMods();
        for (final Stats stat : Stats.values())
        {
            final float b = mods.getModifierRaw(stat);
            if (b > 0) pwr += 20 * b;
        }
        return pwr;
    }

}
