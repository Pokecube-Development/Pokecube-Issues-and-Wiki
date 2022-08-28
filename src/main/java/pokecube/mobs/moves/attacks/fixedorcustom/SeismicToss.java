package pokecube.mobs.moves.attacks.fixedorcustom;

import net.minecraft.world.entity.LivingEntity;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.core.moves.templates.Move_Basic;

public class SeismicToss extends Move_Basic
{

    public SeismicToss()
    {
        super("seismictoss");
        this.setFixedDamage();
    }

    @Override
    public int getPWR(IPokemob user, LivingEntity target)
    {
        return user.getLevel();
    }

}
