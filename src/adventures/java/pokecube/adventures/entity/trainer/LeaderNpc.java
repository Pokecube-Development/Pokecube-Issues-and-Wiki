package pokecube.adventures.entity.trainer;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import pokecube.api.entity.trainers.IHasNPCAIStates.AIState;

public class LeaderNpc extends TrainerNpc
{
    public LeaderNpc(final EntityType<? extends TrainerBase> type, final Level worldIn)
    {
        super(type, worldIn);
        // Stuff below here is not null for real worlds, null for fake ones, so
        // lets return here if null.
        if (this.aiStates == null) return;
        this.aiStates.setAIState(AIState.TRADES_MOBS, false);
        this.pokemobsCap.resetTimeLose = 0;
    }

    @Override
    public void setTypedName(final String name)
    {
        this.setNPCName("pokecube.gym_leader.named:" + name);
    }
}
