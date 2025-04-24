package pokecube.core.ai.tasks.utility;

import com.google.common.collect.Maps;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.level.block.state.BlockState;
import pokecube.core.PokecubeItems;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.core.ai.tasks.PokemobBehaviour;
import pokecube.world.terrain.PokecubeTerrainChecker;
import thut.api.entity.ai.RootTask;

import java.util.Map;
import java.util.function.Predicate;

public abstract class UtilBehaviour extends PokemobBehaviour
{
    private static final Map<MemoryModuleType<?>, MemoryStatus> MEMS = Maps.newHashMap();

    static
    {
        UtilBehaviour.MEMS.put(MemoryModules.ATTACKTARGET.get(), MemoryStatus.VALUE_ABSENT);
    }

    public static Predicate<BlockState> diggable = state ->
            (PokecubeTerrainChecker.isTerrain(state) || PokecubeTerrainChecker.isRock(state)
                    || PokecubeTerrainChecker.isCutablePlant(state) || PokecubeTerrainChecker.isLeaves(state)
                    || PokecubeTerrainChecker.isWood(state)) && state.getBlock() != PokecubeItems.NEST.get();

    public UtilBehaviour()
    {
        super(UtilBehaviour.MEMS);
    }

    public UtilBehaviour(final Map<MemoryModuleType<?>, MemoryStatus> mems)
    {
        super(RootTask.merge(UtilBehaviour.MEMS, mems));
    }
}
