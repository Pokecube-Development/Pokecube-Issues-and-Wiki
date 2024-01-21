package pokecube.core.ai.tasks.idle;

import java.util.Map;
import java.util.Random;

import com.google.common.collect.Maps;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import pokecube.api.data.PokedexEntry;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.ai.AIRoutine;
import pokecube.api.entity.pokemob.ai.CombatStates;
import pokecube.api.entity.pokemob.ai.GeneralStates;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.core.ai.tasks.TaskBase;
import pokecube.core.init.Config;
import thut.api.level.terrain.TerrainManager;
import thut.api.maths.Vector3;
import thut.core.common.ThutCore;

/**
 * This IAIRunnable makes the mobs randomly wander around if they have nothing
 * better to do.
 */
public class IdleWalkTask extends BaseIdleTask
{
    public static int IDLETIMER = 1;

    public static Vector3 getRandomPointNear(final BlockGetter world, final IPokemob mob, final Vector3 v,
            final int distance, double minDy, double maxDy)
    {
        final Random rand = ThutCore.newRandom();

        // SElect random gaussians from here.
        double x = rand.nextFloat() * distance;
        double z = rand.nextFloat() * distance;

        // Cap x and z to distance.
        if (Math.abs(x) > distance) x = Math.signum(x) * distance;
        if (Math.abs(z) > distance) z = Math.signum(z) * distance;

        // Don't select distances too far up/down from current.
        final double y = Math.min(Math.max(minDy, rand.nextGaussian() * 4), maxDy);
        v.addTo(x, y, z);

        // Ensure the target location is loaded.
        if (!TerrainManager.isAreaLoaded(mob.getEntity().getLevel(), v, 8)) return null;

        // TODO also ensure no lava, etc
        if (v.isClearOfBlocks(world)) return v;
        return null;
    }

    private static final Map<MemoryModuleType<?>, MemoryStatus> _MEMS = Maps.newHashMap();

    private static final Map<MemoryModuleType<?>, MemoryStatus> _getMems()
    {
        if (_MEMS.isEmpty())
        {
            // Dont run if have a walk target
            _MEMS.put(MemoryModules.WALK_TARGET, MemoryStatus.VALUE_ABSENT);
            // Don't run if have a target location for moves
            _MEMS.put(MemoryModules.MOVE_TARGET.get(), MemoryStatus.VALUE_ABSENT);
            // Don't run if we have a path
            _MEMS.put(MemoryModules.PATH, MemoryStatus.VALUE_ABSENT);
            // Don't run if guarding an egg
            _MEMS.put(MemoryModules.EGG.get(), MemoryStatus.VALUE_ABSENT);
        }
        return _MEMS;
    }

    PokedexEntry entry;

    private double x;
    private double y;
    private double z;

    Vector3 v = new Vector3();
    Vector3 v1 = new Vector3();

    public IdleWalkTask(final IPokemob pokemob)
    {
        super(pokemob, IdleWalkTask._getMems());
        this.entry = pokemob.getPokedexEntry();
    }

    @Override
    protected boolean simpleRun()
    {
        return true;
    }

    /** Floating things try to stay their preferedHeight from the ground. */
    protected void doFloatingIdle()
    {
        this.v.set(this.x, this.y, this.z);
        final Vector3 temp = Vector3.getNextSurfacePoint(this.world, this.v, Vector3.secondAxisNeg, this.v.y);
        if (temp == null || !this.pokemob.isRoutineEnabled(AIRoutine.AIRBORNE)) return;
        this.y = temp.y + this.pokemob.getFloatHeight();
    }

    /**
     * Flying things will path to air, so long as not airborne, somethimes they
     * will decide to path downwards, the height they path to will be centered
     * around players, to prevent them from all flying way up, or way down
     */
    protected void doFlyingIdle()
    {
        final boolean grounded = !this.pokemob.isRoutineEnabled(AIRoutine.AIRBORNE);
        final boolean tamed = this.pokemob.getGeneralState(GeneralStates.TAMED)
                && !this.pokemob.getGeneralState(GeneralStates.STAYING);
        final boolean up = Math.random() < 0.9;
        if (grounded && up && !tamed) this.pokemob.setRoutineState(AIRoutine.AIRBORNE, true);
        else if (!tamed) this.doGroundIdle();
        final Player player = this.world.getNearestPlayer(this.entity, Config.Rules.despawnDistance(world));
        if (player != null)
        {
            final double diff = Math.abs(player.getY() - this.y);
            if (diff > 5) this.y = player.getY() + 5 * (1 - Math.random());
        }
    }

    /** Grounded things will path to surface points. */
    protected void doGroundIdle()
    {
        this.v.set(this.x, this.y, this.z);
        this.v.set(Vector3.getNextSurfacePoint(this.world, this.v, Vector3.secondAxisNeg,
                this.v.y - entity.level.getMinBuildHeight()));
        if (this.v != null) this.y = this.v.y;
    }

