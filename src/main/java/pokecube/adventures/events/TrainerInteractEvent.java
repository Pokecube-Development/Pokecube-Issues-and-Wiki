package pokecube.adventures.events;

import javax.annotation.Nullable;

import net.minecraft.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingEvent;
import pokecube.adventures.capabilities.utils.ActionContext;

public class TrainerInteractEvent extends LivingEvent
{
    @Nullable
    public final ActionContext action;

    public TrainerInteractEvent(final LivingEntity entity, final ActionContext action)
    {
        super(entity);
        this.action = action;
    }

    /**
     * This event is fired when a trainer checks isUsableByPlayer for inventory
     * access. It is fired one the MinecraftForge.EVENT_BUS. The results are as
     * follows:
     * <br>
     * DEFAULT - use whatever trainer.isUsableByPlayer(target) returns
     * ALLOW - allow gui interaction regardless
     * DENY - deny gui interaction regardless
     * <br>
     * Action may be null if the trainer involved had not been interacted with
     * when this is called!
     * The entity argument handed to the LivingEvent is the player involved in
     * the interaction.
     */
    @HasResult
    public static class CanInteract extends TrainerInteractEvent
    {

        public CanInteract(final LivingEntity entity, final ActionContext action)
        {
            super(entity, action);
        }

    }
}
