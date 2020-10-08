package pokecube.mobs.abilities.l;

import pokecube.core.database.PokedexEntry;
import pokecube.core.database.abilities.Ability;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.interfaces.pokemob.moves.MovePacket;

public class Libero extends Ability
{
    /*@Override
    public void onMoveUse(final IPokemob mob, final MovePacket move)
    {   
    	final Move_Base attack = move.getMove();
        if (!move.pre || move.attack.equals("struggle")) return;     
		if(mob == move.attacker)
        	mob.setType1(attack.move.type);
    }*/
    
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
