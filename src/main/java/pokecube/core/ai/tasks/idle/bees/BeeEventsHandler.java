package pokecube.core.ai.tasks.idle.bees;

import java.util.Optional;

import net.minecraft.block.BeehiveBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.BlockTags;
import net.minecraft.tileentity.BeehiveTileEntity;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.tasks.idle.bees.sensors.FlowerSensor;
import pokecube.core.events.HarvestCheckEvent;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;

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
        if (!BeeTasks.isValidBee(event.getEntity())) return;

        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(event.getEntity());
        final Brain<?> brain = pokemob.getEntity().getBrain();
        if (!brain.hasMemory(BeeTasks.HAS_NECTAR)) return;

        final StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        final Optional<Boolean> hasNectar = brain.getMemory(BeeTasks.HAS_NECTAR);
        final boolean nectar = hasNectar.isPresent() && hasNectar.get();
        boolean fromHive = false;
        final String hive_class = BeehiveTileEntity.class.getName();
        final int n = 0;
        for (final StackTraceElement element : stack)
        {
            fromHive = element.getClassName().equals(hive_class);
            if (fromHive || n > 20) break;
        }
        final Optional<GlobalPos> pos_opt = brain.getMemory(BeeTasks.HIVE_POS);

        // No hive pos
        if (!pos_opt.isPresent()) return;

        if (fromHive && nectar)
        {
            brain.removeMemory(BeeTasks.HAS_NECTAR);
            pokemob.eat(ItemStack.EMPTY);
            final World world = event.getEntity().getEntityWorld();
            final GlobalPos pos = pos_opt.get();
            final BlockState state = world.getBlockState(pos.getPos());
            if (state.getBlock().isIn(BlockTags.BEEHIVES))
            {
                final int i = BeehiveTileEntity.getHoneyLevel(state);
                if (i < 5)
                {
                    int j = world.rand.nextInt(100) == 0 ? 2 : 1;
                    if (i + j > 5) --j;
                    world.setBlockState(pos.getPos(), state.with(BeehiveBlock.HONEY_LEVEL, Integer.valueOf(i + j)));
                }
            }
        }
    }
}
