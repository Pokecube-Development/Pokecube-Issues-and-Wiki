package pokecube.gimmicks.nests.tasks.ants.tasks.work;

import com.google.common.collect.Maps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.gimmicks.nests.tasks.ants.AntTasks;
import pokecube.gimmicks.nests.tasks.ants.nest.Node;
import pokecube.gimmicks.nests.tasks.ants.tasks.AbstractAntTask;
import thut.core.common.ThutCore;

import java.util.Map;

public class Idle extends AbstractAntTask
{
    private static final Map<MemoryModuleType<?>, MemoryStatus> mems = Maps.newHashMap();

    static
    {
        Idle.mems.put(MemoryModules.WORK_POS.get(), MemoryStatus.VALUE_ABSENT);
        Idle.mems.put(MemoryModules.WALK_TARGET, MemoryStatus.VALUE_ABSENT);
        Idle.mems.put(MemoryModules.PATH, MemoryStatus.VALUE_ABSENT);
    }

    int timer = 0;

    public Idle()
    {
        super(Idle.mems);
    }

    @Override
    public void reset(Mob entityIn)
    {
        this.timer = 0;
    }

    @Override
    protected void tick(final ServerLevel level, final Mob entity, final long gameTime)
    {
        if (entity.getNavigation().isInProgress()) return;
        final int num = this.nest.hab.rooms.allRooms.size();
        if (num == 0) return;
        if (this.timer-- > 0) return;
        this.timer = 100;
        final int index = ThutCore.newRandom().nextInt(num);
        final Node room = this.nest.hab.rooms.allRooms.get(index);
        if (!room.started) return;
        // PokecubeAPI.logDebug("wander to {} ({})", room.center,
        // room.type);
        this.setWalkTo(entity, room.getCenter(), 1, 1);
    }

    @Override
    protected boolean doTask(Mob entity)
    {
        return !AntTasks.shouldAntBeInNest((ServerLevel) entity.level(), this.nest.nest.getBlockPos());
    }

}
