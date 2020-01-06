package pokecube.core.database.abilities.f;

import java.util.List;

import pokecube.core.database.abilities.Ability;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.interfaces.pokemob.moves.MovePacket;
import pokecube.core.items.pokemobeggs.EntityPokemobEgg;
import thut.api.maths.Vector3;

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
    public void onMoveUse(IPokemob mob, MovePacket move)
    {
        final Move_Base attack = move.getMove();

        final IPokemob attacker = move.attacker;
        if (attacker == mob || move.pre || attacker == move.attacked) return;
        if (move.hit && attack.getAttackCategory() == IMoveConstants.CATEGORY_CONTACT && Math.random() > 0.7)
            move.attacker.setStatus(IMoveConstants.STATUS_BRN);
    }

    @Override
    public void onUpdate(IPokemob mob)
    {
        final Vector3 v = Vector3.getNewVector().set(mob.getEntity());
        final List<EntityPokemobEgg> eggs = mob.getEntity().getEntityWorld().getEntitiesWithinAABB(
                EntityPokemobEgg.class, v.getAABB().expand(this.range, this.range, this.range));
        for (final EntityPokemobEgg egg : eggs)
            egg.incubateEgg();
    }

}
