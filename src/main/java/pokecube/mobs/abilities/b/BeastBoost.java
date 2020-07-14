package pokecube.mobs.abilities.b;

import net.minecraft.entity.LivingEntity;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.abilities.Ability;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.moves.MovePacket;
import pokecube.core.moves.MovesUtils;

public class BeastBoost extends Ability
{
	@Override
    public void onMoveUse(IPokemob mob, MovePacket move)
    {
		final LivingEntity target = (LivingEntity) move.attacked;
    	final IPokemob targetMob = CapabilityPokemob.getPokemobFor(target);
    	
        if (mob == move.attacked)
        {
            if (!targetMob.inCombat()) {
            	MovesUtils.handleStats2(mob, mob.getOwner(), IMoveConstants.ATTACK, IMoveConstants.RAISE);
            	MovesUtils.handleStats2(mob, mob.getOwner(), IMoveConstants.DEFENSE, IMoveConstants.RAISE);
            	MovesUtils.handleStats2(mob, mob.getOwner(), IMoveConstants.SPATACK, IMoveConstants.RAISE);
            	MovesUtils.handleStats2(mob, mob.getOwner(), IMoveConstants.SPDEFENSE, IMoveConstants.RAISE);
            	MovesUtils.handleStats2(mob, mob.getOwner(), IMoveConstants.VIT, IMoveConstants.RAISE);
            	System.out.println("subiu tudo!");
            }
        }
    }
	
	@Override
    public void onUpdate(final IPokemob mob)
    {
    	final PokedexEntry mobs = mob.getPokedexEntry();
    	if(!mob.inCombat()) 
        	mob.setPokedexEntry(mobs);
    }
}
