package pokecube.mobs.abilities.s;

import java.util.Random;

import net.minecraft.world.World;
import pokecube.core.database.abilities.Ability;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.pokemob.moves.MovePacket;
import pokecube.core.moves.PokemobTerrainEffects;
import pokecube.core.network.packets.PacketSyncTerrain;
import thut.api.maths.Vector3;
import thut.api.terrain.TerrainManager;
import thut.api.terrain.TerrainSegment;

public class SandSpit extends Ability
{
	public int duration = 300 + new Random().nextInt(600);
    
	@Override
    public void onMoveUse(IPokemob mob, MovePacket move)
    {
		final IPokemob attacker = move.attacker;
		final World world = mob.getEntity().getEntityWorld();
		
		
		final TerrainSegment segment = TerrainManager.getInstance().getTerrian(world, Vector3.getNewVector());
	    final PokemobTerrainEffects teffect = (PokemobTerrainEffects) segment.geTerrainEffect("pokemobEffects");
		
        if (attacker == mob || move.pre || attacker == move.attacked) return;
        if (move.hit)
        {
        	//terrain.doWorldAction(mob, location);
        	
        	teffect.setEffect(1, duration + world.getGameTime(), mob);

            if (mob.getEntity().isServerWorld()) PacketSyncTerrain.sendTerrainEffects(mob.getEntity(),
                    segment.chunkX, segment.chunkY, segment.chunkZ, teffect);
        }       
    }
}
