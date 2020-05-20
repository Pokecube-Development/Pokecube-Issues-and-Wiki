package pokecube.core.ai.tasks;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.memory.WalkTarget;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.interfaces.pokemob.ai.LogicStates;
import thut.api.entity.ai.IAIRunnable;
import thut.api.entity.ai.ITask;
import thut.api.maths.Vector3;
import thut.api.terrain.TerrainManager;
import thut.lib.ItemStackTools;

public abstract class TaskBase<E extends LivingEntity> extends Task<E> implements ITask
{
    /** Thread safe inventory setting for pokemobs. */
    public static class InventoryChange implements IRunnable
    {
        public final int       entity;
        public final int       slot;
        public final int       minSlot;
        public final ItemStack stack;

        public InventoryChange(final Entity entity, final int slot, final ItemStack stack, final boolean min)
        {
            this.entity = entity.getEntityId();
            this.stack = stack;
            if (min)
            {
                this.minSlot = slot;
                this.slot = -1;
            }
            else
            {
                this.slot = slot;
                this.minSlot = 0;
            }
        }

        @Override
        public boolean run(final World world)
        {
            final Entity e = world.getEntityByID(this.entity);
            final IPokemob pokemob = CapabilityPokemob.getPokemobFor(e);
            if (e == null || pokemob == null) return false;
            if (this.slot > 0) pokemob.getInventory().setInventorySlotContents(this.slot, this.stack);
            else if (!ItemStackTools.addItemStackToInventory(this.stack, pokemob.getInventory(), this.minSlot)) e
                    .entityDropItem(this.stack, 0);
            return true;
        }

    }

    public static interface IRunnable
    {
        /**
         * @param world
         * @return task ran sucessfully
         */
        boolean run(World world);
    }

    /** Thread safe sound playing. */
    public static class PlaySound implements IRunnable
    {
        final DimensionType dim;
        final Vector3       loc;
        final SoundEvent    sound;
        final SoundCategory cat;
        final float         volume;
        final float         pitch;

        public PlaySound(final DimensionType dim, final Vector3 loc, final SoundEvent sound, final SoundCategory cat,
                final float volume, final float pitch)
        {
            this.dim = dim;
            this.sound = sound;
            this.volume = volume;
            this.loc = loc;
            this.pitch = pitch;
            this.cat = cat;
        }

        @Override
        public boolean run(final World world)
        {
            if (this.dim != world.getDimension().getType()) return false;
            world.playSound(null, this.loc.x, this.loc.y, this.loc.z, this.sound, this.cat, this.volume, this.pitch);
            return true;
        }

    }

    public static Map<MemoryModuleType<?>, MemoryModuleStatus> merge(
            final Map<MemoryModuleType<?>, MemoryModuleStatus> mems2,
            final Map<MemoryModuleType<?>, MemoryModuleStatus> mems3)
    {
        final Map<MemoryModuleType<?>, MemoryModuleStatus> ret = Maps.newHashMap();
        ret.putAll(mems2);
        ret.putAll(mems3);
        return ImmutableMap.copyOf(mems3);
    }

    protected final IPokemob    pokemob;
    protected final MobEntity   entity;
    protected final ServerWorld world;

    protected List<IRunnable> toRun = Lists.newArrayList();

    final Map<MemoryModuleType<?>, MemoryModuleStatus> neededMems;

    int priority = 0;
    int mutex    = 0;

    public TaskBase(final IPokemob pokemob)
    {
        this(pokemob, ImmutableMap.of());
    }

    public TaskBase(final IPokemob pokemob, final Map<MemoryModuleType<?>, MemoryModuleStatus> neededMems)
    {
        super(neededMems);
        this.pokemob = pokemob;
        this.entity = pokemob.getEntity();
        if (this.entity.getEntityWorld() instanceof ServerWorld) this.world = (ServerWorld) this.entity
                .getEntityWorld();
        else this.world = null;
        this.neededMems = ImmutableMap.copyOf(neededMems);
    }

