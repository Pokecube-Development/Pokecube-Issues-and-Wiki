package pokecube.api.entity.trainers;

import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.INBTSerializable;
import pokecube.api.entity.trainers.actions.ActionContext;
import pokecube.api.entity.trainers.actions.MessageState;
import thut.core.common.ThutCore;

/**
 * This is a general capability interface for a mob which gives rewards.
 *
 */
public interface IHasRewards extends INBTSerializable<ListTag>
{
    /**
     * Base reward class, presently is just an ItemStack with a particular
     * chance.
     *
     */
    public static class Reward
    {
        public final ItemStack stack;
        public final float chance;

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

    /**
     * @return All of the rewards we will check to give.
     */
    List<Reward> getRewards();

    /**
     * @param rewardee - The entity getting the rewards
     * @param rewarder - The entity giving the rewards.
     */
    default void giveReward(final LivingEntity rewardee, final LivingEntity rewarder)
    {
        for (final Reward reward : this.getRewards())
        {
            final ItemStack i = reward.stack;
            if (i.isEmpty()) continue;
            if (ThutCore.newRandom().nextFloat() > reward.chance) continue;
            boolean drop = true;
            if (rewardee instanceof ServerPlayer player)
            {
                var inventory = player.getInventory();
                drop = !inventory.add(i.copy());
            }
            if (drop)
            {
                final ItemEntity item = rewardee.spawnAtLocation(i.copy(), 0.5f);
                if (item == null) continue;
                item.setPickUpDelay(0);
            }
            final IHasMessages messageSender = TrainerCaps.getMessages(rewarder);
            if (messageSender != null)
            {
                messageSender.sendMessage(MessageState.GIVEITEM, rewardee, rewarder.getDisplayName(), i.getHoverName(),
                        rewardee.getDisplayName());
                messageSender.doAction(MessageState.GIVEITEM, new ActionContext(rewardee, rewarder));
            }
        }
    }

    @Override
    default ListTag serializeNBT()
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

    @Override
    default void deserializeNBT(final ListTag nbt)
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
}