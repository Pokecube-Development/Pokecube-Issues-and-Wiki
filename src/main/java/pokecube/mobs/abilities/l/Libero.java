package pokecube.mobs.abilities.l;

import pokecube.api.data.PokedexEntry;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.moves.MovePacket;
import pokecube.api.moves.Move_Base;
import pokecube.core.database.abilities.Ability;

public class Libero extends Ability
{
    @Override
    public void onMoveUse(final IPokemob mob, final MovePacket move)
    {   
    	final Move_Base attack = move.getMove();
        if (!move.pre || move.attack.equals("struggle")) return;     
		if(mob == move.attacker)
        	mob.setType1(attack.move.type);
    }
    
    @Override
    public void onUpdate(final IPokemob mob)
    {
    	final PokedexEntry mobs = mob.getPokedexEntry();
    	if(!mob.inCombat()) 
        	mob.setType1(mobs.getType1());
    }
    
    @Override
    public IPokemob onRecall(final IPokemob mob)
    {
        final PokedexEntry mobs = mob.getPokedexEntry();
        mob.setType1(mobs.getType1());
        return super.onRecall(mob);
    }
}
