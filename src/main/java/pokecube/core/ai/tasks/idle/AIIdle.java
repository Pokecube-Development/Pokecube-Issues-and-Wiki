package pokecube.core.ai.tasks.idle;

import java.util.Random;
import java.util.UUID;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.pathfinding.Path;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.IBlockReader;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.tasks.AIBase;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IMoveConstants.AIRoutine;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.interfaces.pokemob.ai.LogicStates;
import thut.api.maths.Vector3;
import thut.api.terrain.TerrainManager;

/**
 * This IAIRunnable makes the mobs randomly wander around if they have nothing
 * better to do.
 */
public class AIIdle extends AIBase
{
    public static int IDLETIMER = 1;

    public static Vector3 getRandomPointNear(final IBlockReader world, final IPokemob mob, final Vector3 v,
            final int distance)
    {
        final Random rand = new Random();

        // SElect random gaussians from here.
        double x = rand.nextGaussian() * distance;
        double z = rand.nextGaussian() * distance;

        // Cap x and z to distance.
        if (Math.abs(x) > distance) x = Math.signum(x) * distance;
        if (Math.abs(z) > distance) z = Math.signum(z) * distance;

        // Don't select distances too far up/down from current.
        final double y = Math.min(Math.max(1, rand.nextGaussian() * 4), 2);
        v.addTo(x, y, z);

        // Ensure the target location is loaded.
        if (!TerrainManager.isAreaLoaded(mob.getEntity().getEntityWorld(), v, 8)) return null;

        // TODO also ensure no lava, etc
        if (v.isClearOfBlocks(world)) return v;
        return null;
    }

    private AttributeModifier idlePathing = null;
    final PokedexEntry        entry;
    private double            x;
    private double            y;
    private double            z;
    private final double      speed;

    private int ticksSinceLastPathed = 0;

    private double maxLength = 16;
    Vector3        v         = Vector3.getNewVector();

    Vector3 v1 = Vector3.getNewVector();

    public AIIdle(final IPokemob pokemob)
    {
        super(pokemob);
        this.setMutex(2);
        this.entry = pokemob.getPokedexEntry();
        this.speed = this.entity.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getValue();
        this.idlePathing = new AttributeModifier(UUID.fromString("4454b0d8-75ef-4689-8fce-daab61a7e1b1"),
                "pokecube:idle_path", 0.5, Operation.MULTIPLY_BASE);
    }

    /** Floating things try to stay their preferedHeight from the ground. */
    private void doFloatingIdle()
    {
        this.v.set(this.x, this.y, this.z);
        final Vector3 temp = Vector3.getNextSurfacePoint(this.world, this.v, Vector3.secondAxisNeg, this.v.y);
        if (temp == null || !this.pokemob.isRoutineEnabled(AIRoutine.AIRBORNE)) return;
        this.y = temp.y + this.entry.preferedHeight;
    }

    /**
     * Flying things will path to air, so long as not airborne, somethimes they
     * will decide to path downwards, the height they path to will be centered
     * around players, to prevent them from all flying way up, or way down
     */
    private void doFlyingIdle()
    {
        final boolean grounded = !this.pokemob.isRoutineEnabled(AIRoutine.AIRBORNE);
        final boolean tamed = this.pokemob.getGeneralState(GeneralStates.TAMED) && !this.pokemob.getGeneralState(
                GeneralStates.STAYING);
        final boolean up = Math.random() < 0.9;
        if (grounded && up && !tamed) this.pokemob.setRoutineState(AIRoutine.AIRBORNE, true);
        else if (!tamed)
        {
            this.pokemob.setRoutineState(AIRoutine.AIRBORNE, false);
            this.v.set(this.x, this.y, this.z);
            this.v.set(Vector3.getNextSurfacePoint(this.world, this.v, Vector3.secondAxisNeg, this.v.y));
            if (this.v != null) this.y = this.v.y;
        }
        final PlayerEntity player = this.getNearestPlayer(this.entity, PokecubeCore.getConfig().aiDisableDistance);
        if (player != null)
        {
            final double diff = Math.abs(player.posY - this.y);
            if (diff > 5) this.y = player.posY + 5 * (1 - Math.random());
        }
    }

    /** Grounded things will path to surface points. */
    private void doGroundIdle()
    {
        this.v.set(this.x, this.y, this.z);
        this.v.set(Vector3.getNextSurfacePoint(this.world, this.v, Vector3.secondAxisNeg, this.v.y));
        if (this.v != null) this.y = this.v.y;
    }

    /** Stationary things will not idle path at all */
    public void doStationaryIdle()
    {
        this.x = this.entity.posX;
        this.y = this.entity.posY;
        this.z = this.entity.posZ;
    }

    /** Water things will not idle path out of water. */
    public void doWaterIdle()
    {
        this.v.set(this.x, this.y, this.z);
        if (this.world.getFluidState(this.v.getPos()).isTagged(FluidTags.WATER))
        {
            this.x = this.entity.posX;
            this.y = this.entity.posY;
            this.z = this.entity.posZ;
        }
    }

