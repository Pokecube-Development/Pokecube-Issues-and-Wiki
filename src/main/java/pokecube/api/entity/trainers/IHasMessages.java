package pokecube.api.entity.trainers;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.util.INBTSerializable;
import pokecube.api.entity.trainers.actions.Action;
import pokecube.api.entity.trainers.actions.ActionContext;
import pokecube.api.entity.trainers.actions.IAction;
import pokecube.api.entity.trainers.actions.MessageState;

/**
 * A general capability for something which provides messages and
 * {@link Action}s for a given {@link ActionContext}
 *
 */
public interface IHasMessages extends INBTSerializable<CompoundTag>
{
    /**
     * @param state   - {@link MessageState} to apply for
     * @param context - {@link ActionContext} involved
     * @return whether anything occurred.
     */
    default boolean doAction(MessageState state, ActionContext context)
    {
        final IAction action = this.getAction(state);
        if (action != null) return action.doAction(context);
        return false;
    }

    /**
     * @param state - {@link MessageState} to get for
     * @return the - {@link IAction} to apply for that state.
     */
    IAction getAction(MessageState state);

    /**
     * @param state - {@link MessageState} to get for
     * @return the - unlocalised string message to send.
     */
    String getMessage(MessageState state);

    /**
     * 
     * @param state  - {@link MessageState} to get for
     * @param target - the entity to send the message to
     * @param args   - localisation arguments for the message
     * @return if a message was sent
     */
    boolean sendMessage(MessageState state, Entity target, Object... args);
    
    /**
     * @param state - {@link MessageState} to set for
     * @param action - {@link IAction} to apply.
     */
    void setAction(MessageState state, IAction action);
    
    /**
     * @param state - {@link MessageState} to set for
     * @param message - unlocalised string message to send.
     */
    void setMessage(MessageState state, String message);
}