package pokecube.mobs.abilities.p;

import net.minecraft.entity.LivingEntity;
import pokecube.core.database.abilities.Ability;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.moves.MovePacket;

public class PowerSpot extends Ability
{
	@Override
    public void onMoveUse(IPokemob mob, MovePacket move)
    {
    	final LivingEntity target = (LivingEntity) move.attacked;
    	final IPokemob targetMob = CapabilityPokemob.getPokemobFor(target);
    	final IPokemob attacker = move.attacker;
    	
    	if(move.pre || attacker == move.attacked) return;
    	if(targetMob != null) {
    		move.PWR *= 0.3;
    	}
    }
}
