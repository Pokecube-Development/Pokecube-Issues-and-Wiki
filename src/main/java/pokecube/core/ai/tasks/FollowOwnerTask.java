package pokecube.core.ai.tasks;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.BrainUtil;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.memory.WalkTarget;
import net.minecraft.pathfinding.PathNavigator;
import net.minecraft.util.math.EntityPosWrapper;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.core.interfaces.IMoveConstants.AIRoutine;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import thut.api.maths.Vector3;

/**
 * This attempts to make the mob follow the owner around in the world. It
 * triggers if the owner gets too far away from the mob, and the mob is set to
 * follow.
 */
public class FollowOwnerTask extends TaskBase
{
    private static final Map<MemoryModuleType<?>, MemoryModuleStatus> mems = Maps.newHashMap();
    static
    {
        // Dont run if have a combat target
        FollowOwnerTask.mems.put(MemoryModules.ATTACKTARGET, MemoryModuleStatus.VALUE_ABSENT);
        // Don't run if have a target location for moves
        FollowOwnerTask.mems.put(MemoryModules.MOVE_TARGET, MemoryModuleStatus.VALUE_ABSENT);
    }

    public static double speedMult = 2;

    private LivingEntity theOwner;

    private PathNavigator petPathfinder;

    private final double speed;
    private boolean      pathing = false;

    float maxDist;
    float minDist;

    Vector3 ownerPos = Vector3.getNewVector();
    Vector3 v        = Vector3.getNewVector();
    Vector3 v1       = Vector3.getNewVector();

    public FollowOwnerTask(final IPokemob entity, final float min, final float max)
    {
        super(entity, FollowOwnerTask.mems);
        this.minDist = min;
        this.maxDist = max;
        this.speed = entity.getMovementSpeed();
        if (this.pokemob.getOwner() != null) this.ownerPos.set(this.pokemob.getOwner());
    }

    @Override
    public void reset()
    {
        this.ownerPos.set(this.theOwner);
        this.theOwner = null;
        this.pathing = false;
    }

    @Override
    public void run()
    {
        if (this.theOwner == null)
        {
            this.theOwner = this.pokemob.getOwner();
            this.ownerPos.set(this.theOwner);
            this.pathing = true;
        }
        // Look at owner.
        if (BrainUtil.canSee(this.entity.getBrain(), this.theOwner)) BrainUtil.lookAt(this.entity, this.theOwner);
        else if (!this.petPathfinder.noPath() && this.petPathfinder.getPath().getCurrentPathIndex() < this.petPathfinder
                .getPath().getCurrentPathLength() - 3)
        {
            double x, y, z;
            x = this.petPathfinder.getPath().getPathPointFromIndex(this.petPathfinder.getPath().getCurrentPathIndex()
                    + 1).x + 0.5;
            y = this.petPathfinder.getPath().getPathPointFromIndex(this.petPathfinder.getPath().getCurrentPathIndex()
                    + 1).y + 0.5;
            z = this.petPathfinder.getPath().getPathPointFromIndex(this.petPathfinder.getPath().getCurrentPathIndex()
                    + 1).z + 0.5;
            // Or look at path location
            BrainUtils.lookAt(this.entity, x, y, z);
        }
        final boolean hasTarget = this.entity.getBrain().hasMemory(MemoryModuleType.WALK_TARGET);
        WalkTarget target = hasTarget ? this.entity.getBrain().getMemory(MemoryModuleType.WALK_TARGET).get() : null;
        if (target == null || target.getTarget().getPos().squareDistanceTo(this.theOwner.getPositionVec()) > 1)
            target = new WalkTarget(new EntityPosWrapper(this.theOwner), (float) this.speed, 1);
        this.setWalkTo(target);
    }

    @Override
    public boolean shouldRun()
    {
        if (!this.pokemob.isRoutineEnabled(AIRoutine.FOLLOW)) return false;
        if (!TaskBase.canMove(this.pokemob)) return false;
        final LivingEntity LivingEntity = this.pokemob.getOwner();
        this.petPathfinder = this.entity.getNavigator();
        // Nothing to follow
        if (LivingEntity == null) return false;
        else if (this.pokemob.getGeneralState(GeneralStates.STAYING)) return false;
        else if (this.pathing && this.entity.getDistanceSq(LivingEntity) > this.maxDist * this.maxDist) return true;
        else if (this.entity.getDistanceSq(LivingEntity) < this.minDist * this.minDist) return false;
        else if (Vector3.getNewVector().set(LivingEntity).distToSq(this.ownerPos) < this.minDist * this.minDist)
            return false;
        // Follow owner.
        else return true;
    }

}