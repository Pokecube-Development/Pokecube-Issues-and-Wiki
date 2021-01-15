package pokecube.core.ai.tasks.idle.ants;

import java.util.Map;
import java.util.Optional;

import com.google.common.collect.Maps;

import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.util.math.GlobalPos;
import pokecube.core.handlers.events.SpawnHandler;
import pokecube.core.interfaces.IPokemob;
import thut.api.maths.Vector3;

public class Patrol extends AntTask
{
    private static final Map<MemoryModuleType<?>, MemoryModuleStatus> mems = Maps.newHashMap();
    static
    {
        // Only patrol if not working
        Patrol.mems.put(AntTasks.WORK_POS, MemoryModuleStatus.VALUE_ABSENT);
    }

    int patrolTimer = 0;

    public Patrol(final IPokemob pokemob)
    {
        super(pokemob, Patrol.mems);
    }

    @Override
    public void reset()
    {
        this.patrolTimer = 0;
    }

    @Override
    public void run()
    {
        Vector3 spot = Vector3.getNewVector();
        final Optional<GlobalPos> pos_opt = this.entity.getBrain().getMemory(AntTasks.NEST_POS);
        if (pos_opt.isPresent())
        {
            spot.set(pos_opt.get().getPos());

            spot = SpawnHandler.getRandomPointNear(this.world, spot, 8);
            if (spot != null) this.setWalkTo(spot, 1, 1);
        }
        this.reset();
    }

    @Override
    boolean doTask()
    {
        return this.patrolTimer++ > 100;
    }

}
