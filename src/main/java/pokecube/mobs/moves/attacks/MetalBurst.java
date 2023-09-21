package pokecube.mobs.moves.attacks;

import net.minecraft.world.entity.LivingEntity;
import pokecube.api.data.moves.MoveProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.utils.MoveApplication.Damage;
import pokecube.api.moves.utils.MoveApplication.PostMoveUse;
import pokecube.core.PokecubeCore;
import pokecube.core.moves.damage.PokemobDamageSource;
import thut.api.Tracker;

@MoveProvider(name = "metal-burst")
public class MetalBurst implements PostMoveUse
{
    @Override
    public void applyPostMove(Damage t)
    {
        if (t.move().canceled || t.move().failed) return;
        IPokemob user = t.move().getUser();
        LivingEntity target = t.move().getTarget();
        final LivingEntity attacker = user.getEntity();
        if (!user.getMoveStats().biding)
        {
            attacker.getPersistentData().putLong("bideTime",
                    Tracker.instance().getTick() + PokecubeCore.getConfig().attackCooldown);
            user.getMoveStats().biding = true;
            user.getMoveStats().SPECIALDAMAGETAKENCOUNTER = 0;
            user.getMoveStats().PHYSICALDAMAGETAKENCOUNTER = 0;
        }
        else if (attacker.getPersistentData().getLong("bideTime") < Tracker.instance().getTick())
        {
            attacker.getPersistentData().remove("bideTime");
            final int damage = (int) (1.5
                    * (user.getMoveStats().SPECIALDAMAGETAKENCOUNTER + user.getMoveStats().PHYSICALDAMAGETAKENCOUNTER));
            user.getMoveStats().SPECIALDAMAGETAKENCOUNTER = 0;
            user.getMoveStats().PHYSICALDAMAGETAKENCOUNTER = 0;
            if (target != null && !target.isInvulnerable())
                target.hurt(new PokemobDamageSource(attacker, t.move().getMove()), damage);
            user.getMoveStats().biding = false;
        }
    }
}