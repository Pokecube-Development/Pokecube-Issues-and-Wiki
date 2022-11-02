package pokecube.api.entity.pokemob;

import java.util.List;

import pokecube.api.entity.pokemob.ai.AIRoutine;
import pokecube.api.entity.pokemob.ai.CombatStates;
import pokecube.api.entity.pokemob.ai.GeneralStates;
import pokecube.api.entity.pokemob.ai.LogicStates;
import pokecube.api.moves.utils.IMoveConstants;
import pokecube.core.ai.logic.Logic;

public interface IHasMobAIStates extends IMoveConstants
{
    /** the value of the AI state state. */
    default boolean getCombatState(CombatStates state)
    {
        return (this.getTotalCombatState() & state.getMask()) != 0;
    }

    /** the value of the AI state state. */
    default boolean getGeneralState(final GeneralStates state)
    {
        return (this.getTotalGeneralState() & state.getMask()) != 0;
    }

    /** the value of the AI state state. */
    default boolean getLogicState(LogicStates state)
    {
        return (this.getTotalLogicState() & state.getMask()) != 0;
    }

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
    default void setCombatState(CombatStates state, boolean flag)
    {
        final int byte0 = this.getTotalCombatState();
        if (flag == ((byte0 & state.getMask()) != 0)) return;
        final int newState = flag ? byte0 | state.getMask() : byte0 & -state.getMask() - 1;
        this.setTotalCombatState(newState);
    }

    /** Sets AI state state to flag. */
    default void setGeneralState(GeneralStates state, boolean flag)
    {
        final int byte0 = this.getTotalGeneralState();
        if (flag == ((byte0 & state.getMask()) != 0)) return;
        final int newState = flag ? byte0 | state.getMask() : byte0 & -state.getMask() - 1;
        this.setTotalGeneralState(newState);
    }

    /** Sets AI state state to flag. */
    default void setLogicState(final LogicStates state, final boolean flag)
    {
        final int byte0 = this.getTotalLogicState();
        if (flag == ((byte0 & state.getMask()) != 0)) return;
        final int newState = flag ? byte0 | state.getMask() : byte0 & -state.getMask() - 1;
        this.setTotalLogicState(newState);
    }

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
