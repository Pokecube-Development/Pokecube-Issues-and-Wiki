package pokecube.core.ai.tasks.idle;

import com.google.common.collect.Maps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
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

import java.util.Map;
import java.util.Random;

/**
 * This IAIRunnable makes the mobs randomly wander around if they have nothing better to do.
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
        if (!TerrainManager.isAreaLoaded(mob.getEntity().level(), v, 8)) return null;

        // TODO also ensure no lava, etc
        if (v.isClearOfBlocks(world)) return v;
        return null;
    }

    private static final Map<MemoryModuleType<?>, MemoryStatus> _MEMS = Maps.newHashMap();

    private static Map<MemoryModuleType<?>, MemoryStatus> _getMems()
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

    private double x;
    private double y;
    private double z;

    Vector3 v = new Vector3();
    Vector3 v1 = new Vector3();

    public IdleWalkTask()
    {
        super(IdleWalkTask._getMems());
    }

    @Override
    protected boolean simpleRun()
    {
        return true;
    }

    /** Floating things try to stay their preferedHeight from the ground. */
    protected void doFloatingIdle(ServerLevel level, IPokemob pokemob)
    {
        this.v.set(this.x, this.y, this.z);
        final Vector3 temp = Vector3.getNextSurfacePoint(level, this.v, Vector3.secondAxisNeg, this.v.y);
        if (temp == null || !pokemob.isRoutineEnabled(AIRoutine.AIRBORNE)) return;
        this.y = temp.y + pokemob.getFloatHeight();
    }

    /**
     * Flying things will path to air, so long as not airborne, somethimes they will decide to path downwards, the
     * height they path to will be centered around players, to prevent them from all flying way up, or way down
     */
    protected void doFlyingIdle(ServerLevel level, IPokemob pokemob)
    {
        final boolean grounded = !pokemob.isRoutineEnabled(AIRoutine.AIRBORNE);
        final boolean tamed =
                pokemob.getGeneralState(GeneralStates.TAMED) && !pokemob.getGeneralState(GeneralStates.STAYING);
        final boolean up = Math.random() < 0.9;
        if (grounded && up && !tamed) pokemob.setRoutineState(AIRoutine.AIRBORNE, true);
        else if (!tamed) this.doGroundIdle(level, pokemob);
        final Player player = level.getNearestPlayer(pokemob.getEntity(), Config.Rules.despawnDistance(level));
        if (player != null)
        {
            final double diff = Math.abs(player.getY() - this.y);
            if (diff > 5) this.y = player.getY() + 5 * (1 - Math.random());
        }
    }

    /** Grounded things will path to surface points. */
    protected void doGroundIdle(ServerLevel level, IPokemob pokemob)
    {
        this.v.set(this.x, this.y, this.z);
        this.v.set(Vector3.getNextSurfacePoint(level, this.v, Vector3.secondAxisNeg,
                this.v.y - level.getMinBuildHeight()));
        if (this.v != null) this.y = this.v.y;
    }

    /** Stationary things will not idle path at all */
    protected void doStationaryIdle(ServerLevel level, IPokemob pokemob)
    {
        var entity = pokemob.getEntity();
        this.x = entity.getX();
        this.y = entity.getY();
        this.z = entity.getZ();
    }

    /** Water things will not idle path out of water. */
    protected void doWaterIdle(ServerLevel level, IPokemob pokemob)
    {
        this.v.set(this.x, this.y, this.z);
        if (!level.getFluidState(this.v.getPos()).is(FluidTags.WATER))
        {
            var entity = pokemob.getEntity();
            this.x = entity.getX();
            this.y = entity.getY();
            this.z = entity.getZ();
        }
    }

    protected boolean getLocation(ServerLevel level, IPokemob pokemob)
    {
        final boolean tameFactor =
                pokemob.getGeneralState(GeneralStates.TAMED) && !pokemob.getGeneralState(GeneralStates.STAYING);
        var entry = pokemob.getPokedexEntry();
        var entity = pokemob.getEntity();
        int distance = tameFactor ? PokecubeCore.getConfig().idleMaxPathTame : PokecubeCore.getConfig().idleMaxPathWild;
        boolean goHome = false;
        if (!tameFactor)
        {
            if (pokemob.getHome() == null)
            {
                this.v1.set(entity);
                pokemob.setHome(this.v1.intX(), this.v1.intY(), this.v1.intZ(), 16);
            }
            distance = (int) Math.min(distance, pokemob.getHomeDistance());
            this.v.set(pokemob.getHome());
            if (entity.blockPosition().distSqr(pokemob.getHome())
                    > pokemob.getHomeDistance() * pokemob.getHomeDistance() * 0.75) goHome = true;
        }
        else
        {
            LivingEntity setTo = entity;
            if (pokemob.getOwner() != null) setTo = pokemob.getOwner();
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

            final Vector3 v = IdleWalkTask.getRandomPointNear(level, pokemob, this.v, distance, minDy, maxDy);
            if (v == null) return false;
            double diff = Math.max(entry.length * pokemob.getEntity().getScale(),
                    entry.width * pokemob.getEntity().getScale());
            diff = Math.max(2, diff);
            if (this.v1.distToSq(v) < diff) return false;
            this.x = v.x;
            this.y = Math.round(v.y);
            this.z = v.z;
        }
        return true;
    }

    @Override
    public void reset(Mob entityIn)
    {}

    @Override
    protected void tick(final ServerLevel level, final Mob entity, final long gameTime)
    {
        var pokemob = PokemobCaps.getPokemobFor(entity);
        if (!this.getLocation(level, pokemob)) return;
        var entry = pokemob.getPokedexEntry();
        if (entry.flys()) this.doFlyingIdle(level, pokemob);
        else if (entry.floats()) this.doFloatingIdle(level, pokemob);
        else if (entry.swims() && entity.isInWater()) this.doWaterIdle(level, pokemob);
        else if (entry.isStationary) this.doStationaryIdle(level, pokemob);
        else this.doGroundIdle(level, pokemob);
        this.v1.set(entity);
        this.v.set(this.x, this.y, this.z);
        if (this.v1.distToSq(this.v) <= 1) return;
        this.setWalkTo(entity, this.v, 1, 3);
    }

    @Override
    public boolean shouldRun(Mob entity)
    {
        // Configs can set this to -1 to disable idle movement entirely.
        if (IdleWalkTask.IDLETIMER <= 0) return false;
        IPokemob pokemob = PokemobCaps.getPokemobFor(entity);

        // Not currently able to move.
        if (!TaskBase.canMove(pokemob)) return false;

        // Check a random number as well
        if (entity.getRandom().nextInt(IdleWalkTask.IDLETIMER) != 0) return false;

        // Wander disabled, so don't run.
        if (!pokemob.isRoutineEnabled(AIRoutine.WANDER)) return false;

        // Pokedex entry says it doesn't wander.
        if (pokemob.getPokedexEntry().isStationary) return false;

        // Angry at something
        if (pokemob.getCombatState(CombatStates.BATTLING)) return false;

        // Owner is controlling us.
        if (pokemob.getGeneralState(GeneralStates.CONTROLLED)) return false;
        return !entity.getBrain().hasMemoryValue(MemoryModules.WALK_TARGET);
    }

    @Override
    protected boolean canStillUse(final ServerLevel worldIn, final Mob entityIn, final long gameTimeIn)
    {
        return !entityIn.getBrain().hasMemoryValue(MemoryModules.WALK_TARGET);
    }
}