    /** Stationary things will not idle path at all */
    protected void doStationaryIdle()
    {
        this.x = this.entity.getX();
        this.y = this.entity.getY();
        this.z = this.entity.getZ();
    }

    /** Water things will not idle path out of water. */
    protected void doWaterIdle()
    {
        this.v.set(this.x, this.y, this.z);
        if (!this.world.getFluidState(this.v.getPos()).is(FluidTags.WATER))
        {
            this.x = this.entity.getX();
            this.y = this.entity.getY();
            this.z = this.entity.getZ();
        }
    }

    protected boolean getLocation()
    {
        final boolean tameFactor = this.pokemob.getGeneralState(GeneralStates.TAMED)
                && !this.pokemob.getGeneralState(GeneralStates.STAYING);
        if (this.entry != pokemob.getPokedexEntry()) this.entry = pokemob.getPokedexEntry();
        int distance = tameFactor ? PokecubeCore.getConfig().idleMaxPathTame : PokecubeCore.getConfig().idleMaxPathWild;
        boolean goHome = false;
        if (!tameFactor)
        {
            if (this.pokemob.getHome() == null)
            {
                this.v1.set(this.entity);
                this.pokemob.setHome(this.v1.intX(), this.v1.intY(), this.v1.intZ(), 16);
            }
            distance = (int) Math.min(distance, this.pokemob.getHomeDistance());
            this.v.set(this.pokemob.getHome());
            if (this.entity.blockPosition().distSqr(this.pokemob.getHome()) > this.pokemob.getHomeDistance()
                    * this.pokemob.getHomeDistance() * 0.75)
                goHome = true;
        }
        else
        {
            LivingEntity setTo = this.entity;
            if (this.pokemob.getOwner() != null) setTo = this.pokemob.getOwner();
            this.v.set(setTo);
        }
        if (goHome)
        {
            this.x = this.v.x;
            this.y = Math.round(this.v.y);
            this.z = this.v.z;
        }
        else
        {
            double minDy = 0;
            double maxDy = 2;

            boolean verticalMotion = entry.flys() || entry.floats() || (entry.swims() && entity.isInWater());
            if (verticalMotion) minDy = -2;

            final Vector3 v = IdleWalkTask.getRandomPointNear(this.world, this.pokemob, this.v, distance, minDy, maxDy);
            if (v == null) return false;
            double diff = Math.max(this.entry.length * this.pokemob.getSize(),
                    this.entry.width * this.pokemob.getSize());
            diff = Math.max(2, diff);
            if (this.v1.distToSq(v) < diff) return false;
            this.x = v.x;
            this.y = Math.round(v.y);
            this.z = v.z;
        }
        return true;
    }

    @Override
    public void reset()
    {}

    @Override
    public void run()
    {
        if (!this.getLocation()) return;
        if (this.entry.flys()) this.doFlyingIdle();
        else if (this.entry.floats()) this.doFloatingIdle();
        else if (this.entry.swims() && this.entity.isInWater()) this.doWaterIdle();
        else if (this.entry.isStationary) this.doStationaryIdle();
        else this.doGroundIdle();
        this.v1.set(this.entity);
        this.v.set(this.x, this.y, this.z);
        if (this.v1.distToSq(this.v) <= 1) return;
        this.setWalkTo(this.v, 1, 3);
    }

    @Override
    protected void start(final ServerLevel worldIn, final Mob entityIn, final long gameTimeIn)
    {
        this.run();
    }

    @Override
    public boolean shouldRun()
    {
        // Configs can set this to -1 to disable idle movement entirely.
        if (IdleWalkTask.IDLETIMER <= 0) return false;

        // Not currently able to move.
        if (!TaskBase.canMove(this.pokemob)) return false;

        // Check a random number as well
        if (this.entity.getRandom().nextInt(IdleWalkTask.IDLETIMER) != 0) return false;

        // Wander disabled, so don't run.
        if (!this.pokemob.isRoutineEnabled(AIRoutine.WANDER)) return false;

        // Pokedex entry says it doesn't wander.
        if (this.pokemob.getPokedexEntry().isStationary) return false;

        // Angry at something
        if (this.pokemob.getCombatState(CombatStates.BATTLING)) return false;

        // Owner is controlling us.
        if (this.pokemob.getGeneralState(GeneralStates.CONTROLLED)) return false;
        if (this.entity.getBrain().hasMemoryValue(MemoryModules.WALK_TARGET)) return false;
        return true;
    }

    @Override
    protected boolean canStillUse(final ServerLevel worldIn, final Mob entityIn, final long gameTimeIn)
    {
        return !this.entity.getBrain().hasMemoryValue(MemoryModules.WALK_TARGET);
    }
}