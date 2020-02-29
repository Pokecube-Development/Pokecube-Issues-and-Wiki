package pokecube.core.moves.templates;

import net.minecraft.entity.Entity;
import pokecube.core.interfaces.IPokemob;

public class Z_Move_Basic extends Move_Basic
{

    public Z_Move_Basic(final String name)
    {
        super(name);
    }

    // TODO handle status effect moves in here as well somehow.

    @Override
    public int getPWR(final IPokemob user, final Entity target)
    {
        final int pwr = super.getPWR(user, target);
        if (pwr < 55) return 100;
        if (pwr < 65) return 120;
        if (pwr < 75) return 140;
        if (pwr < 85) return 160;
        if (pwr < 95) return 175;
        if (pwr < 100) return 180;
        if (pwr < 110) return 185;
        if (pwr < 125) return 190;
        if (pwr < 130) return 195;
        return 200;
    }
}
