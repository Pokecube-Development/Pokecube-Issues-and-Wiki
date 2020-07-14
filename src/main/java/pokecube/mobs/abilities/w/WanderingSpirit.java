package pokecube.mobs.abilities.w;

import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.database.abilities.Ability;
import pokecube.core.database.abilities.AbilityManager;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.interfaces.pokemob.moves.MovePacket;

public class WanderingSpirit extends Ability
{		
	private static final String[] Looked = { "Disguise", "FlowerGift", "GulpMissile", "IceFace", "Imposter",
			"Receiver", "RKSSystem", "Schooling", "StanceChange", "WonderGuard", "ZenMode" };

	Ability Spirit;
	
    @Override
    public void onMoveUse(IPokemob mob, MovePacket move)
    {
    	final Move_Base attack = move.getMove();
    	final IPokemob attacker = move.attacker;
    	
    	if(move.pre || attacker == move.attacked) return;
        if (move.hit && attack.getAttackCategory() == IMoveConstants.CATEGORY_CONTACT && Math.random() > 0.7) {
            final String name = attacker.getAbility().getName().toString();
            
            for(final String m : WanderingSpirit.Looked) {
            	if(m.equalsIgnoreCase(name))
            	{
            		System.out.println("NOP!");
            		break;
            	}
            	else
            	{
	      			final Ability ability = attacker.getAbility();
	      			if (ability != null) {
	                	this.Spirit = AbilityManager.makeAbility(ability.getClass(), mob);
	                }
            	}
        	}
        }
        
        if (this.Spirit != null) {
        	this.Spirit.onMoveUse(mob, move);
        }
    }

    @Override
    public void onUpdate(final IPokemob mob)
    {
        if (this.Spirit != null && !BrainUtils.hasAttackTarget(mob.getEntity()))
        {
            this.Spirit.destroy();
            this.Spirit = null;
        }
        else if (this.Spirit != null) this.Spirit.onUpdate(mob);
    }
}
