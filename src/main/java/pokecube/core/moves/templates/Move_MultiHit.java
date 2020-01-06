package pokecube.core.moves.templates;

import java.util.Random;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import pokecube.core.PokecubeCore;
import pokecube.core.events.pokemob.combat.MoveUse;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.moves.MoveQueue.MoveQueuer;
import pokecube.core.moves.animations.EntityMoveUse;
import thut.api.maths.Vector3;

public class Move_MultiHit extends Move_Basic
{
    public Move_MultiHit(String name)
    {
        super(name);
    }

    @Override
    public void ActualMoveUse(@Nonnull Entity user, @Nullable Entity target, @Nonnull Vector3 start,
            @Nonnull Vector3 end)
    {
        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(user);
        if (pokemob == null) return;
        final int count = this.getCount(pokemob, target);
        int duration = 5;
        if (this.getAnimation(pokemob) != null) duration = this.getAnimation(pokemob).getDuration();
        for (int i = 0; i < count; i++)
        {
            if (PokecubeCore.MOVE_BUS.post(new MoveUse.ActualMoveUse.Init(pokemob, this, target))) // Move
                                                                                                   // Failed
                                                                                                   // message
                                                                                                   // here?
                break;
            final EntityMoveUse moveUse = new EntityMoveUse(EntityMoveUse.TYPE, user.getEntityWorld());
            moveUse.setUser(user).setMove(this, i * duration).setTarget(target).setStart(start).setEnd(end);
            MoveQueuer.queueMove(moveUse);
            // Setting this way results in the last one fired being the "active"
            // move, hopefully this doesn't cause any problems, if it does, see
            // about changing to addActiveMove instead of setActiveMove?
            pokemob.setActiveMove(moveUse);
        }
    }

    public int getCount(@Nonnull IPokemob user, @Nullable Entity target)
    {
        final int random = new Random().nextInt(6);
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
