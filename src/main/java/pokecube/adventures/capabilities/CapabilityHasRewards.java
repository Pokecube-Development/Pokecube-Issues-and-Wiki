package pokecube.adventures.capabilities;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import pokecube.adventures.capabilities.CapabilityNPCMessages.IHasMessages;
import pokecube.adventures.capabilities.utils.ActionContext;
import pokecube.adventures.capabilities.utils.MessageState;
import thut.core.common.ThutCore;

public class CapabilityHasRewards
{
    public static class DefaultRewards implements IHasRewards, ICapabilitySerializable<ListTag>
    {
        private final LazyOptional<IHasRewards> holder  = LazyOptional.of(() -> this);
        private final List<Reward>              rewards = Lists.newArrayList();

        @Override
        public void deserializeNBT(final ListTag nbt)
        {
            this.getRewards().clear();
            for (int i = 0; i < nbt.size(); ++i)
            {
                final CompoundTag tag = nbt.getCompound(i);
                final ItemStack stack = ItemStack.of(tag);
                final float chance = tag.contains("chance") ? tag.getFloat("chance") : 1;
                this.getRewards().add(new Reward(stack, chance));
            }
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
        public ListTag serializeNBT()
        {
            final ListTag ListNBT = new ListTag();
            for (final Reward element : this.getRewards())
            {
                final ItemStack stack = element.stack;

                if (!stack.isEmpty())
                {
                    final CompoundTag CompoundNBT = new CompoundTag();
                    stack.save(CompoundNBT);
                    CompoundNBT.putFloat("chance", element.chance);
                    ListNBT.add(CompoundNBT);
                }
            }
            return ListNBT;
        }

    }

    public static interface IHasRewards
    {
        List<Reward> getRewards();

        default void giveReward(final Player player, final LivingEntity rewarder)
        {
            for (final Reward reward : this.getRewards())
            {
                final ItemStack i = reward.stack;
                if (i.isEmpty()) continue;
                if (ThutCore.newRandom().nextFloat() > reward.chance) continue;
                if (!player.getInventory().add(i.copy()))
                {
                    final ItemEntity item = player.spawnAtLocation(i.copy(), 0.5f);
                    if (item == null) continue;
                    item.setPickUpDelay(0);
                }
                final IHasMessages messageSender = TrainerCaps.getMessages(rewarder);
                if (messageSender != null)
                {
                    messageSender.sendMessage(MessageState.GIVEITEM, player, rewarder.getDisplayName(), i
                            .getHoverName(), player.getDisplayName());
                    messageSender.doAction(MessageState.GIVEITEM, new ActionContext(player, rewarder));
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

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        public void readNBT(final Capability<IHasRewards> capability, final IHasRewards instance, final Direction side,
                final Tag base)
        {
            if (instance instanceof INBTSerializable<?>) ((INBTSerializable) instance).deserializeNBT(base);
        }

        @Override
        public Tag writeNBT(final Capability<IHasRewards> capability, final IHasRewards instance, final Direction side)
        {
            if (instance instanceof INBTSerializable<?>) return ((INBTSerializable<?>) instance).serializeNBT();
            return null;
        }

    }

    public static Storage storage;
}
