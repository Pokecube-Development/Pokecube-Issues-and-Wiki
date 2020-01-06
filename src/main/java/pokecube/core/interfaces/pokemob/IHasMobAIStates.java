package pokecube.core.interfaces.pokemob;

import java.util.List;

import pokecube.core.ai.logic.Logic;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.interfaces.pokemob.ai.LogicStates;

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
    void initAI();

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
