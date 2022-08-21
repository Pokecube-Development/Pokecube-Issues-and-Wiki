package pokecube.core.moves.templates;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.world.entity.Entity;
import pokecube.api.PokecubeAPI;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.events.pokemobs.combat.MoveUse;
import pokecube.core.moves.MoveQueue.MoveQueuer;
import pokecube.core.moves.animations.EntityMoveUse;
import thut.api.maths.Vector3;
import thut.core.common.ThutCore;

public class Move_MultiHit extends Move_Basic
{
    public Move_MultiHit(final String name)
    {
        super(name);
    }

    @Override
    public void ActualMoveUse(@Nonnull final Entity user, @Nullable final Entity target, @Nonnull final Vector3 start,
            @Nonnull final Vector3 end)
    {
        final IPokemob pokemob = PokemobCaps.getPokemobFor(user);
        if (pokemob == null) return;
        final int count = this.getCount(pokemob, target);
        int duration = 5;
        if (this.getAnimation(pokemob) != null) duration = this.getAnimation(pokemob).getDuration();
        for (int i = 0; i < count; i++)
        {
            // TODO move failed message here?
            if (PokecubeAPI.MOVE_BUS.post(new MoveUse.ActualMoveUse.Init(pokemob, this, target))) break;
            final EntityMoveUse moveUse = EntityMoveUse.Builder.make(user, this, start).setEnd(end).setTarget(target)
                    .setStartTick(i * duration).build();
            MoveQueuer.queueMove(moveUse);

            // Setting this way results in the last one fired being the "active"
            // move, hopefully this doesn't cause any problems, if it does, see
            // about changing to addActiveMove instead of setActiveMove?
            pokemob.setActiveMove(moveUse);
        }
    }

    public int getCount(@Nonnull final IPokemob user, @Nullable final Entity target)
    {
        final int random = ThutCore.newRandom().nextInt(6);
        switch (random)
        {
        case 1:
            return 2;
        case 2:
            return 3;
        case 3:
            return 3;
        case 4:
            return 4;
        case 5:
            return 5;
        default:
            return 2;
        }
    }
}
