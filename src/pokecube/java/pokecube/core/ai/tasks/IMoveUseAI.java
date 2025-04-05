package pokecube.core.ai.tasks;

import pokecube.api.entity.pokemob.IPokemob;
import pokecube.core.ai.brain.BrainUtils;
import thut.api.maths.Vector3;

public interface IMoveUseAI
{
    default void setUseMove(IPokemob pokemob, Vector3 targetLoc)
    {
        BrainUtils.setMoveUseTarget(pokemob.getEntity(), targetLoc);
    }

    default void setUsingMove(IPokemob pokemob)
    {
    }

    default void clearUseMove(IPokemob pokemob)
    {
        BrainUtils.clearMoveUseTarget(pokemob.getEntity());
    }
}
