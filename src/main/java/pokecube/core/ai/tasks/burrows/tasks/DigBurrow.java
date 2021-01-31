package pokecube.core.ai.tasks.burrows.tasks;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import pokecube.core.ai.tasks.burrows.AbstractBurrowTask;
import pokecube.core.ai.tasks.burrows.BurrowTasks;
import pokecube.core.interfaces.IPokemob;

public class DigBurrow extends AbstractBurrowTask
{
    private static final Map<MemoryModuleType<?>, MemoryModuleStatus> mems = Maps.newHashMap();

    static
    {
        DigBurrow.mems.put(BurrowTasks.JOB_INFO, MemoryModuleStatus.VALUE_PRESENT);
    }

    boolean dig   = false;
    boolean build = false;

    public DigBurrow(final IPokemob pokemob)
    {
        super(pokemob, DigBurrow.mems);
    }

    @Override
    public void reset()
    {
        this.entity.getBrain().removeMemory(BurrowTasks.JOB_INFO);
    }

    @Override
    public void run()
    {
        if (this.dig)
        {
            System.out.println("diggy dig dig");
            System.out.println("diggy dig dig?");
        }
        else
        {

        }
    }

    @Override
    protected boolean doTask()
    {
        this.dig = this.burrow.hab.burrow.shouldDig(this.world.getGameTime());
        this.build = this.burrow.hab.burrow.shouldBuild(this.world.getGameTime());
        return this.dig || this.build;
    }

}
