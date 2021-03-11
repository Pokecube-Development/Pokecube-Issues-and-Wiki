package pokecube.core.ai.npc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.mojang.datafixers.util.Pair;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.util.WeightedList;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import pokecube.core.ai.brain.RootTask;

public class MultiTask<E extends LivingEntity> extends RootTask<E>
{
    private static final Method CONTINUE;

    static
    {
        CONTINUE = ObfuscationReflectionHelper.findMethod(Task.class, "canStillUse", ServerWorld.class,
                LivingEntity.class, long.class);
    }
    private final Set<MemoryModuleType<?>>      memoryModules;
    private final MultiTask.Ordering            ordering;
    private final MultiTask.RunType             runType;
    private final WeightedList<Task<? super E>> tasks = new WeightedList<>();

    public MultiTask(final Map<MemoryModuleType<?>, MemoryModuleStatus> neededMemories,
            final Set<MemoryModuleType<?>> modules, final MultiTask.Ordering ordering, final MultiTask.RunType type,
            final List<Pair<Task<? super E>, Integer>> tasks)
    {
        super(neededMemories);
        this.memoryModules = modules;
        this.ordering = ordering;
        this.runType = type;
        tasks.forEach((pair) ->
        {
            this.tasks.add(pair.getFirst(), pair.getSecond());
        });
    }

    @Override
    protected boolean canStillUse(final ServerWorld worldIn, final E entityIn, final long gameTimeIn)
    {
        return this.tasks.stream().filter((task) ->
        {
            return task.getStatus() == Task.Status.RUNNING;
        }).anyMatch((task) ->
        {
            boolean var = false;
            try
            {
                var = (boolean) MultiTask.CONTINUE.invoke(task, worldIn, entityIn, gameTimeIn);
            }
            catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
            {
            }
            return var;
        });
    }

    @Override
    protected boolean timedOut(final long gameTime)
    {
        return false;
    }

    @Override
    protected void start(final ServerWorld worldIn, final E entityIn, final long gameTimeIn)
    {
        this.ordering.apply(this.tasks);
        this.runType.process(this.tasks, worldIn, entityIn, gameTimeIn);
    }

    @Override
    protected void tick(final ServerWorld worldIn, final E owner, final long gameTime)
    {
        this.tasks.stream().filter((task) ->
        {
            return task.getStatus() == Task.Status.RUNNING;
        }).forEach((task) ->
        {
            task.tickOrStop(worldIn, owner, gameTime);
        });
    }

    @Override
    protected void stop(final ServerWorld worldIn, final E entityIn, final long gameTimeIn)
    {
        this.tasks.stream().filter((task) ->
        {
            return task.getStatus() == Task.Status.RUNNING;
        }).forEach((task) ->
        {
            task.doStop(worldIn, entityIn, gameTimeIn);
        });
        this.memoryModules.forEach(entityIn.getBrain()::eraseMemory);
    }

    @Override
    public String toString()
    {
        final Set<? extends Task<? super E>> set = this.tasks.stream().filter((task) ->
        {
            return task.getStatus() == Task.Status.RUNNING;
        }).collect(Collectors.toSet());
        return "(" + this.getClass().getSimpleName() + "): " + set;
    }

    static enum Ordering
    {
        ORDERED((list) ->
        {
        }), SHUFFLED(WeightedList::shuffle);

        private final Consumer<WeightedList<?>> consumer;

        private Ordering(final Consumer<WeightedList<?>> consumer)
        {
            this.consumer = consumer;
        }

        public void apply(final WeightedList<?> list)
        {
            this.consumer.accept(list);
        }
    }

    static enum RunType
    {
        RUN_ONE
        {
            @Override
            public <E extends LivingEntity> void process(final WeightedList<Task<? super E>> list,
                    final ServerWorld world, final E mob, final long time)
            {
                list.stream().filter((sub_task) ->
                {
                    return sub_task.getStatus() == Task.Status.STOPPED;
                }).filter((sub_task) ->
                {
                    return sub_task.tryStart(world, mob, time);
                }).findFirst();
            }
        },
        TRY_ALL
        {
            @Override
            public <E extends LivingEntity> void process(final WeightedList<Task<? super E>> list,
                    final ServerWorld world, final E mob, final long time)
            {
                list.stream().filter((sub_task) ->
                {
                    return sub_task.getStatus() == Task.Status.STOPPED;
                }).forEach((sub_task) ->
                {
                    sub_task.tryStart(world, mob, time);
                });
            }
        };

        private RunType()
        {
        }

        public abstract <E extends LivingEntity> void process(WeightedList<Task<? super E>> list, ServerWorld world,
                E mob, long time);
    }
}