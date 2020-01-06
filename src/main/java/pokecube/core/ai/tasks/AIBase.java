package pokecube.core.ai.tasks;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.Path;
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
import thut.api.maths.Vector3;
import thut.lib.ItemStackTools;

public abstract class AIBase implements IAIRunnable
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

    protected final IPokemob    pokemob;
    protected final MobEntity   entity;
    protected final ServerWorld world;

    protected List<IRunnable> toRun = Lists.newArrayList();

    int priority = 0;
    int mutex    = 0;

    public AIBase(final IPokemob pokemob)
    {
        this.pokemob = pokemob;
        this.entity = pokemob.getEntity();
        if (this.entity.getEntityWorld() instanceof ServerWorld) this.world = (ServerWorld) this.entity
                .getEntityWorld();
        else this.world = null;
    }

    protected boolean addEntityPath(final MobEntity entity, final Path path, final double speed)
    {
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
        return this.getEntitiesWithinDistance(source.getEntityWorld(), source.getPosition(), distance, clazz,
                targetClass);
    }

    protected <T extends Entity> List<T> getEntitiesWithinDistance(final World world, final BlockPos pos,
            final float distance, final Class<T> clazz, final Class<?>... targetClass)
    {
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

    protected void setAttackTarget(final MobEntity attacker, final LivingEntity target)
    {
        attacker.setAttackTarget(target);
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

}
