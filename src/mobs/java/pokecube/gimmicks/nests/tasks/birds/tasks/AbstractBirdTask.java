package pokecube.gimmicks.nests.tasks.birds.tasks;

import com.google.common.collect.Maps;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.entity.pokemob.ai.GeneralStates;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.core.ai.tasks.TaskBase;
import pokecube.core.ai.tasks.utility.UtilBehaviour;
import pokecube.core.eventhandlers.MoveEventsHandler;
import pokecube.gimmicks.nests.tasks.birds.BirdTasks;
import pokecube.gimmicks.nests.tasks.birds.sensors.NestSensor;
import pokecube.gimmicks.nests.tasks.birds.sensors.NestSensor.BirdNest;
import thut.api.entity.ai.RootTask;
import thut.api.maths.Vector3;

import java.util.List;
import java.util.Map;

public abstract class AbstractBirdTask extends TaskBase
{
    private static final Map<MemoryModuleType<?>, MemoryStatus> mems = Maps.newHashMap();

    static
    {
        // Don't run if we don't have a hive
        // The HiveSensor will try to set this if it is invalid.
        AbstractBirdTask.mems.put(MemoryModules.NEST_POS.get(), MemoryStatus.VALUE_PRESENT);
    }

    protected BirdNest nest;

    private int check_timer = 0;

    public AbstractBirdTask()
    {
        super(AbstractBirdTask.mems);
    }

    public AbstractBirdTask(final Map<MemoryModuleType<?>, MemoryStatus> neededMems)
    {
        super(RootTask.merge(AbstractBirdTask.mems, neededMems));
    }

    public void tryHarvest(ServerLevel level, IPokemob pokemob, final BlockPos pos, final boolean breakOnly)
    {
        final Vector3 v = new Vector3();
        final BlockState state = level.getBlockState(pos);
        if (breakOnly)
        {
            var entity = pokemob.getEntity();
            if (UtilBehaviour.diggable.test(state) && MoveEventsHandler.canAffectBlock(pokemob, v.set(pos), "nest_dig",
                    false, false))
            {
                level.destroyBlock(pos, true, entity);
                // attempt to collect the drops
                final List<ItemEntity> drops = level.getEntitiesOfClass(ItemEntity.class, v.getAABB().inflate(3));
                for (final ItemEntity e : drops)
                {
                    final ItemStack stack = e.getItem().copy();
                    new InventoryChange(entity, 2, stack, true).run(level);
                    e.setItem(ItemStack.EMPTY);
                }
            }
        }
        else
        {
            // Otherwise we should apply via the normal gather task rules!

        }
    }

    abstract protected boolean doTask(IPokemob pokemob);

    @Override
    public boolean loadThrottle()
    {
        return true;
    }

    @Override
    public boolean shouldRun(Mob entity)
    {
        if (this.nest == null || this.check_timer-- < 0)
        {
            this.nest = NestSensor.getNest(entity).orElse(null);
            this.check_timer = 1200;
        }
        if (this.nest == null) return false;
        var pokemob = PokemobCaps.getPokemobFor(entity);
        final boolean tameCheck = pokemob.getOwnerId() == null || pokemob.getGeneralState(GeneralStates.STAYING);
        final boolean aiEnabled = pokemob.isRoutineEnabled(BirdTasks.BIRD_NESTS);
        return tameCheck && aiEnabled && this.doTask(pokemob);
    }

}
