package pokecube.core.ai.tasks.idle.bees;

import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.tasks.idle.bees.sensors.FlowerSensor;
import pokecube.core.events.HarvestCheckEvent;
import pokecube.core.interfaces.IInhabitable;
import pokecube.core.interfaces.capabilities.CapabilityInhabitable;

public class BeeEventsHandler
{

    public static void init()
    {
        MinecraftForge.EVENT_BUS.addListener(BeeEventsHandler::onBeeAddedToWorld);
        PokecubeCore.POKEMOB_BUS.addListener(BeeEventsHandler::onBeeGatherBlocks);
    }

    /**
     * Here we check if the harvesting block to test is a flower, we
     * specifically prevent those here, as we polinate them instead!
     */
    private static void onBeeGatherBlocks(final HarvestCheckEvent event)
    {
        if (!BeeTasks.isValidBee(event.getEntity())) return;
        if (FlowerSensor.flowerPredicate.test(event.state)) event.setResult(Result.DENY);
    }

    /**
     * Here we will check if it was a bee, added from a bee-hive, and if so, we
     * will increment the honey level as needed.
     */
    private static void onBeeAddedToWorld(final EntityJoinWorldEvent event)
    {
        // We only consider MobEntities
        if (!(event.getEntity() instanceof MobEntity)) return;
        final MobEntity mob = (MobEntity) event.getEntity();
        final Brain<?> brain = mob.getBrain();
        // No hive pos, not a bee leaving hive
        if (!brain.hasMemory(BeeTasks.HIVE_POS)) return;
        final World world = event.getEntity().getEntityWorld();
        final GlobalPos pos = brain.getMemory(BeeTasks.HIVE_POS).get();
        // not same dimension, not a bee leaving hive
        if (pos.getDimension() != world.getDimensionKey()) return;

        // This will indicate if the tile did actually cause the spawn.
        boolean fromHive = false;
        int n = 0;
        Class<?> c = null;
        // Check the stack to see if tile resulted in our spawn, if not, then we
        // are not from it either!
        for (final StackTraceElement element : Thread.currentThread().getStackTrace())
        {
            try
            {
                c = Class.forName(element.getClassName());
                fromHive = TileEntity.class.isAssignableFrom(c);
            }
            catch (final ClassNotFoundException e)
            {
                // NOOP, why would this happen anyway?
                e.printStackTrace();
            }
            if (fromHive || n++ > 100) break;
        }
        // was not from the hive, so exit
        if (!fromHive) return;
        // not loaded, definitely not a bee leaving hive
        if (!world.isAreaLoaded(pos.getPos(), 0)) return;
        final TileEntity tile = world.getTileEntity(pos.getPos());
        // No tile entity here? also not a bee leaving hive!
        if (tile == null) return;
        // Not the same class, so return as well.
        if (tile.getClass() != c) return;
        final IInhabitable habitat = tile.getCapability(CapabilityInhabitable.CAPABILITY).orElse(null);
        // Not a habitat, so not going to be a bee leaving a hive
        if (habitat == null) return;
        habitat.onLeaveHabitat(mob);
    }
}
