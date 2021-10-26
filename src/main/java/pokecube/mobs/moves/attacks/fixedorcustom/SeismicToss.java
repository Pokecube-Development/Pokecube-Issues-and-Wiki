package pokecube.mobs.moves.attacks.fixedorcustom;

import net.minecraft.world.entity.Entity;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.moves.templates.Move_Basic;

public class SeismicToss extends Move_Basic
{

    public SeismicToss()
    {
        super("seismictoss");
        this.setFixedDamage();
    }

    @Override
    public int getPWR(IPokemob user, Entity target)
    {
        return user.getLevel();
    }

}
