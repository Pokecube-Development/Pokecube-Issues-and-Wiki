package pokecube.api.events.npcs;

import net.neoforged.bus.api.ICancellableEvent;
import pokecube.core.entity.npc.NpcMob;

public class NpcBreedEvent extends NpcEvent
{

    public NpcBreedEvent(NpcMob entity)
    {
        super(entity);
    }

    /**
     * This event is fired on the ThutCore.FORGE_BUS whenever an NPC
     * checks canBreed(). Cancelling this event will force canBreed() to return
     * false.
     *
     */
    public static class Check extends NpcBreedEvent implements ICancellableEvent
    {
        public Check(NpcMob entity)
        {
            super(entity);
        }
    }
}
