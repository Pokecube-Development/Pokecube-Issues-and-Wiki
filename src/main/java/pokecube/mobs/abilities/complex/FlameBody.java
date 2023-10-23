package pokecube.mobs.abilities.complex;

import java.util.List;

import pokecube.api.data.abilities.Ability;
import pokecube.api.data.abilities.AbilityProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.MoveEntry;
import pokecube.api.moves.utils.IMoveConstants;
import pokecube.api.moves.utils.MoveApplication;
import pokecube.core.items.pokemobeggs.EntityPokemobEgg;
import thut.api.maths.Vector3;

@AbilityProvider(name = "flame-body", singleton = false)
public class FlameBody extends Ability
{
    int range = 4;

    @Override
    public Ability init(Object... args)
    {
        for (int i = 0; i < 2; i++)
            if (args != null && args.length > i) if (args[i] instanceof Integer)
            {
                this.range = (int) args[i];
                return this;
            }
        return this;
    }

    @Override
    public void postMoveUse(final IPokemob mob, final MoveApplication move)
    {
        if (!areWeTarget(mob, move)) return;
        final MoveEntry attack = move.getMove();
        final IPokemob attacker = move.getUser();
        if (move.hit && attack.isContact(attacker) && Math.random() > 0.7)
            attacker.setStatus(mob, IMoveConstants.STATUS_BRN);
    }

    @Override
    public void onUpdate(IPokemob mob)
    {
        final Vector3 v = new Vector3().set(mob.getEntity());
        final List<EntityPokemobEgg> eggs = mob.getEntity().getLevel().getEntitiesOfClass(
                EntityPokemobEgg.class, v.getAABB().expandTowards(this.range, this.range, this.range));
        for (final EntityPokemobEgg egg : eggs)
            egg.incubateEgg();
    }
}
