package pokecube.core.ai.tasks.bees.tasks;

import java.util.Map;
import java.util.Optional;

import com.google.common.collect.Maps;

import net.minecraft.block.BlockState;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.core.ai.tasks.bees.AbstractBeeTask;
import pokecube.core.ai.tasks.bees.BeeTasks;
import pokecube.core.ai.tasks.bees.sensors.FlowerSensor;
import pokecube.core.interfaces.IPokemob;
import thut.api.entity.ai.VectorPosWrapper;
import thut.api.maths.Vector3;

public class GatherNectar extends AbstractBeeTask
{
    private static final Map<MemoryModuleType<?>, MemoryModuleStatus> mems = Maps.newHashMap();
    static
    {
        // No gathering nectar if we have it
        GatherNectar.mems.put(BeeTasks.HAS_NECTAR, MemoryModuleStatus.VALUE_ABSENT);
        // Only gather nectar if we have a flower
        GatherNectar.mems.put(BeeTasks.FLOWER_POS, MemoryModuleStatus.VALUE_PRESENT);
    }

    // Timer for gathering stuff, when this reaches 400, we end gathering, by
    // setting HAS_NECTAR, and clearing the FLOWER_POS
    int gather_timer = 0;

    Vector3 gatherSpot = Vector3.getNewVector();
    Vector3 flowerSpot = Vector3.getNewVector();

    public GatherNectar(final IPokemob pokemob)
    {
        super(pokemob, GatherNectar.mems);
    }

    @Override
    public void reset()
    {
        this.gather_timer = 0;
        this.gatherSpot.clear();
    }

    @Override
    public void run()
    {
        final Optional<GlobalPos> pos_opt = this.entity.getBrain().getMemory(BeeTasks.FLOWER_POS);
        if (pos_opt.isPresent())
        {
            final World world = this.entity.getCommandSenderWorld();
            final GlobalPos pos = pos_opt.get();
            boolean clearPos = pos.dimension() != world.dimension();
            // Once a second check if flower is still valid.
            if (!clearPos && this.entity.tickCount % 20 == 0) clearPos = !FlowerSensor.flowerPredicate.test(world
                    .getBlockState(pos.pos()));
            // If flower not still around, clear the memory and return early.
            // The FlowerSensor will find a new flower for us later.
            if (clearPos)
            {
                this.entity.getBrain().eraseMemory(BeeTasks.FLOWER_POS);
                this.reset();
                return;
            }
            final Brain<?> brain = this.entity.getBrain();

            // We have gathered enough, lets go home now
            if (this.gather_timer++ > 400)
            {
                brain.eraseMemory(BeeTasks.FLOWER_POS);
                brain.setMemory(BeeTasks.HAS_NECTAR, true);
                this.reset();
                return;
            }

            this.flowerSpot.set(pos.pos()).addTo(0.5, 0.5, 0.5);
            // Set the mob as looking at the flower
            brain.setMemory(MemoryModules.LOOK_TARGET, new VectorPosWrapper(this.flowerSpot));

            // Find a random spot near the flower to move to, to fly around it
            // while gathering.
            if (this.gatherSpot.isEmpty() || this.entity.getRandom().nextInt(25) == 0)
            {
                this.gatherSpot.set(this.flowerSpot);
                this.gatherSpot.addTo(this.getRandomOffset(), 0, this.getRandomOffset());
            }

            // If too far, we path normally over to the flower, otherwise, use
            // MoveHelper directly.
            if (this.entity.distanceToSqr(this.gatherSpot.x, this.gatherSpot.y, this.gatherSpot.z) < 4)
            {
                final BlockState state = this.flowerSpot.getBlockState(world);
                if (state.isRandomlyTicking() && this.entity.getRandom().nextInt(10) == 0) state.randomTick(
                        (ServerWorld) world, pos.pos(), this.entity.getRandom());
                this.entity.getMoveControl().setWantedPosition(this.gatherSpot.x, this.gatherSpot.y, this.gatherSpot.z, 0.35F);
            }
            else this.setWalkTo(this.gatherSpot, 1, 0);
        }
    }

    /**
     * @return a random number from 0 to 0.333 for an offset from the flower
     *         position.
     */
    private float getRandomOffset()
    {
        return (this.entity.getRandom().nextFloat() * 2.0F - 1.0F) * 0.33333334F;
    }

    @Override
    public boolean doTask()
    {
        final Brain<?> brain = this.entity.getBrain();
        final Optional<Boolean> hasNectar = brain.getMemory(BeeTasks.HAS_NECTAR);
        final boolean nectar = hasNectar.isPresent() && hasNectar.get();
        return !nectar;
    }

}
