package pokecube.gimmicks.nests;

import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.NewRegistryEvent;
import pokecube.api.entity.CapabilityInhabitor.InhabitorProvider;
import pokecube.core.PokecubeCore;
import pokecube.core.entity.pokemobs.EntityPokemob;
import pokecube.core.eventhandlers.EventsHandler;
import pokecube.gimmicks.nests.tasks.ants.AntTasks;
import pokecube.gimmicks.nests.tasks.ants.AntTasks.AntInhabitor;
import pokecube.gimmicks.nests.tasks.bees.BeeTasks;
import pokecube.gimmicks.nests.tasks.bees.BeeTasks.BeeInhabitor;
import pokecube.gimmicks.nests.tasks.burrows.BurrowTasks;
import thut.api.item.ItemList;
import thut.core.common.ThutCore;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = PokecubeCore.MODID)
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

        ThutCore.FORGE_BUS.addGenericListener(Entity.class, NestTasks::onEntityCaps);
    }

    private static void onEntityCaps(final AttachCapabilitiesEvent<Entity> event)
    {
        if (event.getObject() instanceof EntityPokemob mob)
        {
            // If it is a bee, we will add this to it.
            if (mob.getType().is(EntityTypeTags.BEEHIVE_INHABITORS))
                event.addCapability(EventsHandler.BEECAP, new InhabitorProvider(new BeeInhabitor(mob)));
            if (ItemList.is(AntTasks.ANTS, mob))
                event.addCapability(EventsHandler.ANTCAP, new InhabitorProvider(new AntInhabitor(mob)));
        }
    }
}
