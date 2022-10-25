package pokecube.mobs.moves.attacks;

import net.minecraft.world.entity.LivingEntity;
import pokecube.api.data.moves.MoveProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.utils.MoveApplication;
import pokecube.api.moves.utils.MoveApplication.Damage;
import pokecube.api.moves.utils.MoveApplication.PostMoveUse;
import pokecube.core.PokecubeCore;
import pokecube.core.moves.damage.PokemobDamageSource;
import thut.api.Tracker;

@MoveProvider(name = "counter")
public class Counter implements PostMoveUse
{
    @Override
    public void applyPostMove(Damage t)
    {
        MoveApplication packet = t.move();
        if (packet.canceled || packet.failed) return;
        IPokemob user = packet.getUser();
        final LivingEntity attacker = user.getEntity();
        final LivingEntity attacked = packet.getTarget();
        if (!user.getMoveStats().biding)
        {
            attacker.getPersistentData().putLong("bideTime",
                    Tracker.instance().getTick() + PokecubeCore.getConfig().attackCooldown);
            user.getMoveStats().biding = true;
            user.getMoveStats().PHYSICALDAMAGETAKENCOUNTER = 0;
        }
        else if (attacker.getPersistentData().getLong("bideTime") < Tracker.instance().getTick())
        {
            attacker.getPersistentData().remove("bideTime");
            final int damage = 2 * user.getMoveStats().PHYSICALDAMAGETAKENCOUNTER;
            user.getMoveStats().PHYSICALDAMAGETAKENCOUNTER = 0;
            if (attacked != null && !attacked.isInvulnerable())
                attacked.hurt(new PokemobDamageSource(attacker, packet.getMove()), damage);
            user.getMoveStats().biding = false;
        }
    }
}
