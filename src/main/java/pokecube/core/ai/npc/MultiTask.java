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

public class MultiTask<E extends LivingEntity> extends Task<E>
{
    private static final Method CONTINUE;

    static
    {
        CONTINUE = ObfuscationReflectionHelper.findMethod(Task.class, "func_212834_g_", ServerWorld.class,
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
            this.tasks.func_226313_a_(pair.getFirst(), pair.getSecond());
        });
    }

    @Override
    protected boolean shouldContinueExecuting(final ServerWorld worldIn, final E entityIn, final long gameTimeIn)
    {
        return this.tasks.func_220655_b().filter((task) ->
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
    protected boolean isTimedOut(final long gameTime)
    {
        return false;
    }

    @Override
    protected void startExecuting(final ServerWorld worldIn, final E entityIn, final long gameTimeIn)
    {
        this.ordering.func_220628_a(this.tasks);
        this.runType.func_220630_a(this.tasks, worldIn, entityIn, gameTimeIn);
    }

    @Override
    protected void updateTask(final ServerWorld worldIn, final E owner, final long gameTime)
    {
        this.tasks.func_220655_b().filter((p_220408_0_) ->
        {
            return p_220408_0_.getStatus() == Task.Status.RUNNING;
        }).forEach((p_220409_4_) ->
        {
            p_220409_4_.tick(worldIn, owner, gameTime);
        });
    }

    @Override
    protected void resetTask(final ServerWorld worldIn, final E entityIn, final long gameTimeIn)
    {
        this.tasks.func_220655_b().filter((p_220407_0_) ->
        {
            return p_220407_0_.getStatus() == Task.Status.RUNNING;
        }).forEach((p_220412_4_) ->
        {
            p_220412_4_.stop(worldIn, entityIn, gameTimeIn);
        });
        this.memoryModules.forEach(entityIn.getBrain()::removeMemory);
    }

    @Override
    public String toString()
    {
        final Set<? extends Task<? super E>> set = this.tasks.func_220655_b().filter((p_220410_0_) ->
        {
            return p_220410_0_.getStatus() == Task.Status.RUNNING;
        }).collect(Collectors.toSet());
        return "(" + this.getClass().getSimpleName() + "): " + set;
    }

    static enum Ordering
    {
        ORDERED((p_220627_0_) ->
        {
        }), SHUFFLED(WeightedList::func_226309_a_);

        private final Consumer<WeightedList<?>> field_220629_c;

        private Ordering(final Consumer<WeightedList<?>> p_i50849_3_)
        {
            this.field_220629_c = p_i50849_3_;
        }

        public void func_220628_a(final WeightedList<?> p_220628_1_)
        {
            this.field_220629_c.accept(p_220628_1_);
        }
    }

    static enum RunType
    {
        RUN_ONE
        {
            @Override
            public <E extends LivingEntity> void func_220630_a(final WeightedList<Task<? super E>> p_220630_1_,
                    final ServerWorld p_220630_2_, final E p_220630_3_, final long p_220630_4_)
            {
                p_220630_1_.func_220655_b().filter((p_220634_0_) ->
                {
                    return p_220634_0_.getStatus() == Task.Status.STOPPED;
                }).filter((p_220633_4_) ->
                {
                    return p_220633_4_.start(p_220630_2_, p_220630_3_, p_220630_4_);
                }).findFirst();
            }
        },
        TRY_ALL
        {
            @Override
            public <E extends LivingEntity> void func_220630_a(final WeightedList<Task<? super E>> p_220630_1_,
                    final ServerWorld p_220630_2_, final E p_220630_3_, final long p_220630_4_)
            {
                p_220630_1_.func_220655_b().filter((p_220632_0_) ->
                {
                    return p_220632_0_.getStatus() == Task.Status.STOPPED;
                }).forEach((p_220631_4_) ->
                {
                    p_220631_4_.start(p_220630_2_, p_220630_3_, p_220630_4_);
                });
            }
        };

        private RunType()
        {
        }

        public abstract <E extends LivingEntity> void func_220630_a(WeightedList<Task<? super E>> p_220630_1_,
                ServerWorld p_220630_2_, E p_220630_3_, long p_220630_4_);
    }
}