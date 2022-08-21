package pokecube.api.entity.trainers;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.util.INBTSerializable;
import pokecube.api.entity.trainers.actions.Action;
import pokecube.api.entity.trainers.actions.ActionContext;
import pokecube.api.entity.trainers.actions.MessageState;

public interface IHasMessages extends INBTSerializable<CompoundTag>
{
    boolean doAction(MessageState state, ActionContext context);

    Action getAction(MessageState state);

    String getMessage(MessageState state);

    boolean sendMessage(MessageState state, Entity target, Object... args);

    void setAction(MessageState state, Action action);

    void setMessage(MessageState state, String message);
}