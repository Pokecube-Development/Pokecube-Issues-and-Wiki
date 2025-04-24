package pokecube.core.ai.tasks;

import com.google.common.collect.ImmutableMap;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.entity.pokemob.ai.LogicStates;
import pokecube.core.inventory.pokemob.PokemobInventory;
import thut.api.entity.ai.IAIRunnable;
import thut.api.entity.ai.ITask;
import thut.api.entity.ai.RootTask;
import thut.api.maths.Vector3;
import thut.lib.ItemStackTools;

import java.util.Map;

public abstract class TaskBase extends RootTask<Mob> implements ITask
{
    /** Thread safe inventory setting for pokemobs. */
    public static class InventoryChange implements IRunnable
    {
        public final int entity;
        public final int slot;
        public final int minSlot;
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
            final IPokemob pokemob = PokemobCaps.getPokemobFor(e);
            if (e == null || pokemob == null) return false;
            if (this.slot > 0) pokemob.getInventory().setItem(this.slot, this.stack);
            else if (!ItemStackTools.addItemStackToInventory(this.stack, pokemob.getInventory(), this.minSlot,
                    PokemobInventory.MAIN_INVENTORY_SIZE)) e.spawnAtLocation(this.stack, 0);
            return true;
        }

    }

    /** Thread safe sound playing. */
    public static class PlaySound implements IRunnable
    {
        final ResourceKey<Level> dim;
        final Vector3 loc;
        final SoundEvent sound;
        final SoundSource cat;
        final float volume;
        final float pitch;

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
        var entity = pokemob.getEntity();
        // Don't allow motion if the mob is actually a passenger, this should
        // help for say gengars riding dragons...
        if (entity.isPassenger()) return false;
        var pose = entity.getPose();
        // Pose check is cheap, so use it
        if (pose == Pose.DYING || entity.getPose() == Pose.SLEEPING || pose == Pose.SITTING) return false;
        // Can't move at all in this case
        if (pokemob.getLogicState(LogicStates.CANNOTMOVE)) return false;
        // Don't move while sitting
        if (pokemob.getLogicState(LogicStates.SITTING)) return false;
        // DOLATER add other checks for things like bind, etc
        return entity.getAttribute(Attributes.MOVEMENT_SPEED).getValue() > 0;
    }

    int priority = 0;

    public TaskBase()
    {
        this(ImmutableMap.of());
    }

    public TaskBase(final Map<MemoryModuleType<?>, MemoryStatus> neededMems)
    {
        super(neededMems);
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
    protected boolean checkExtraStartConditions(final ServerLevel level, final Mob owner)
    {
        if (this.isPaused(level, owner)) return this.tempRun;
        return this.tempRun = this.shouldRun(owner);
    }

    @Override
    protected void stop(final ServerLevel level, final Mob entityIn, final long gameTimeIn)
    {
        // Incase this is called when paused, we don't want to accept it, so
        // return early.
        if (this.isPaused(level, entityIn)) return;
        this.reset(entityIn);
    }

    @Override
    protected boolean canStillUse(final ServerLevel level, final Mob entityIn, final long gameTimeIn)
    {
        if (this.isPaused(level, entityIn)) return this.tempCont;
        return this.tempCont = this.shouldRun(entityIn);
    }
}
