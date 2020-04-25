package pokecube.adventures.capabilities;

import java.util.List;
import java.util.Random;

import com.google.common.collect.Lists;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import pokecube.adventures.capabilities.CapabilityNPCMessages.IHasMessages;
import pokecube.adventures.capabilities.utils.MessageState;

public class CapabilityHasRewards
{
    public static class DefaultRewards implements IHasRewards, ICapabilitySerializable<ListNBT>
    {
        private final LazyOptional<IHasRewards> holder  = LazyOptional.of(() -> this);
        private final List<Reward>              rewards = Lists.newArrayList();

        @Override
        public void deserializeNBT(final ListNBT nbt)
        {
            CapabilityHasRewards.storage.readNBT(TrainerCaps.REWARDS_CAP, this, null, nbt);
        }

        @Override
        public <T> LazyOptional<T> getCapability(final Capability<T> cap, final Direction side)
        {
            return TrainerCaps.REWARDS_CAP.orEmpty(cap, this.holder);
        }

        @Override
        public List<Reward> getRewards()
        {
            return this.rewards;
        }

        @Override
        public ListNBT serializeNBT()
        {
            return (ListNBT) CapabilityHasRewards.storage.writeNBT(TrainerCaps.REWARDS_CAP, this, null);
        }

    }

    public static interface IHasRewards
    {
        List<Reward> getRewards();

        default void giveReward(final PlayerEntity player, final LivingEntity rewarder)
        {
            for (final Reward reward : this.getRewards())
            {
                final ItemStack i = reward.stack;
                if (i.isEmpty()) continue;
                if (new Random().nextFloat() > reward.chance) continue;
                if (!player.inventory.addItemStackToInventory(i.copy()))
                {
                    final ItemEntity item = player.entityDropItem(i.copy(), 0.5f);
                    if (item == null) continue;
                    item.setPickupDelay(0);
                }
                final IHasMessages messageSender = TrainerCaps.getMessages(rewarder);
                if (messageSender != null)
                {
                    messageSender.sendMessage(MessageState.GIVEITEM, player, rewarder.getDisplayName(), i
                            .getDisplayName(), player.getDisplayName());
                    messageSender.doAction(MessageState.GIVEITEM, player);
                }
            }
        }
    }

    public static class Reward
    {
        public final ItemStack stack;
        public final float     chance;

        public Reward(final ItemStack stack)
        {
            this(stack, 1);
        }

        public Reward(final ItemStack stack, final float chance)
        {
            this.stack = stack;
            this.chance = chance;
        }
    }

    public static class Storage implements Capability.IStorage<IHasRewards>
    {

        @Override
        public void readNBT(final Capability<IHasRewards> capability, final IHasRewards instance, final Direction side,
                final INBT base)
        {
            if (!(base instanceof ListNBT)) return;
            final ListNBT ListNBT = (ListNBT) base;
            instance.getRewards().clear();
            for (int i = 0; i < ListNBT.size(); ++i)
            {
                final CompoundNBT tag = ListNBT.getCompound(i);
                final ItemStack stack = ItemStack.read(tag);
                final float chance = tag.contains("chance") ? tag.getFloat("chance") : 1;
                instance.getRewards().add(new Reward(stack, chance));
            }
        }

        @Override
        public INBT writeNBT(final Capability<IHasRewards> capability, final IHasRewards instance, final Direction side)
        {
            final ListNBT ListNBT = new ListNBT();
            for (final Reward element : instance.getRewards())
            {
                final ItemStack stack = element.stack;

                if (!stack.isEmpty())
                {
                    final CompoundNBT CompoundNBT = new CompoundNBT();
                    stack.write(CompoundNBT);
                    CompoundNBT.putFloat("chance", element.chance);
                    ListNBT.add(CompoundNBT);
                }
            }
            return ListNBT;
        }

    }

    public static Storage storage;
}
