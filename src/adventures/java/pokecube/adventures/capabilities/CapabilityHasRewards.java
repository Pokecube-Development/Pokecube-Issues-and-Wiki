package pokecube.adventures.capabilities;

import java.util.List;

import com.google.common.collect.Lists;

import net.neoforged.neoforge.attachment.IAttachmentHolder;
import pokecube.api.entity.trainers.IHasRewards;
import thut.api.data.HolderProvider;

public class CapabilityHasRewards
{
    public static class DefaultRewards implements IHasRewards
    {
        private final List<Reward> rewards = Lists.newArrayList();

        @Override
        public List<Reward> getRewards()
        {
            return this.rewards;
        }
    }
    
    private static final HolderProvider<IHasRewards> _REGISTRY = new HolderProvider<>();

    public static void registerProvider(HolderProvider.Provider<IHasRewards> reg)
    {
        _REGISTRY.register(reg);
    }

    public static IHasRewards make(IAttachmentHolder holder)
    {
        return _REGISTRY.make(holder);
    }
}
