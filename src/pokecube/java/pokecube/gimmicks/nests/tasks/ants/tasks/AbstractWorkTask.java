package pokecube.gimmicks.nests.tasks.ants.tasks;

import com.google.common.collect.Maps;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.core.ai.tasks.utility.StoreItems;
import pokecube.core.ai.tasks.utility.UtilBehaviour;
import pokecube.core.eventhandlers.MoveEventsHandler;
import pokecube.gimmicks.nests.tasks.ants.AntTasks;
import pokecube.gimmicks.nests.tasks.ants.AntTasks.AntJob;
import thut.api.maths.Vector3;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public abstract class AbstractWorkTask extends AbstractAntTask
{
    private static final Map<MemoryModuleType<?>, MemoryStatus> mems = Maps.newHashMap();

    static
    {
        AbstractWorkTask.mems.put(MemoryModules.WORK_POS.get(), MemoryStatus.VALUE_PRESENT);
        AbstractWorkTask.mems.put(MemoryModules.GOING_HOME.get(), MemoryStatus.VALUE_ABSENT);
    }

    private final Predicate<AntJob> validJob;

    public AbstractWorkTask(final Predicate<AntJob> job)
    {
        super(AbstractWorkTask.mems);
        this.validJob = job;
    }

    public AbstractWorkTask(final Map<MemoryModuleType<?>, MemoryStatus> mems, final Predicate<AntJob> job)
    {
        super(mems);
        this.validJob = job;
    }

    public boolean tryHarvest(ServerLevel level, IPokemob pokemob, BlockPos pos, boolean breakOnly)
    {
        final Vector3 v = new Vector3();
        final BlockState state = level.getBlockState(pos);
        if (breakOnly)
        {
            if (UtilBehaviour.diggable.test(state) && MoveEventsHandler.canAffectBlock(pokemob, v.set(pos), "nest_dig",
                    false, false))
            {
                level.destroyBlock(pos, true, pokemob.getEntity());
                // attempt to collect the drops
                final List<ItemEntity> drops = level.getEntitiesOfClass(ItemEntity.class, v.getAABB().inflate(3));
                for (final ItemEntity e : drops)
                {
                    final ItemStack stack = e.getItem().copy();
                    new InventoryChange(pokemob.getEntity(), 2, stack, true).run(level);
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

    protected boolean shouldWork(Mob entity)
    {
        return true;
    }

    @Override
    public final boolean doTask(Mob entity)
    {
        if (!this.validJob.test(this.job)) return false;
        if (AntTasks.shouldAntBeInNest((ServerLevel) entity.level(), this.nest.nest.getBlockPos())) return false;
        final Brain<?> brain = entity.getBrain();
        if (!brain.hasMemoryValue(MemoryModules.WORK_POS.get())) return false;
        var storage = entity.getData(StoreItems.StoreBehaviour.TYPE);
        if (storage == null) return false;
        return this.shouldWork(entity);
    }
}
