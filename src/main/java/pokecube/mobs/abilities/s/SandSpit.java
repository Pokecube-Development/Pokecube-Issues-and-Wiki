package pokecube.mobs.abilities.s;

import net.minecraft.world.World;
import pokecube.core.database.abilities.Ability;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.pokemob.moves.MovePacket;
import pokecube.core.moves.PokemobTerrainEffects;
import pokecube.core.network.packets.PacketSyncTerrain;
import thut.api.maths.Vector3;
import thut.api.terrain.TerrainManager;
import thut.api.terrain.TerrainSegment;
import thut.core.common.ThutCore;

public class SandSpit extends Ability
{
	public int duration = 300 + ThutCore.newRandom().nextInt(600);
    
	@Override
    public void onMoveUse(IPokemob mob, MovePacket move)
    {
		final IPokemob attacker = move.attacker;
		final World world = mob.getEntity().getCommandSenderWorld();
		
		
		final TerrainSegment segment = TerrainManager.getInstance().getTerrian(world, Vector3.getNewVector());
	    final PokemobTerrainEffects teffect = (PokemobTerrainEffects) segment.geTerrainEffect("pokemobEffects");
		
        if (attacker == mob || move.pre || attacker == move.attacked) return;
        if (move.hit)
        {
        	//terrain.doWorldAction(mob, location);
        	
        	teffect.setEffectDuration(PokemobTerrainEffects.WeatherEffectType.SAND, duration + world.getGameTime(), mob);

            if (mob.getEntity().isEffectiveAi()) PacketSyncTerrain.sendTerrainEffects(mob.getEntity(),
                    segment.chunkX, segment.chunkY, segment.chunkZ, teffect);
        }       
    }
}
