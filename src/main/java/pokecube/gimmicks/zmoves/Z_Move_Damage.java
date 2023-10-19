package pokecube.gimmicks.zmoves;

import net.minecraft.world.entity.LivingEntity;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.MoveEntry;
import pokecube.api.moves.MoveEntry.PowerProvider;

public class Z_Move_Damage implements PowerProvider
{
    public static PowerProvider INSTANCE = new Z_Move_Damage();

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
