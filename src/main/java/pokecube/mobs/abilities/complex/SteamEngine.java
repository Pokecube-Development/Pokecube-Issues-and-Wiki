package pokecube.mobs.abilities.complex;

import java.util.List;

import pokecube.api.data.abilities.Ability;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.moves.MovePacket;
import pokecube.api.moves.IMoveConstants;
import pokecube.api.utils.PokeType;
import pokecube.core.items.pokemobeggs.EntityPokemobEgg;
import pokecube.core.moves.MovesUtils;
import thut.api.maths.Vector3;

public class SteamEngine extends Ability
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
        if (!move.pre) return;
        if (move.attackType == PokeType.getType("water") || 
        		move.attackType == PokeType.getType("fire") && mob == move.attacker)
        {
        	MovesUtils.handleStats2(mob, mob
                    .getEntity(), IMoveConstants.VIT, IMoveConstants.RAISE);
        }
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

    @Override
    public boolean singleton()
    {
        return false;
    }
}
