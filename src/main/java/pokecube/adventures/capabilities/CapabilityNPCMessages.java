package pokecube.adventures.capabilities;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.LazyOptional;
import pokecube.adventures.capabilities.utils.BattleAction;
import pokecube.adventures.capabilities.utils.GuiOpenAction;
import pokecube.api.PokecubeAPI;
import pokecube.api.entity.trainers.IHasMessages;
import pokecube.api.entity.trainers.TrainerCaps;
import pokecube.api.entity.trainers.actions.Action;
import pokecube.api.entity.trainers.actions.IAction;
import pokecube.api.entity.trainers.actions.MessageState;
import pokecube.core.PokecubeCore;
import thut.lib.TComponent;

public class CapabilityNPCMessages
{
    public static class DefaultMessager implements IHasMessages, ICapabilitySerializable<CompoundTag>
    {
        private final LazyOptional<IHasMessages> holder = LazyOptional.of(() -> this);
        Map<MessageState, String> messages = Maps.newHashMap();
        Map<MessageState, IAction> actions = Maps.newHashMap();

        public DefaultMessager()
        {
            this.messages.put(MessageState.AGRESS, "pokecube.trainer.agress");
            this.messages.put(MessageState.ABOUTSEND, "pokecube.trainer.next");
            this.messages.put(MessageState.SENDOUT, "pokecube.trainer.toss");
            this.messages.put(MessageState.DEFEAT, "pokecube.trainer.defeat");
            this.messages.put(MessageState.DEAGRESS, "pokecube.trainer.forget");
            this.messages.put(MessageState.GIVEITEM, "pokecube.trainer.drop");
            this.messages.put(MessageState.INTERACT_YESBATTLE, "pokecube.trainer.agress_request_allowed");
            this.messages.put(MessageState.INTERACT_NOBATTLE, "pokecube.trainer.agress_request_denied");

            this.actions.put(MessageState.INTERACT_YESBATTLE, new BattleAction());
            this.actions.put(MessageState.INTERACT, new GuiOpenAction());
        }

        @Override
        public void deserializeNBT(final CompoundTag nbt)
        {
            if (!nbt.contains("messages")) return;
            final CompoundTag messTag = nbt.getCompound("messages");
            for (final MessageState state : MessageState.values())
                if (messTag.contains(state.name())) this.setMessage(state, messTag.getString(state.name()));
            final CompoundTag actionTag = nbt.getCompound("actions");
            for (final MessageState state : MessageState.values()) if (actionTag.contains(state.name()))
                this.setAction(state, new Action(actionTag.getString(state.name())));
        }

        @Override
        public IAction getAction(final MessageState state)
        {
            return this.actions.get(state);
        }

        @Override
        public <T> LazyOptional<T> getCapability(final Capability<T> cap, final Direction side)
        {
            return TrainerCaps.MESSAGES_CAP.orEmpty(cap, this.holder);
        }

        @Override
        public String getMessage(final MessageState state)
        {
            return this.messages.get(state);
        }

        @Override
        public boolean sendMessage(final MessageState state, final Entity target, final Object... args)
        {
            if (target instanceof FakePlayer || this.messages.get(state) == null
                    || this.messages.get(state).trim().isEmpty())
                return false;
            if (target instanceof ServerPlayer player)
                thut.lib.ChatHelper.sendSystemMessage(player, TComponent.translatable(this.messages.get(state), args));
            if (PokecubeCore.getConfig().debug_misc) PokecubeAPI.logInfo(state + ": " + this.messages.get(state));
            return true;
        }

        @Override
        public CompoundTag serializeNBT()
        {
            final CompoundTag nbt = new CompoundTag();
            final CompoundTag messTag = new CompoundTag();
            final CompoundTag actionTag = new CompoundTag();
            for (final MessageState state : MessageState.values())
            {
                final String message = this.getMessage(state);
                if (message != null && !message.isEmpty()) messTag.putString(state.name(), message);
                final IAction action = this.getAction(state);
                if (action instanceof Action a1 && !a1.getCommand().isEmpty())
                    actionTag.putString(state.name(), a1.getCommand());
            }
            nbt.put("messages", messTag);
            nbt.put("actions", actionTag);
            return nbt;
        }

        @Override
        public void setAction(final MessageState state, final IAction action)
        {
            this.actions.put(state, action);
        }

        @Override
        public void setMessage(final MessageState state, final String message)
        {
            this.messages.put(state, message);
        }

    }
}
