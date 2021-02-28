package pokecube.core.ai.tasks.burrows;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.block.BlockState;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import pokecube.core.ai.brain.RootTask;
import pokecube.core.ai.tasks.TaskBase;
import pokecube.core.ai.tasks.burrows.sensors.BurrowSensor;
import pokecube.core.ai.tasks.burrows.sensors.BurrowSensor.Burrow;
import pokecube.core.ai.tasks.utility.UtilTask;
import pokecube.core.handlers.events.MoveEventsHandler;
import pokecube.core.interfaces.IMoveConstants.AIRoutine;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import thut.api.maths.Vector3;

public abstract class AbstractBurrowTask extends TaskBase
{
    private static final Map<MemoryModuleType<?>, MemoryModuleStatus> mems = Maps.newHashMap();
    static
    {
        // Don't run if we don't have a hive
        // The HiveSensor will try to set this if it is invalid.
        AbstractBurrowTask.mems.put(BurrowTasks.BURROW, MemoryModuleStatus.VALUE_PRESENT);
    }

    protected Burrow burrow;

    private int check_timer = 0;

    public AbstractBurrowTask(final IPokemob pokemob)
    {
        super(pokemob, AbstractBurrowTask.mems);
    }

    public AbstractBurrowTask(final IPokemob pokemob, final Map<MemoryModuleType<?>, MemoryModuleStatus> neededMems)
    {
        super(pokemob, RootTask.merge(AbstractBurrowTask.mems, neededMems));
    }

    public boolean tryHarvest(final BlockPos pos, final boolean breakOnly)
    {
        final Vector3 v = Vector3.getNewVector();
        final BlockState state = this.world.getBlockState(pos);
        if (breakOnly)
        {
            if (UtilTask.diggable.test(state) && MoveEventsHandler.canAffectBlock(this.pokemob, v.set(pos), "nest_dig",
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

    abstract protected boolean doTask();

    @Override
    public boolean loadThrottle()
    {
        return true;
    }

    @Override
    public boolean shouldRun()
    {
        if (this.burrow == null || this.check_timer-- < 0)
        {
            this.burrow = BurrowSensor.getNest(this.entity).orElse(null);
            this.check_timer = 1200;
        }
        if (this.burrow == null) return false;
        final boolean tameCheck = this.pokemob.getOwnerId() == null || this.pokemob.getGeneralState(
                GeneralStates.STAYING);
        final boolean aiEnabled = this.pokemob.isRoutineEnabled(AIRoutine.BURROWS);
        return tameCheck && aiEnabled && this.doTask();
    }

}
