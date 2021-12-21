package pokecube.core.events.npc;

import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.event.entity.living.LivingEvent;
import pokecube.core.entity.npc.NpcMob;

public abstract class NpcEvent extends LivingEvent
{
    private final NpcMob trainer;
    private final LevelAccessor world;

    public NpcEvent(NpcMob entity)
    {
        super(entity);
        this.trainer = entity;
        this.world = entity.level;
    }

    public NpcMob getNpcMob()
    {
        return this.trainer;
    }

    public LevelAccessor getWorld()
    {
        return this.world;
    }

    @HasResult
    /**
     * Fired on the MinecraftForge.EVENT_BUS when an NPC is interacted with. If
     * this gets a Result of Result.ALLOW, it will trigger opening of the Npc's
     * inventory instead of the regular interactions.
     *
     */
    public static class OpenInventory extends NpcEvent
    {

        public OpenInventory(NpcMob entity)
        {
            super(entity);
        }

    }
}
