package pokecube.gimmicks.nests.tasks.ants.tasks.nest;

import java.util.Map;
import java.util.Optional;

import com.google.common.collect.Maps;

import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.level.block.entity.BlockEntity;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.core.PokecubeItems;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.core.ai.poi.PointsOfInterest;
import pokecube.core.ai.tasks.idle.BaseIdleTask;
import pokecube.core.blocks.nests.NestTile;
import pokecube.gimmicks.nests.tasks.ants.nest.AntHabitat;
import pokecube.gimmicks.nests.tasks.ants.sensors.NestSensor;
import pokecube.gimmicks.nests.tasks.ants.sensors.NestSensor.AntNest;
import pokecube.gimmicks.nests.tasks.bees.BeeTasks;

public class CheckNest extends BaseIdleTask
{
    private static final Map<MemoryModuleType<?>, MemoryStatus> mems = Maps.newHashMap();
    static
    {
        // Only run if we have a nest
        CheckNest.mems.put(MemoryModules.NEST_POS.get(), MemoryStatus.VALUE_PRESENT);
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
        final Optional<Integer> hiveTimer = brain.getMemory(MemoryModules.OUT_OF_NEST_TIMER.get());
        final int time = hiveTimer.orElseGet(() -> 0) - 1;
        brain.setMemory(BeeTasks.OUT_OF_HIVE_TIMER.get(), time);
        if (this.nest == null) this.nest = NestSensor.getNest(this.entity).orElse(null);

        if (this.new_hive_cooldown % 10 == 0)
        {
            // Lets sync items with other ants in the nest.
            if (brain.hasMemoryValue(MemoryModules.VISIBLE_ITEMS.get()))
            {
                var seen = brain.getMemory(MemoryModules.VISIBLE_ITEMS.get());
                if (seen.isPresent() && this.nest != null && this.nest.hab != null)
                {
                    var list = seen.get();
                    for (var item : list) if (!nest.hab.items.contains(item)) nest.hab.items.add(item);
                }
            }
        }

        if (this.new_hive_cooldown++ % 60 == 0)
        {
            final Optional<GlobalPos> pos_opt = brain.getMemory(MemoryModules.NEST_POS.get());
            if (pos_opt.isPresent())
            {
                final GlobalPos pos = pos_opt.get();
                boolean clearHive = pos.dimension() != this.world.dimension();
                final double dist = pos.pos().distSqr(this.entity.blockPosition());
                // If we have moved too far from the nest, just clear it. In
                // this case, "too far" is more than 100 blocks
                clearHive = clearHive || dist > 10000;
                if (!clearHive)
                {
                    final PoiManager pois = this.world.getPoiManager();
                    final long n = pois.getCountInRange(p -> p == PointsOfInterest.NEST.get(), pos.pos(), 1,
                            PoiManager.Occupancy.ANY);
                    clearHive = n == 0;

                    if (clearHive && dist < 256 && this.nest != null && this.world.isLoaded(pos.pos()))
                    {
                        // Lets remake the hive.
                        this.world.setBlockAndUpdate(pos.pos(), PokecubeItems.NEST.get().defaultBlockState());
                        final BlockEntity tile = this.world.getBlockEntity(pos.pos());
                        if (tile instanceof NestTile nest)
                        {
                            nest.setWrappedHab(new AntHabitat());
                            nest.addResident(this.pokemob);
                            // Copy over the old habitat info.
                            nest.setWrappedHab(this.nest.hab);
                            brain.eraseMemory(MemoryModules.NO_NEST_TIMER.get());
                            this.nest = null;
                            return;
                        }
                    }
                }
                // If we should clear the hive, remove the memory, the
                // HiveSensor will find a new hive.
                if (clearHive) this.entity.getBrain().eraseMemory(MemoryModules.NEST_POS.get());
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
