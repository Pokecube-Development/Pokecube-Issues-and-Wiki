package pokecube.mobs.moves.attacks.psychic;

import net.minecraft.world.entity.Entity;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.moves.templates.Move_Basic;

public class Move_Psywave extends Move_Basic
{

    public Move_Psywave()
    {
        super("psywave");
    }

    @Override
    public int getPWR(IPokemob user, Entity target)
    {
        final int lvl = user.getLevel();
        final int pwr = (int) Math.max(1, lvl * (Math.random() + 0.5));

        return pwr;
    }

}
