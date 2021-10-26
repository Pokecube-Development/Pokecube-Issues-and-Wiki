package pokecube.mobs.moves.attacks.fixedorcustom;

import net.minecraft.entity.Entity;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.moves.templates.Move_Basic;

public class Nighshade extends Move_Basic
{

    public Nighshade()
    {
        super("nightshade");
        this.setFixedDamage();
    }

    @Override
    public int getPWR(IPokemob user, Entity target)
    {
        return user.getLevel();
    }

}
