package pokecube.api.entity.pokemob;

import java.util.List;

import pokecube.api.entity.pokemob.ai.CombatStates;
import pokecube.api.entity.pokemob.ai.GeneralStates;
import pokecube.api.entity.pokemob.ai.LogicStates;
import pokecube.api.moves.IMoveConstants;
import pokecube.core.ai.logic.Logic;

public interface IHasMobAIStates extends IMoveConstants
{
    /** the value of the AI state state. */
    boolean getCombatState(CombatStates state);

    /** the value of the AI state state. */
    boolean getGeneralState(GeneralStates state);

    /** the value of the AI state state. */
    boolean getLogicState(LogicStates state);

    ////////////////////////////////////////////////////////////
    List<Logic> getTickLogic();

    ///////////////////////////////////////////////////

    /** @return total combat state for saving */
    int getTotalCombatState();

    ///////////////////////////////////////////////////
    /** @return Total general state for saving */
    int getTotalGeneralState();

    /** @return Total logic state for saving */
    int getTotalLogicState();

    /** Initializes the ai */
    void postInitAI();

    /**
     * First stage of brain initialisation, This calls early in the LivingEntity
     * constructor, so some things may not be available yet
     */
    void preInitAI();

    default void initAI()
    {
        preInitAI();
        postInitAI();
    }

    /**
     * This should default to whatever the routine defaults to, see
     * {@link AIRoutine#getDefault()}
     *
     * @param routine
     * @return
     */
    boolean isRoutineEnabled(AIRoutine routine);

    /** Sets AI state state to flag. */
    void setCombatState(CombatStates state, boolean flag);

    /** Sets AI state state to flag. */
    void setGeneralState(GeneralStates state, boolean flag);

    /** Sets AI state state to flag. */
    void setLogicState(LogicStates state, boolean flag);

    /**
     * @param routine
     * @param enabled
     */
    void setRoutineState(AIRoutine routine, boolean enabled);

    /**
     * Used for loading combat state.
     *
     * @param state
     */
    void setTotalCombatState(int state);

    /**
     * Used for loading general state.
     *
     * @param state
     */
    void setTotalGeneralState(int state);

    /**
     * Used for loading logic state.
     *
     * @param state
     */
    void setTotalLogicState(int state);

}
