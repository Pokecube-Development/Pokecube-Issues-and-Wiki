package pokecube.adventures.capabilities;

import com.google.common.collect.Lists;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import pokecube.api.entity.trainers.IHasRewards;
import thut.api.data.HolderProvider;

import java.util.List;

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
    
    private static final HolderProvider<IHasRewards> _REGISTRY = new HolderProvider<>(ResourceLocation.parse("pokecube_adventure:trainer_rewards"));

    public static void registerProvider(HolderProvider.Provider<IHasRewards> reg)
    {
        _REGISTRY.register(reg);
    }

    public static IHasRewards make(IAttachmentHolder holder)
    {
        return _REGISTRY.make(holder);
    }
}
