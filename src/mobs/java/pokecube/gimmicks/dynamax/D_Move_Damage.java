package pokecube.gimmicks.dynamax;

import net.minecraft.world.entity.LivingEntity;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.MoveEntry;
import pokecube.api.moves.MoveEntry.PowerProvider;

public class D_Move_Damage implements PowerProvider
{
    public static PowerProvider INSTANCE = new D_Move_Damage();

    @Override
    public int getPWR(IPokemob user, LivingEntity target, int orig_pwr)
    {
        final int index = user.getMoveIndex();
        int pwr = 60;
        if (index >= 0 && index < 4)
        {
            final MoveEntry tmp = user.getSelectedMove();
            if (tmp != null)
            {
                pwr = tmp.power;
            }
        }
        if (orig_pwr > 0) return orig_pwr;
        if (pwr < 55) return 75;
        if (pwr < 65) return 90;
        if (pwr < 75) return 105;
        if (pwr < 85) return 120;
        if (pwr < 95) return 131;
        if (pwr < 100) return 135;
        if (pwr < 110) return 138;
        if (pwr < 125) return 142;
        if (pwr < 130) return 146;
        return 150;
    }

}
