package pokecube.core.ai.tasks.ants.tasks;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import com.google.common.collect.Maps;

import net.minecraft.block.BlockState;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import pokecube.core.PokecubeItems;
import pokecube.core.ai.tasks.ants.AntTasks;
import pokecube.core.ai.tasks.ants.AntTasks.AntJob;
import pokecube.core.ai.tasks.utility.StoreTask;
import pokecube.core.handlers.events.MoveEventsHandler;
import pokecube.core.interfaces.IMoveConstants.AIRoutine;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.world.terrain.PokecubeTerrainChecker;
import thut.api.entity.ai.IAIRunnable;
import thut.api.maths.Vector3;

public abstract class AbstractWorkTask extends AbstractAntTask
{
    private static final Map<MemoryModuleType<?>, MemoryModuleStatus> mems = Maps.newHashMap();
    static
    {
        AbstractWorkTask.mems.put(AntTasks.WORK_POS, MemoryModuleStatus.VALUE_PRESENT);
        AbstractWorkTask.mems.put(AntTasks.GOING_HOME, MemoryModuleStatus.VALUE_ABSENT);
    }
    protected StoreTask storage = null;

    protected Predicate<BlockState> diggable = state -> (PokecubeTerrainChecker.isTerrain(state)
            || PokecubeTerrainChecker.isRock(state) || PokecubeTerrainChecker.isCutablePlant(state)
            || PokecubeTerrainChecker.isLeaves(state) || PokecubeTerrainChecker.isWood(state)) && state
                    .getBlock() != PokecubeItems.NESTBLOCK.get();

    private final Predicate<AntJob> validJob;

    public AbstractWorkTask(final IPokemob pokemob, final Predicate<AntJob> job)
    {
        super(pokemob, AbstractWorkTask.mems);
        this.validJob = job;
    }

    public AbstractWorkTask(final IPokemob pokemob, final Map<MemoryModuleType<?>, MemoryModuleStatus> mems,
            final Predicate<AntJob> job)
    {
        super(pokemob, mems);
        this.validJob = job;
    }

    public boolean tryHarvest(final BlockPos pos, final boolean breakOnly)
    {
        final Vector3 v = Vector3.getNewVector();
        final BlockState state = this.world.getBlockState(pos);
        if (breakOnly)
        {
            if (this.diggable.test(state) && MoveEventsHandler.canAffectBlock(this.pokemob, v.set(pos), "ant_dig",
                    false, false))
            {
                this.world.destroyBlock(pos, true, this.entity);
                // attempt to collect the drops
                final List<ItemEntity> drops = this.world.getEntitiesWithinAABB(ItemEntity.class, v.getAABB().grow(3));
                for (final ItemEntity e : drops)
                {
                    final ItemStack stack = e.getItem().copy();
                    new InventoryChange(this.entity, 2, stack, true).run(this.world);
                    e.setItem(ItemStack.EMPTY);
                }
                return true;
            }
        }
        else
        {
            // Otherwise we should apply via the normal gather task rules!

        }
        return false;
    }

    protected boolean shouldWork()
    {
        return true;
    }

    @Override
    public final boolean doTask()
    {
        if (AntTasks.shouldAntBeInNest(this.world, this.nest.nest.getPos())) return false;
        final Brain<?> brain = this.entity.getBrain();
        if (!brain.hasMemory(AntTasks.WORK_POS)) return false;

        if (this.storage == null) for (final IAIRunnable run : this.pokemob.getTasks())
            if (run instanceof StoreTask)
            {
                this.storage = (StoreTask) run;
                this.pokemob.setRoutineState(AIRoutine.STORE, true);
                this.storage.storageLoc = this.nest.nest.getPos();
                this.storage.berryLoc = this.nest.nest.getPos();
                break;
            }
        if (this.storage == null) return false;
        if (!this.validJob.test(this.job)) return false;
        return this.shouldWork();
    }
}
