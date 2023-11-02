package pokecube.api.events.npcs;

import net.minecraftforge.eventbus.api.Cancelable;
import pokecube.core.entity.npc.NpcMob;

public class NpcBreedEvent extends NpcEvent
{

    public NpcBreedEvent(NpcMob entity)
    {
        super(entity);
    }

    @Cancelable
    /**
     * This event is fired on the ThutCore.FORGE_BUS whenever an NPC
     * checks canBreed(). Cancelling this event will force canBreed() to return
     * false.
     *
     */
    public static class Check extends NpcBreedEvent
    {
        public Check(NpcMob entity)
        {
            super(entity);
        }
    }
}
