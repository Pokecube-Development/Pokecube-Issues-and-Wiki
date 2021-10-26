package pokecube.core.ai.tasks.ants.tasks.work;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.core.ai.tasks.ants.AntTasks;
import pokecube.core.ai.tasks.ants.nest.Node;
import pokecube.core.ai.tasks.ants.tasks.AbstractAntTask;
import pokecube.core.interfaces.IPokemob;
import thut.core.common.ThutCore;

public class Idle extends AbstractAntTask
{
    private static final Map<MemoryModuleType<?>, MemoryStatus> mems = Maps.newHashMap();
    static
    {
        Idle.mems.put(AntTasks.WORK_POS, MemoryStatus.VALUE_ABSENT);
        Idle.mems.put(MemoryModules.WALK_TARGET, MemoryStatus.VALUE_ABSENT);
        Idle.mems.put(MemoryModules.PATH, MemoryStatus.VALUE_ABSENT);
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
        if (this.entity.getNavigation().isInProgress()) return;
        final int num = this.nest.hab.rooms.allRooms.size();
        if (num == 0) return;
        if (this.timer-- > 0) return;
        this.timer = 100;
        final int index = ThutCore.newRandom().nextInt(num);
        final Node room = this.nest.hab.rooms.allRooms.get(index);
        if (!room.started) return;
        // PokecubeCore.LOGGER.debug("wander to {} ({})", room.center,
        // room.type);
        this.setWalkTo(room.getCenter(), 1, 1);
    }

    @Override
    protected boolean doTask()
    {
        if (AntTasks.shouldAntBeInNest(this.world, this.nest.nest.getBlockPos())) return false;
        return true;
    }

}
