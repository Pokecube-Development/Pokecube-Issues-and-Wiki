package pokecube.core.ai.tasks.ants.tasks.work;

import java.util.Map;
import java.util.Random;

import com.google.common.collect.Maps;

import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.core.ai.tasks.ants.AntTasks;
import pokecube.core.ai.tasks.ants.nest.Node;
import pokecube.core.ai.tasks.ants.tasks.AbstractAntTask;
import pokecube.core.interfaces.IPokemob;

public class Idle extends AbstractAntTask
{
    private static final Map<MemoryModuleType<?>, MemoryModuleStatus> mems = Maps.newHashMap();
    static
    {
        Idle.mems.put(AntTasks.WORK_POS, MemoryModuleStatus.VALUE_ABSENT);
        Idle.mems.put(MemoryModules.WALK_TARGET, MemoryModuleStatus.VALUE_ABSENT);
        Idle.mems.put(MemoryModules.PATH, MemoryModuleStatus.VALUE_ABSENT);
    }

    int timer = 0;

    public Idle(final IPokemob pokemob)
    {
        super(pokemob, Idle.mems);
    }

    @Override
    public void reset()
    {
        this.timer = 0;
    }

    @Override
    public void run()
    {
        if (this.entity.getNavigator().hasPath()) return;
        final int num = this.nest.hab.rooms.allRooms.size();
        if (num == 0) return;
        if (this.timer-- > 0) return;
        this.timer = 100;
        final int index = new Random().nextInt(num);
        final Node room = this.nest.hab.rooms.allRooms.get(index);
        if (!room.started) return;
        // PokecubeCore.LOGGER.debug("wander to {} ({})", room.center,
        // room.type);
        this.setWalkTo(room.getCenter(), 1, 1);
    }

    @Override
    protected boolean doTask()
    {
        if (AntTasks.shouldAntBeInNest(this.world, this.nest.nest.getPos())) return false;
        return true;
    }

}
