package pokecube.legends.init.function;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.eventbus.api.Event.Result;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.events.pokemob.CaptureEvent;
import pokecube.core.interfaces.IPokecube;
import pokecube.core.interfaces.IPokecube.PokecubeBehavior;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.items.pokecubes.PokecubeManager;

public class RaidCapture 
{
	public static void CatchPokemobRaid(final CaptureEvent.Pre event)
    {
		final ResourceLocation id = PokecubeItems.getCubeId(event.getFilledCube());

        final PokecubeBehavior cube = IPokecube.BEHAVIORS.getValue(id);
        
        //Catch Raids
        if(event.mob.getPersistentData().getBoolean("pokecube_legends:raid_mob") == true) {
        	if(id.toString().equals("pokecube_legends:dyna")) 
        	{
        	   PokecubeCore.LOGGER.debug("Life: " + event.mob.getHealth() + "Max Life: " + event.mob.getMaxHealth());
        	   if(event.mob.getHealth() < (event.mob.getMaxHealth() / 2)) {
        		   cube.onPreCapture(event);
        	   }else {
            	event.setCanceled(true);
            	event.setResult(Result.DENY);
            	}
            }else {
        	event.setCanceled(true);
        	event.setResult(Result.DENY);
        	}
    	}
        
        //No Catch normal Pokemobs
        if(event.mob.getPersistentData().getBoolean("pokecube_legends:raid_mob") == false ) {
        	event.setCanceled(true);
        	event.setResult(Result.DENY);
        }
    }
	
	public static void PostCatchPokemobRaid (final CaptureEvent.Post event) 
	{
		final ResourceLocation id = PokecubeItems.getCubeId(event.getFilledCube());
            
		//Catch Raids
    	if(id.toString().equals("pokecube_legends:dyna")) 
    	{
    		IPokemob pokemob = event.getCaught();
    		pokemob.setPokecube(PokecubeItems.getStack("pokecube"));
    		
    		//Pokemob Level Spawm
            int level = pokemob.getLevel();

            if(level <= 10 || level >= 40) {
            	level = 20;
            	pokemob.setForSpawn(level, false);
            }
            
    		event.setFilledCube(PokecubeManager.pokemobToItem(pokemob), true);
    	}
	}
}
