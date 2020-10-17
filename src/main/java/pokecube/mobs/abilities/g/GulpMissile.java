package pokecube.mobs.abilities.g;

import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.abilities.Ability;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.pokemob.moves.MovePacket;
import pokecube.core.moves.MovesUtils;

public class GulpMissile extends Ability
{
	private static PokedexEntry baseNormal;
    private static PokedexEntry arrakuda;  
    private static PokedexEntry pikachu;

    
    private static boolean      noTurnBase = false;
    
   /* @Override
    public void onMoveUse(final IPokemob mob, final MovePacket move)
    {
        if (GulpMissile.noTurnBase) return;
        if (GulpMissile.baseNormal == null)
        {
        	GulpMissile.baseNormal = Database.getEntry("Cramorant");
        	GulpMissile.arrakuda = Database.getEntry("Cramorant Gulping");
        	GulpMissile.pikachu = Database.getEntry("Cramorant Gorging");
        	GulpMissile.noTurnBase = GulpMissile.baseNormal == null; 
            if (GulpMissile.noTurnBase) return;
        }
        
        //Turn Forme
        final PokedexEntry mobs = mob.getPokedexEntry();
        if (mobs == GulpMissile.baseNormal)
        {
        	if(move.attack.equals("surf") || move.attack.equals("dive")) 
        	{
		        if (mob.getEntity().getHealth() < (mob.getEntity().getMaxHealth() / 2))
		        {
		            if (mobs == GulpMissile.baseNormal) 
		            	mob.setPokedexEntry(GulpMissile.pikachu);
		        }
		        else if(mob.getEntity().getHealth() > (mob.getEntity().getMaxHealth() / 2)) 
		        {
		        	if (mobs == GulpMissile.baseNormal) 
		        		mob.setPokedexEntry(GulpMissile.arrakuda);
		        }
        	}
        }
        
        final IPokemob attacker = move.attacker;
        if (attacker == mob || move.pre || attacker == move.attacked) return;
        //Hit for Arrakuda
        if (mobs == GulpMissile.arrakuda)
        {
        	if (move.hit) {
        		attacker.getEntity().setHealth((attacker.getEntity().getMaxHealth()) / 4);
            	MovesUtils.handleStats2(mob, attacker.getEntity(),
            			IMoveConstants.DEFENSE, IMoveConstants.FALL);
            	mob.setPokedexEntry(GulpMissile.baseNormal);
            	System.out.println("Peixe!");
            }
        }
        //Hit for Pikachu
        else if (mobs == GulpMissile.pikachu)
        {
        	if (move.hit) {
        		attacker.getEntity().setHealth((attacker.getEntity().getMaxHealth()) / 4);
            	MovesUtils.handleStats2(mob, attacker.getEntity(),
            			IMoveConstants.STATUS_PAR, IMoveConstants.FALL);
            	mob.setPokedexEntry(GulpMissile.baseNormal);
            	System.out.println("Pikachu?!");
            }
        }
    }
    */

    @Override
    public IPokemob onRecall(final IPokemob mob)
    {
        final PokedexEntry mobs = mob.getPokedexEntry();
        final boolean isSurf = mobs == GulpMissile.arrakuda;
        final boolean isDive = mobs == GulpMissile.pikachu;
        if (isSurf || isDive) return mob.setPokedexEntry(GulpMissile.baseNormal);
        return super.onRecall(mob);
    }
}
