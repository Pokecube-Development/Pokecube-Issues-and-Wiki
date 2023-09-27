package pokecube.core.ai.tasks;

import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.ai.CombatStates;
import pokecube.core.ai.brain.BrainUtils;
import thut.api.maths.Vector3;

public interface IMoveUseAI
{
    default void setUseMove(IPokemob pokemob, Vector3 targetLoc)
    {
        BrainUtils.setMoveUseTarget(pokemob.getEntity(), targetLoc);
        this.setUsingMove(pokemob);
    }

    default void setUsingMove(IPokemob pokemob)
    {
        pokemob.setCombatState(CombatStates.EXECUTINGMOVE, true);
    }

    default void clearUseMove(IPokemob pokemob)
    {
        pokemob.setCombatState(CombatStates.EXECUTINGMOVE, false);
        BrainUtils.clearMoveUseTarget(pokemob.getEntity());
    }
}
