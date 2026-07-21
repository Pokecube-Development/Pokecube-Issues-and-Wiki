package pokecube.gimmicks.nests;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.Mob;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import pokecube.api.ai.IInhabitor;
import pokecube.api.entity.CapabilityInhabitor;
import pokecube.core.PokecubeCore;
import pokecube.gimmicks.nests.tasks.ants.AntTasks;
import pokecube.gimmicks.nests.tasks.ants.AntTasks.AntInhabitor;
import pokecube.gimmicks.nests.tasks.bees.BeeTasks;
import pokecube.gimmicks.nests.tasks.bees.BeeTasks.BeeInhabitor;
import pokecube.gimmicks.nests.tasks.burrows.BurrowTasks;
import thut.api.data.HolderProvider;
import thut.api.item.ItemList;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, modid = PokecubeCore.MODID)
public class NestTasks
{
    static
    {
        init();
    }

    @SubscribeEvent
    /**
     * Dummy event handler for ensuring this class has the static init called.
     * 
     * @param event
     */
    public static void dummyHandler(final NewRegistryEvent event)
    {

    }

    public static void init()
    {
        BeeTasks.init();
        AntTasks.init();
        BurrowTasks.init();
        ResourceLocation ANT = ResourceLocation.parse("pokecube:ant");
        CapabilityInhabitor._REGISTRY.register(new HolderProvider.Provider<IInhabitor>()
        {

            @Override
            public IInhabitor apply(IAttachmentHolder t)
            {
                if (!(t instanceof Mob mob)) return null;
                if (!(ItemList.is(AntTasks.ANTS, mob))) return null;
                return new AntInhabitor(mob);
            }

            @Override
            protected ResourceLocation key()
            {
                return ANT;
            }
        });
        ResourceLocation BEE = ResourceLocation.parse("pokecube:bee");
        CapabilityInhabitor._REGISTRY.register(new HolderProvider.Provider<IInhabitor>()
        {

            @Override
            public IInhabitor apply(IAttachmentHolder t)
            {
                if (!(t instanceof Mob mob)) return null;
                if (!mob.getType().is(EntityTypeTags.BEEHIVE_INHABITORS)) return null;
                return new BeeInhabitor(mob);
            }

            @Override
            protected ResourceLocation key()
            {
                return BEE;
            }
        });
    }
}
