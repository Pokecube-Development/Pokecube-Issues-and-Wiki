package pokecube.core.moves.templates;

import net.minecraft.entity.Entity;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.moves.MovesUtils;

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
        int pwr = super.getPWR(user, target);
        final int index = user.getMoveIndex();
        if (index >= 0 && index < 4)
        {
            final String base_name = user.getMoveStats().moves[user.getMoveIndex()];
            final Move_Base base = MovesUtils.getMoveFromName(base_name);
            if (base != null) pwr = base.getPWR(user, target);
        }
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
