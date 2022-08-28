package pokecube.mobs.moves.attacks.fixedorcustom;

import net.minecraft.world.entity.LivingEntity;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.core.moves.templates.Move_Basic;

public class Nighshade extends Move_Basic
{

    public Nighshade()
    {
        super("nightshade");
        this.setFixedDamage();
    }

    @Override
    public int getPWR(IPokemob user, LivingEntity target)
    {
        return user.getLevel();
    }

}
