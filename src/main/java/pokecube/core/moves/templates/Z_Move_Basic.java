package pokecube.core.moves.templates;

import net.minecraft.entity.Entity;
import pokecube.core.interfaces.IPokemob;

public class Z_Move_Basic extends Move_Basic
{

    public Z_Move_Basic(final String name)
    {
        super(name);
    }

    @Override
    public int getPWR(final IPokemob user, final Entity target)
    {
        // TODO adjust accordingly!
        final int pwr = super.getPWR(user, target);
        return pwr < 50 ? 100 : pwr;
    }
}
