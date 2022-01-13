package pokecube.core.events.npc;

import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.event.entity.living.LivingEvent;

public abstract class NpcEvent extends LivingEvent
{
    private final Villager trainer;
    private final LevelAccessor world;

    public NpcEvent(Villager entity)
    {
        super(entity);
        this.trainer = entity;
        this.world = entity.level;
    }

    public Villager getNpcMob()
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

        public OpenInventory(Villager entity)
        {
            super(entity);
        }

    }
}