    private boolean getLocation()
    {
        final boolean tameFactor = this.pokemob.getGeneralState(GeneralStates.TAMED) && !this.pokemob.getGeneralState(
                GeneralStates.STAYING);
        int distance = tameFactor ? PokecubeCore.getConfig().idleMaxPathTame : PokecubeCore.getConfig().idleMaxPathWild;
        this.maxLength = distance + this.pokemob.getHomeDistance();
        boolean goHome = false;
        if (!tameFactor)
        {
            if (this.pokemob.getHome() == null || this.pokemob.getHome().getX() == 0 && this.pokemob.getHome()
                    .getY() == 0 & this.pokemob.getHome().getZ() == 0)
            {
                this.v1.set(this.entity);
                this.pokemob.setHome(this.v1.intX(), this.v1.intY(), this.v1.intZ(), 16);
            }
            distance = (int) Math.min(distance, this.pokemob.getHomeDistance());
            this.v.set(this.pokemob.getHome());
            if (this.entity.getPosition().distanceSq(this.pokemob.getHome()) > this.pokemob.getHomeDistance()
                    * this.pokemob.getHomeDistance()) goHome = true;
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
            final Vector3 v = AIIdle.getRandomPointNear(this.world, this.pokemob, this.v, distance);
            if (v == null) return false;
            double diff = Math.max(this.pokemob.getPokedexEntry().length * this.pokemob.getSize(), this.pokemob
                    .getPokedexEntry().width * this.pokemob.getSize());
            diff = Math.max(2, diff);
            if (this.v1.distToSq(v) < diff) return false;
            this.x = v.x;
            this.y = Math.round(v.y);
            this.z = v.z;
        }
        this.pokemob.setGeneralState(GeneralStates.IDLE, true);
        return true;
    }

    @Override
    public void reset()
    {
        this.pokemob.setGeneralState(GeneralStates.IDLE, false);
    }

    @Override
    public void run()
    {
        if (!this.pokemob.getGeneralState(GeneralStates.IDLE)) if (!this.getLocation()) return;
        if (this.pokemob.getPokedexEntry().flys()) this.doFlyingIdle();
        else if (this.pokemob.getPokedexEntry().floats()) this.doFloatingIdle();
        else if (this.entry.swims() && this.entity.isInWater()) this.doWaterIdle();
        else if (this.entry.isStationary) this.doStationaryIdle();
        else this.doGroundIdle();
        this.v1.set(this.entity);
        this.v.set(this.x, this.y, this.z);

        this.pokemob.setGeneralState(GeneralStates.IDLE, false);
        if (this.v1.distToSq(this.v) <= 1) return;

        this.entity.getAttribute(SharedMonsterAttributes.FOLLOW_RANGE).removeModifier(this.idlePathing);
        this.entity.getAttribute(SharedMonsterAttributes.FOLLOW_RANGE).applyModifier(this.idlePathing);
        Path path = this.entity.getNavigator().func_225466_a(this.x, this.y, this.z, 0);
        this.entity.getAttribute(SharedMonsterAttributes.FOLLOW_RANGE).removeModifier(this.idlePathing);
        if (path != null && path.getCurrentPathLength() > this.maxLength) path = null;
        this.addEntityPath(this.entity, path, this.speed);
    }

    @Override
    public boolean shouldRun()
    {
        // Configs can set this to -1 to disable idle movement entirely.
        if (AIIdle.IDLETIMER <= 0) return false;

        // Not currently able to move.
        if (!this.canMove()) return false;

        // Check a random number as well
        if (this.entity.getRNG().nextInt(AIIdle.IDLETIMER) != 0) return false;

        // Wander disabled, so don't run.
        if (!this.pokemob.isRoutineEnabled(AIRoutine.WANDER)) return false;

        // Pokedex entry says it doesn't wander.
        if (this.pokemob.getPokedexEntry().isStationary) return false;

        // Angry at something
        if (this.pokemob.getCombatState(CombatStates.ANGRY)) return false;

        // Trying to use a move.
        if (this.pokemob.getCombatState(CombatStates.EXECUTINGMOVE)) return false;

        // Pathing somewhere.
        if (this.pokemob.getLogicState(LogicStates.PATHING)) return false;

        // Owner is controlling us.
        if (this.pokemob.getGeneralState(GeneralStates.CONTROLLED)) return false;

        // Sitting
        if (this.pokemob.getLogicState(LogicStates.SITTING)) return false;

        Path current = this.entity.getNavigator().getPath();
        if (current != null && this.entity.getNavigator().noPath()) current = null;
        // Have path, no need to idle
        if (current != null) this.ticksSinceLastPathed = 0;

        return this.ticksSinceLastPathed++ > AIIdle.IDLETIMER;
    }

}