package pokecube.core.ai.tasks.utility;

import java.util.Map;
import java.util.function.Predicate;

import com.google.common.collect.Maps;

import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.level.block.state.BlockState;
import pokecube.core.PokecubeItems;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.core.ai.brain.RootTask;
import pokecube.core.ai.tasks.TaskBase;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.world.terrain.PokecubeTerrainChecker;

public abstract class UtilTask extends TaskBase
{
    private static final Map<MemoryModuleType<?>, MemoryStatus> MEMS = Maps.newHashMap();

    static
    {
        UtilTask.MEMS.put(MemoryModules.ATTACKTARGET, MemoryStatus.VALUE_ABSENT);
    }

    public static Predicate<BlockState> diggable = state -> (PokecubeTerrainChecker.isTerrain(state)
            || PokecubeTerrainChecker.isRock(state) || PokecubeTerrainChecker.isCutablePlant(state)
            || PokecubeTerrainChecker.isLeaves(state) || PokecubeTerrainChecker.isWood(state)) && state
                    .getBlock() != PokecubeItems.NESTBLOCK.get();

    public UtilTask(final IPokemob pokemob)
    {
        super(pokemob, UtilTask.MEMS);
    }

    public UtilTask(final IPokemob pokemob, final Map<MemoryModuleType<?>, MemoryStatus> mems)
    {
        super(pokemob, RootTask.merge(UtilTask.MEMS, mems));
    }

    @Override
    public boolean loadThrottle()
    {
        return true;
    }
}
