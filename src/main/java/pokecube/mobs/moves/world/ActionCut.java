package pokecube.mobs.moves.world;

import net.minecraft.core.Direction;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.utils.IMoveWorldEffect;
import pokecube.core.PokecubeCore;
import pokecube.core.eventhandlers.MoveEventsHandler;
import pokecube.core.moves.TreeRemover;
import thut.api.maths.Vector3;
import thut.core.common.ThutCore;

public class ActionCut implements IMoveWorldEffect
{
    public ActionCut()
    {
    }

    @Override
    public boolean applyOutOfCombat(final IPokemob user, final Vector3 location)
    {
        boolean used = false;
        int count = 10;
        final int level = user.getLevel();
        final int hungerValue = PokecubeCore.getConfig().pokemobLifeSpan / 8;
        if (!MoveEventsHandler.canAffectBlock(user, location, this.getMoveName())) return false;
        TreeRemover remover = new TreeRemover(user.getEntity().getLevel(), user, this.getMoveName(), location);
        int cut = remover.cut(true);
        if (cut == 0)
        {
            final int index = ThutCore.newRandom().nextInt(6);
            for (int i = 0; i < 6; i++)
            {
                final Direction dir = Direction.values()[(i + index) % 6];
                remover = new TreeRemover(user.getEntity().getLevel(), user, this.getMoveName(), location.offset(
                        dir));
                cut = remover.cut(true);
                if (cut != 0) break;
            }
        }
        count = (int) Math.max(1, Math.ceil(cut * hungerValue * Math.pow((100 - level) / 100d, 3)));
        if (count > 0)
        {
            remover.cut(false);
            used = true;
            user.applyHunger(count);
        }
        remover.clear();
        return used;
    }

    @Override
    public String getMoveName()
    {
        return "cut";
    }
}
