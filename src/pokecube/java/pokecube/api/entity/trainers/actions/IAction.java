package pokecube.api.entity.trainers.actions;

/**
 * An action to occur during an interaction or similar.
 *
 */
public interface IAction
{
    /**
     * Called when the interaction occurs.
     * 
     * @param action - context of the interaction
     * @return whether we applied.
     */
    boolean doAction(final ActionContext action);
}
