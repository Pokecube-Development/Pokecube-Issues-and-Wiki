package pokecube.api.ai.combat;

import pokecube.api.PokecubeAPI;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.ai.CombatStates;
import pokecube.core.PokecubeCore;

public interface IMoveSelector
{
    void checkMoves(IPokemob pokemob);

    default void selectMove(IPokemob pokemob, int index, String debugInfo)
    {
        // Disabled, return.
        if (pokemob.getDisableTimer(index) > 0) return;
        // Already selected, so return
        if (index == pokemob.getMoveIndex()) return;
        if (PokecubeCore.getConfig().debug_ai)
            PokecubeAPI.LOGGER.info("Move Swap of type {}, {} g:{} h:{}", debugInfo, pokemob.getEntity(),
                    pokemob.getCombatState(CombatStates.GUARDING), pokemob.getCombatState(CombatStates.HUNTING));
        // Otherwise set it
        pokemob.setMoveIndex(index);
    }
}
