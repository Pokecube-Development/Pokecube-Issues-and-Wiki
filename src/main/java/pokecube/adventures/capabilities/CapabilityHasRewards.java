package pokecube.adventures.capabilities;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.core.Direction;
import net.minecraft.nbt.ListTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import pokecube.api.entity.trainers.IHasRewards;
import pokecube.api.entity.trainers.TrainerCaps;

public class CapabilityHasRewards
{
    public static class DefaultRewards implements IHasRewards, ICapabilitySerializable<ListTag>
    {
        private final LazyOptional<IHasRewards> holder  = LazyOptional.of(() -> this);
        private final List<Reward>              rewards = Lists.newArrayList();

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
    }
}
