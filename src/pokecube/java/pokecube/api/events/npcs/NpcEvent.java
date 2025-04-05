package pokecube.api.events.npcs;

import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.LevelAccessor;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.living.LivingEvent;

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

    /**
     * Fired on the ThutCore.FORGE_BUS when an NPC is interacted with. If
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

        private TriState result = TriState.DEFAULT;

        public void setResult(TriState result)
        {
            this.result = result;
        }

        public TriState getResult()
        {
            return this.result;
        }

    }
}
