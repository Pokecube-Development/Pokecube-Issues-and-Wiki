package pokecube.core.ai.tasks.ants.tasks.nest;

import java.util.Map;
import java.util.Optional;

import com.google.common.collect.Maps;

import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.village.PointOfInterestManager;
import pokecube.core.PokecubeItems;
import pokecube.core.ai.poi.PointsOfInterest;
import pokecube.core.ai.tasks.ants.AntTasks;
import pokecube.core.ai.tasks.ants.sensors.NestSensor;
import pokecube.core.ai.tasks.ants.sensors.NestSensor.AntNest;
import pokecube.core.ai.tasks.bees.BeeTasks;
import pokecube.core.ai.tasks.idle.BaseIdleTask;
import pokecube.core.blocks.nests.NestTile;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityInhabitable.HabitatProvider;

public class CheckNest extends BaseIdleTask
{
    private static final Map<MemoryModuleType<?>, MemoryModuleStatus> mems = Maps.newHashMap();
    static
    {
        // Only run if we have a nest
        CheckNest.mems.put(AntTasks.NEST_POS, MemoryModuleStatus.VALUE_PRESENT);
    }
    protected int new_hive_cooldown = 0;

    protected AntNest nest;

    public CheckNest(final IPokemob pokemob)
    {
        super(pokemob, CheckNest.mems);
    }

    @Override
    public void reset()
    {
        this.new_hive_cooldown = 0;
    }

    @Override
    public void run()
    {
        final Brain<?> brain = this.entity.getBrain();
        final Optional<Integer> hiveTimer = brain.getMemory(AntTasks.OUT_OF_HIVE_TIMER);
        final int time = hiveTimer.orElseGet(() -> 0) - 1;
        brain.setMemory(BeeTasks.OUT_OF_HIVE_TIMER, time);
        if (this.nest == null) this.nest = NestSensor.getNest(this.entity).orElse(null);
        if (this.new_hive_cooldown++ > 60)
        {
            this.new_hive_cooldown = 0;
            final Optional<GlobalPos> pos_opt = brain.getMemory(AntTasks.NEST_POS);
            if (pos_opt.isPresent())
            {
                final GlobalPos pos = pos_opt.get();
                boolean clearHive = pos.getDimension() != this.world.getDimensionKey();
                final double dist = pos.getPos().distanceSq(this.entity.getPosition());
                // If we have moved too far from the nest, just clear it. In
                // this case, "too far" is more than 100 blocks
                clearHive = clearHive || dist > 10000;
                if (!clearHive)
                {
                    final PointOfInterestManager pois = this.world.getPointOfInterestManager();
                    final long n = pois.getCountInRange(p -> p == PointsOfInterest.NEST.get(), pos.getPos(), 1,
                            PointOfInterestManager.Status.ANY);
                    clearHive = n == 0;

                    if (clearHive && dist < 256 && this.nest != null)
                    {
                        // Lets remake the hive.
                        this.world.setBlockState(pos.getPos(), PokecubeItems.NESTBLOCK.get().getDefaultState());
                        final TileEntity tile = this.world.getTileEntity(pos.getPos());
                        if (tile instanceof NestTile)
                        {
                            final NestTile nest = (NestTile) tile;
                            nest.isType(AntTasks.NESTLOC);
                            nest.addResident(this.pokemob);
                            // Copy over the old habitat info.
                            if (nest.habitat instanceof HabitatProvider)
                                ((HabitatProvider) nest.habitat).setWrapped(this.nest.hab);
                            brain.removeMemory(AntTasks.NO_HIVE_TIMER);
                            this.nest = null;
                            return;
                        }
                    }
                }

                // If we should clear the hive, remove the memory, the
                // HiveSensor will find a new hive.
                if (clearHive) this.entity.getBrain().removeMemory(AntTasks.NEST_POS);
            }
        }
    }

    @Override
    public boolean shouldRun()
    {
        // This always runs, as we use the memory present requirement to decide
        // if it would have been valid anyway
        return true;
    }

}
