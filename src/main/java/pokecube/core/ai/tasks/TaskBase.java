package pokecube.core.ai.tasks;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.ai.LogicStates;
import thut.api.entity.ai.IAIRunnable;
import thut.api.entity.ai.ITask;
import thut.api.entity.ai.RootTask;
import thut.api.maths.Vector3;
import thut.lib.ItemStackTools;

public abstract class TaskBase extends RootTask<Mob> implements ITask
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
            this.entity = entity.getId();
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
        public boolean run(final Level world)
        {
            final Entity e = world.getEntity(this.entity);
            final IPokemob pokemob = CapabilityPokemob.getPokemobFor(e);
            if (e == null || pokemob == null) return false;
            if (this.slot > 0) pokemob.getInventory().setItem(this.slot, this.stack);
            else if (!ItemStackTools.addItemStackToInventory(this.stack, pokemob.getInventory(), this.minSlot)) e
                    .spawnAtLocation(this.stack, 0);
            return true;
        }

    }

    /** Thread safe sound playing. */
    public static class PlaySound implements IRunnable
    {
        final ResourceKey<Level> dim;
        final Vector3            loc;
        final SoundEvent         sound;
        final SoundSource      cat;
        final float              volume;
        final float              pitch;

        public PlaySound(final ResourceKey<Level> registryKey, final Vector3 loc, final SoundEvent sound,
                final SoundSource cat, final float volume, final float pitch)
        {
            this.dim = registryKey;
            this.sound = sound;
            this.volume = volume;
            this.loc = loc;
            this.pitch = pitch;
            this.cat = cat;
        }

        @Override
        public boolean run(final Level world)
        {
            if (this.dim != world.dimension()) return false;
            world.playSound(null, this.loc.x, this.loc.y, this.loc.z, this.sound, this.cat, this.volume, this.pitch);
            return true;
        }

    }

    public static boolean canMove(final IPokemob pokemob)
    {
        // Don't allow motion if the mob is actually a passenger, this should
        // help for say gengars riding dragons...
        if (pokemob.getEntity().isPassenger()) return false;
        final boolean sitting = pokemob.getLogicState(LogicStates.SITTING);
        final boolean sleeping = pokemob.getLogicState(LogicStates.SLEEPING) || (pokemob.getStatus()
                & IMoveConstants.STATUS_SLP) > 0;
        final boolean frozen = (pokemob.getStatus() & IMoveConstants.STATUS_FRZ) > 0;
        // DOLATER add other checks for things like bind, etc
        return !(sitting || sleeping || frozen);
    }

    protected final IPokemob pokemob;

    protected final ServerLevel world;

    int priority = 0;

    boolean tempRun  = false;
    boolean tempCont = false;

    public TaskBase(final IPokemob pokemob)
    {
        this(pokemob, ImmutableMap.of());
    }

    public TaskBase(final IPokemob pokemob, final Map<MemoryModuleType<?>, MemoryStatus> neededMems)
    {
        super(pokemob.getEntity(), neededMems);
        this.pokemob = pokemob;
        if (this.entity.getLevel() instanceof ServerLevel) this.world = (ServerLevel) this.entity
                .getLevel();
        else this.world = null;
    }

    @Override
    protected void setWalkTo(final WalkTarget target)
    {
        this.pokemob.setLogicState(LogicStates.SITTING, false);
        super.setWalkTo(target);
    }

    @Override
    public void finish()
    {

    }

    @Override
    public int getPriority()
    {
        return this.priority;
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
    protected boolean checkExtraStartConditions(final ServerLevel worldIn, final Mob owner)
    {
        if (this.isPaused(owner)) return this.tempRun;
        return this.tempRun = this.shouldRun();
    }

    @Override
    protected void stop(final ServerLevel worldIn, final Mob entityIn, final long gameTimeIn)
    {
        // Incase this is called when paused, we don't want to accept it, so
        // return early.
        if (this.isPaused(entityIn)) return;
        this.reset();
    }

    @Override
    protected boolean canStillUse(final ServerLevel worldIn, final Mob entityIn,
            final long gameTimeIn)
    {
        if (this.isPaused(entityIn)) return this.tempCont;
        return this.tempCont = this.shouldRun();
    }

    @Override
    protected void tick(final ServerLevel worldIn, final Mob owner, final long gameTime)
    {
        if (this.isPaused(owner)) return;
        this.run();
        this.tick();
        this.finish();
    }
}
