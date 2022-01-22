package pokecube.core.ai.tasks.misc;

import java.util.Map;
import java.util.UUID;

import com.google.common.collect.Maps;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.core.ai.tasks.TaskBase;
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
    private static final UUID              FOLLOW_SPEED_BOOST_ID = UUID.fromString(
            "662A6B8D-DA3E-4C1C-1234-96EA6097278D");
    private static final AttributeModifier FOLLOW_SPEED_BOOST    = new AttributeModifier(
            FollowOwnerTask.FOLLOW_SPEED_BOOST_ID, "following speed boost", 0.5F,
            AttributeModifier.Operation.MULTIPLY_TOTAL);

    private static final Map<MemoryModuleType<?>, MemoryStatus> mems = Maps.newHashMap();
    static
    {
        // Dont run if have a combat target
        FollowOwnerTask.mems.put(MemoryModules.ATTACKTARGET, MemoryStatus.VALUE_ABSENT);
        // Don't run if have a target location for moves
        FollowOwnerTask.mems.put(MemoryModules.MOVE_TARGET, MemoryStatus.VALUE_ABSENT);
    }

    public static double speedMult = 2;

    private LivingEntity theOwner;

    private PathNavigation petPathfinder;

    private final double speed;
    private boolean      pathing = false;

    float maxDist;
    float minDist;

    Vector3 ownerPos = new Vector3();
    Vector3 v        = new Vector3();
    Vector3 v1       = new Vector3();

    public FollowOwnerTask(final IPokemob entity, final float min, final float max)
    {
        super(entity, FollowOwnerTask.mems);
        this.minDist = min;
        this.maxDist = max;
        this.speed = 1;
        if (this.pokemob.getOwner() != null) this.ownerPos.set(this.pokemob.getOwner());
    }

    @Override
    public void reset()
    {
        this.ownerPos.set(this.theOwner);
        this.entity.setSprinting(false);

        final AttributeInstance iattributeinstance = this.entity.getAttribute(Attributes.MOVEMENT_SPEED);
        if (iattributeinstance.getModifier(FollowOwnerTask.FOLLOW_SPEED_BOOST_ID) != null) iattributeinstance
                .removeModifier(FollowOwnerTask.FOLLOW_SPEED_BOOST);

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
        if (BrainUtils.canSee(this.entity, this.theOwner)) BehaviorUtils.lookAtEntity(this.entity, this.theOwner);
        else if (!this.petPathfinder.isDone() && this.petPathfinder.getPath().getNextNodeIndex() < this.petPathfinder
                .getPath().getNodeCount() - 3)
        {
            double x, y, z;
            x = this.petPathfinder.getPath().getNode(this.petPathfinder.getPath().getNextNodeIndex()
                    + 1).x + 0.5;
            y = this.petPathfinder.getPath().getNode(this.petPathfinder.getPath().getNextNodeIndex()
                    + 1).y + 0.5;
            z = this.petPathfinder.getPath().getNode(this.petPathfinder.getPath().getNextNodeIndex()
                    + 1).z + 0.5;
            // Or look at path location
            BrainUtils.lookAt(this.entity, x, y, z);
        }
        final boolean hasTarget = this.entity.getBrain().hasMemoryValue(MemoryModuleType.WALK_TARGET);
        WalkTarget target = hasTarget ? this.entity.getBrain().getMemory(MemoryModuleType.WALK_TARGET).get() : null;
        if (target == null || target.getTarget().currentPosition().distanceToSqr(this.theOwner.position()) > 1)
            target = new WalkTarget(new EntityTracker(this.theOwner, false), (float) this.speed, 1);

        final boolean isSprinting = this.entity.isSprinting();
        final double ds2 = target.getTarget().currentPosition().distanceToSqr(this.entity.position());
        final boolean shouldSprint = isSprinting ? ds2 > 4 : ds2 > 9;
        if (shouldSprint && !isSprinting) this.entity.setSprinting(shouldSprint);
        final AttributeInstance iattributeinstance = this.entity.getAttribute(Attributes.MOVEMENT_SPEED);
        if (iattributeinstance.getModifier(FollowOwnerTask.FOLLOW_SPEED_BOOST_ID) != null) iattributeinstance
                .removeModifier(FollowOwnerTask.FOLLOW_SPEED_BOOST);
        if (this.entity.isSprinting()) iattributeinstance.addTransientModifier(FollowOwnerTask.FOLLOW_SPEED_BOOST);
        this.setWalkTo(target);
    }

    @Override
    public boolean shouldRun()
    {
        if (!this.pokemob.isRoutineEnabled(AIRoutine.FOLLOW)) return false;
        if (!TaskBase.canMove(this.pokemob)) return false;
        final LivingEntity LivingEntity = this.pokemob.getOwner();
        this.petPathfinder = this.entity.getNavigation();
        // Nothing to follow
        if (LivingEntity == null) return false;
        else if (this.pokemob.getGeneralState(GeneralStates.STAYING)) return false;
        else if (this.pathing && this.entity.distanceToSqr(LivingEntity) > this.maxDist * this.maxDist) return true;
        else if (this.entity.distanceToSqr(LivingEntity) < this.minDist * this.minDist) return false;
        else if (new Vector3().set(LivingEntity).distToSq(this.ownerPos) < this.minDist * this.minDist)
            return false;
        // Follow owner.
        else return true;
    }

}