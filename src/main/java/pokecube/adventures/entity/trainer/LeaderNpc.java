package pokecube.adventures.entity.trainer;

import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.world.World;
import pokecube.adventures.capabilities.CapabilityNPCAIStates.IHasNPCAIStates;

public class LeaderNpc extends TrainerNpc
{
    public static final EntityType<LeaderNpc> TYPE;

    static
    {
        TYPE = EntityType.Builder.create(LeaderNpc::new, EntityClassification.CREATURE).setCustomClientFactory((s,
                w) -> LeaderNpc.TYPE.create(w)).build("leader");
    }

    public LeaderNpc(final EntityType<? extends TrainerBase> type, final World worldIn)
    {
        super(type, worldIn);
        this.aiStates.setAIState(IHasNPCAIStates.STATIONARY, true);
        this.aiStates.setAIState(IHasNPCAIStates.TRADES, false);
        this.pokemobsCap.resetTime = 0;
    }
}
