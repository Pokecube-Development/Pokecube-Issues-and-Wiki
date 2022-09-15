package pokecube.core.moves.templates;

import net.minecraft.world.entity.LivingEntity;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.Move_Base;
import pokecube.core.moves.zmoves.GZMoveManager;

public class Z_Move_Basic extends Move_Basic
{

    public Z_Move_Basic(final String name)
    {
        super(name);
    }

    // TODO handle status effect moves in here as well somehow.

    @Override
    public int getPWR(final IPokemob user, final LivingEntity target)
    {
        final int index = user.getMoveIndex();
        Move_Base base = this;
        if (index >= 0 && index < 4)
        {
            final Move_Base tmp = user.getSelectedMove();
            if (tmp != null) base = tmp;
        }
        final int pwr = base.getPWR(user, target);
        if (GZMoveManager.isZMove(this.move.baseEntry))
        {
            if (base.move.baseEntry.zMovePower > 0) return base.move.baseEntry.zMovePower;
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
        else
        {
            if (base.move.baseEntry.gMovePower > 0) return base.move.baseEntry.gMovePower;
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
}
