package pokecube.core.ai.npc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.mojang.datafixers.util.Pair;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.ShufflingList;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import pokecube.core.ai.brain.RootTask;

public class MultiTask<E extends LivingEntity> extends RootTask<E>
{
    private static final Method CONTINUE;

    static
    {
        CONTINUE = ObfuscationReflectionHelper.findMethod(Behavior.class, "func_212834_g_", ServerLevel.class,
                LivingEntity.class, long.class);
    }
    private final Set<MemoryModuleType<?>> memories;

    private final MultiTask.Ordering ordering;
    private final MultiTask.RunType  runType;

    private final ShufflingList<Behavior<? super E>> behaviors = new ShufflingList<>();

    public MultiTask(final Map<MemoryModuleType<?>, MemoryStatus> neededMemories,
            final Set<MemoryModuleType<?>> modules, final MultiTask.Ordering ordering, final MultiTask.RunType type,
            final List<Pair<Behavior<? super E>, Integer>> tasks)
    {
        super(neededMemories);
        this.memories = modules;
        this.ordering = ordering;
        this.runType = type;
        tasks.forEach((pair) ->
        {
            this.behaviors.add(pair.getFirst(), pair.getSecond());
        });
    }

    @Override
    protected boolean canStillUse(final ServerLevel worldIn, final E entityIn, final long gameTimeIn)
    {
        return this.behaviors.stream().filter((task) ->
        {
            return task.getStatus() == Behavior.Status.RUNNING;
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
    protected void start(final ServerLevel worldIn, final E entityIn, final long gameTimeIn)
    {
        this.ordering.apply(this.behaviors);
        this.runType.process(this.behaviors, worldIn, entityIn, gameTimeIn);
    }

    @Override
    protected void tick(final ServerLevel worldIn, final E owner, final long gameTime)
    {
        this.behaviors.stream().filter((task) ->
        {
            return task.getStatus() == Behavior.Status.RUNNING;
        }).forEach((task) ->
        {
            task.tickOrStop(worldIn, owner, gameTime);
        });
    }

    @Override
    protected void stop(final ServerLevel worldIn, final E entityIn, final long gameTimeIn)
    {
        this.behaviors.stream().filter((task) ->
        {
            return task.getStatus() == Behavior.Status.RUNNING;
        }).forEach((task) ->
        {
            task.doStop(worldIn, entityIn, gameTimeIn);
        });
        this.memories.forEach(entityIn.getBrain()::eraseMemory);
    }

    @Override
    public String toString()
    {
        final Set<? extends Behavior<? super E>> set = this.behaviors.stream().filter((task) ->
        {
            return task.getStatus() == Behavior.Status.RUNNING;
        }).collect(Collectors.toSet());
        return "(" + this.getClass().getSimpleName() + "): " + set;
    }

    static enum Ordering
    {
        ORDERED((list) ->
        {
        }), SHUFFLED(ShufflingList::shuffle);

        private final Consumer<ShufflingList<?>> consumer;

        private Ordering(final Consumer<ShufflingList<?>> consumer)
        {
            this.consumer = consumer;
        }

        public void apply(final ShufflingList<?> list)
        {
            this.consumer.accept(list);
        }
    }

    static enum RunType
    {
        RUN_ONE
        {
            @Override
            public <E extends LivingEntity> void process(final ShufflingList<Behavior<? super E>> list,
                    final ServerLevel world, final E mob, final long time)
            {
                list.stream().filter((sub_task) ->
                {
                    return sub_task.getStatus() == Behavior.Status.STOPPED;
                }).filter((sub_task) ->
                {
                    return sub_task.tryStart(world, mob, time);
                }).findFirst();
            }
        },
        TRY_ALL
        {
            @Override
            public <E extends LivingEntity> void process(final ShufflingList<Behavior<? super E>> list,
                    final ServerLevel world, final E mob, final long time)
            {
                list.stream().filter((sub_task) ->
                {
                    return sub_task.getStatus() == Behavior.Status.STOPPED;
                }).forEach((sub_task) ->
                {
                    sub_task.tryStart(world, mob, time);
                });
            }
        };

        private RunType()
        {
        }

        public abstract <E extends LivingEntity> void process(ShufflingList<Behavior<? super E>> list,
                ServerLevel world, E mob, long time);
    }
}