    protected boolean addEntityPath(final MobEntity entity, final Path path, final double speed)
    {
        if (path == null) entity.getBrain().removeMemory(MemoryModuleType.WALK_TARGET);
        else
        {
            final PathPoint end = path.getFinalPathPoint();
            final WalkTarget target = new WalkTarget(new BlockPos(end.x, end.y, end.z), (float) speed, 10);
            entity.getBrain().setMemory(MemoryModuleType.WALK_TARGET, target);
        }
        entity.getBrain().setMemory(MemoryModuleType.PATH, path);
        return entity.getNavigator().setPath(path, speed);
    }

    protected void addMoveInfo(final IPokemob attacker, final Entity targetEnt, final Vector3 target,
            final float distance)
    {
        attacker.executeMove(targetEnt, target, distance);
    }

    protected boolean canMove()
    {
        // TODO in here, check things like being bound.
        return true;
    }

    @Override
    public void finish()
    {
        this.toRun.forEach(w -> w.run(this.world));
        this.toRun.clear();
    }

    protected <T extends Entity> List<T> getEntitiesWithinDistance(final Entity source, final float distance,
            final Class<T> clazz, final Class<?>... targetClass)
    {
        if (!TerrainManager.isAreaLoaded(source.getEntityWorld(), source.getPosition(), distance)) return Collections
                .emptyList();
        return this.getEntitiesWithinDistance(source.getEntityWorld(), source.getPosition(), distance, clazz,
                targetClass);
    }

    protected <T extends Entity> List<T> getEntitiesWithinDistance(final World world, final BlockPos pos,
            final float distance, final Class<T> clazz, final Class<?>... targetClass)
    {
        if (!TerrainManager.isAreaLoaded(world, pos, distance)) return Collections.emptyList();
        return world.getEntitiesWithinAABB(clazz, new AxisAlignedBB(pos).grow(distance), e ->
        {
            if (clazz.isInstance(e)) return true;
            for (final Class<?> c : targetClass)
                if (c.isInstance(e)) return true;
            return false;
        });
    }

    @Override
    public int getMutex()
    {
        return this.mutex;
    }

    protected PlayerEntity getNearestPlayer(final Entity source, final float distance)
    {
        return source.getEntityWorld().getClosestPlayer(source, distance);
    }

    @Override
    public int getPriority()
    {
        return this.priority;
    }

    protected void setCombatState(final IPokemob pokemob, final CombatStates state, final boolean value)
    {
        pokemob.setCombatState(state, value);
    }

    protected void setGeneralState(final IPokemob pokemob, final GeneralStates state, final boolean value)
    {
        pokemob.setGeneralState(state, value);
    }

    protected void setLogicState(final IPokemob pokemob, final LogicStates state, final boolean value)
    {
        pokemob.setLogicState(state, value);
    }

    @Override
    public IAIRunnable setMutex(final int mutex)
    {
        this.mutex = mutex;
        return this;
    }

    @Override
    public IAIRunnable setPriority(final int prior)
    {
        this.priority = prior;
        return this;
    }

    @Override
    public void tick()
    {
    }

    @Override
    public Map<MemoryModuleType<?>, MemoryModuleStatus> getNeededMemories()
    {
        return this.neededMems;
    }

    @Override
    protected boolean shouldExecute(final ServerWorld worldIn, final E owner)
    {
        return this.shouldRun();
    }

    @Override
    protected void resetTask(final ServerWorld worldIn, final E entityIn, final long gameTimeIn)
    {
        this.reset();
    }

    @Override
    protected boolean shouldContinueExecuting(final ServerWorld worldIn, final E entityIn, final long gameTimeIn)
    {
        return this.shouldRun();
    }

    @Override
    protected void updateTask(final ServerWorld worldIn, final E owner, final long gameTime)
    {
        this.run();
        this.tick();
        this.finish();
    }

    protected boolean canTimeOut()
    {
        return false;
    }

    @Override
    protected boolean isTimedOut(final long gameTime)
    {
        if (!this.canTimeOut()) return false;
        return super.isTimedOut(gameTime);
    }
}
