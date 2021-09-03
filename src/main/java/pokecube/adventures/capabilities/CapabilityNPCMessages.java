package pokecube.adventures.capabilities;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.util.Util;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import pokecube.adventures.capabilities.utils.Action;
import pokecube.adventures.capabilities.utils.ActionContext;
import pokecube.adventures.capabilities.utils.BattleAction;
import pokecube.adventures.capabilities.utils.GuiOpenAction;
import pokecube.adventures.capabilities.utils.MessageState;
import pokecube.core.PokecubeCore;
import pokecube.core.interfaces.PokecubeMod;

public class CapabilityNPCMessages
{
    public static class DefaultMessager implements IHasMessages, ICapabilitySerializable<CompoundNBT>
    {
        private final LazyOptional<IHasMessages> holder   = LazyOptional.of(() -> this);
        Map<MessageState, String>                messages = Maps.newHashMap();
        Map<MessageState, Action>                actions  = Maps.newHashMap();

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
        public void deserializeNBT(final CompoundNBT nbt)
        {
            if (!nbt.contains("messages")) return;
            final CompoundNBT messTag = nbt.getCompound("messages");
            for (final MessageState state : MessageState.values())
                if (messTag.contains(state.name())) this.setMessage(state, messTag.getString(state.name()));
            final CompoundNBT actionTag = nbt.getCompound("actions");
            for (final MessageState state : MessageState.values())
                if (actionTag.contains(state.name())) this.setAction(state, new Action(actionTag.getString(state
                        .name())));
        }

        @Override
        public boolean doAction(final MessageState state, final ActionContext context)
        {
            final Action action = this.actions.get(state);
            if (action != null) return action.doAction(context);
            return false;
        }

        @Override
        public Action getAction(final MessageState state)
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
            if (target instanceof FakePlayer || this.messages.get(state) == null || this.messages.get(state).trim()
                    .isEmpty()) return false;
            target.sendMessage(new TranslationTextComponent(this.messages.get(state), args), Util.NIL_UUID);
            if (PokecubeMod.debug) PokecubeCore.LOGGER.debug(state + ": " + this.messages.get(state));
            return true;
        }

        @Override
        public CompoundNBT serializeNBT()
        {
            final CompoundNBT nbt = new CompoundNBT();
            final CompoundNBT messTag = new CompoundNBT();
            final CompoundNBT actionTag = new CompoundNBT();
            for (final MessageState state : MessageState.values())
            {
                final String message = this.getMessage(state);
                if (message != null && !message.isEmpty()) messTag.putString(state.name(), message);
                final Action action = this.getAction(state);
                if (action != null && !action.getCommand().isEmpty()) actionTag.putString(state.name(), action
                        .getCommand());
            }
            nbt.put("messages", messTag);
            nbt.put("actions", actionTag);
            return nbt;
        }

        @Override
        public void setAction(final MessageState state, final Action action)
        {
            this.actions.put(state, action);
        }

        @Override
        public void setMessage(final MessageState state, final String message)
        {
            this.messages.put(state, message);
        }

    }

    public static interface IHasMessages
    {
        boolean doAction(MessageState state, ActionContext context);

        Action getAction(MessageState state);

        String getMessage(MessageState state);

        boolean sendMessage(MessageState state, Entity target, Object... args);

        void setAction(MessageState state, Action action);

        void setMessage(MessageState state, String message);
    }

    public static class Storage implements Capability.IStorage<IHasMessages>
    {

        @SuppressWarnings({ "rawtypes", "unchecked" })
        @Override
        public void readNBT(final Capability<IHasMessages> capability, final IHasMessages instance,
                final Direction side, final INBT base)
        {
            if (instance instanceof INBTSerializable<?>) ((INBTSerializable) instance).deserializeNBT(base);
        }

        @Override
        public INBT writeNBT(final Capability<IHasMessages> capability, final IHasMessages instance,
                final Direction side)
        {
            if (instance instanceof INBTSerializable<?>) return ((INBTSerializable<?>) instance).serializeNBT();
            return null;
        }
    }

    public static Storage storage;
}
