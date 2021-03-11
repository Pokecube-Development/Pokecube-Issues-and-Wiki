package pokecube.mobs.abilities.s;

import java.util.List;

import pokecube.core.database.abilities.Ability;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.pokemob.moves.MovePacket;
import pokecube.core.items.pokemobeggs.EntityPokemobEgg;
import pokecube.core.moves.MovesUtils;
import pokecube.core.utils.PokeType;
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
        final Vector3 v = Vector3.getNewVector().set(mob.getEntity());
        final List<EntityPokemobEgg> eggs = mob.getEntity().getCommandSenderWorld().getEntitiesOfClass(
                EntityPokemobEgg.class, v.getAABB().expandTowards(this.range, this.range, this.range));
        for (final EntityPokemobEgg egg : eggs)
            egg.incubateEgg();
    }
}
