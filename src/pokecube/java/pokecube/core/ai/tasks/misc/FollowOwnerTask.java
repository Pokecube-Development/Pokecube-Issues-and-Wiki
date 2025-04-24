package pokecube.core.ai.tasks.misc;

import com.google.common.collect.Maps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.entity.pokemob.ai.AIRoutine;
import pokecube.api.entity.pokemob.ai.GeneralStates;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.core.ai.tasks.TaskBase;
import thut.api.maths.Vector3;

import java.util.Map;

/**
 * This attempts to make the mob follow the owner around in the world. It triggers if the owner gets too far away from
 * the mob, and the mob is set to follow.
 */
public class FollowOwnerTask extends TaskBase
{
    public static final ResourceLocation FOLLOW_SPEED_BOOST_ID = ResourceLocation.fromNamespaceAndPath("pokecube",
            "following_speed_boost");
    public static final AttributeModifier FOLLOW_SPEED_BOOST = new AttributeModifier(
            FollowOwnerTask.FOLLOW_SPEED_BOOST_ID, 0.5F, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);

    private static final Map<MemoryModuleType<?>, MemoryStatus> mems = Maps.newHashMap();

    static
    {
        // Dont run if have a combat target
        FollowOwnerTask.mems.put(MemoryModules.ATTACKTARGET.get(), MemoryStatus.VALUE_ABSENT);
        // Don't run if have a target location for moves
        FollowOwnerTask.mems.put(MemoryModules.MOVE_TARGET.get(), MemoryStatus.VALUE_ABSENT);
    }

    private LivingEntity theOwner;

    private PathNavigation petPathfinder;

    private final double speed;

    float maxDist;
    float minDist;

    Vector3 ownerPos = new Vector3();

    public FollowOwnerTask(final float min, final float max)
    {
        super(FollowOwnerTask.mems);
        this.minDist = min;
        this.maxDist = max;
        this.speed = 1;
    }

    @Override
    public void reset(Mob entity)
    {
        this.ownerPos.set(this.theOwner);
        entity.setSprinting(false);

        final AttributeInstance iattributeinstance = entity.getAttribute(Attributes.MOVEMENT_SPEED);
        iattributeinstance.removeModifier(FollowOwnerTask.FOLLOW_SPEED_BOOST);

        this.theOwner = null;
    }

    @Override
    protected void tick(final ServerLevel level, final Mob entity, final long gameTime)
    {
        var pokemob = PokemobCaps.getPokemobFor(entity);
        if (this.theOwner == null)
        {
            this.theOwner = pokemob.getOwner();
            this.ownerPos.set(this.theOwner);
        }
        // Look at owner.
        if (BrainUtils.canSee(entity, this.theOwner)) BehaviorUtils.lookAtEntity(entity, this.theOwner);
        else if (!this.petPathfinder.isDone()
                && this.petPathfinder.getPath().getNextNodeIndex() < this.petPathfinder.getPath().getNodeCount() - 3)
        {
            double x, y, z;
            var node = this.petPathfinder.getPath().getNode(this.petPathfinder.getPath().getNextNodeIndex() + 1);
            x = node.x + 0.5;
            y = node.y + 0.5;
            z = node.z + 0.5;
            // Or look at path location
            BrainUtils.lookAt(entity, x, y, z);
        }
        boolean hasTarget = entity.getBrain().hasMemoryValue(MemoryModuleType.WALK_TARGET);
        WalkTarget target = hasTarget ? entity.getBrain().getMemory(MemoryModuleType.WALK_TARGET).get() : null;
        if (target == null || target.getTarget().currentPosition().distanceToSqr(this.theOwner.position()) > 1)
            target = new WalkTarget(new EntityTracker(this.theOwner, false), (float) this.speed, 1);

        boolean isSprinting = entity.isSprinting();
        double ds2 = target.getTarget().currentPosition().distanceToSqr(entity.position());
        boolean shouldSprint = isSprinting ? ds2 > 9 : ds2 > 64;
        if (shouldSprint != isSprinting) entity.setSprinting(shouldSprint);

        AttributeInstance walkSpeed = entity.getAttribute(Attributes.MOVEMENT_SPEED);
        walkSpeed.removeModifier(FollowOwnerTask.FOLLOW_SPEED_BOOST);
        if (entity.isSprinting())
        {
            walkSpeed.addTransientModifier(FollowOwnerTask.FOLLOW_SPEED_BOOST);
        }
        if (entity.getAttributes().hasAttribute(Attributes.FLYING_SPEED))
        {
            AttributeInstance flySpeed = entity.getAttribute(Attributes.FLYING_SPEED);
            flySpeed.removeModifier(FollowOwnerTask.FOLLOW_SPEED_BOOST);
            if (entity.isSprinting())
            {
                flySpeed.addTransientModifier(FollowOwnerTask.FOLLOW_SPEED_BOOST);
            }
        }
        this.setWalkTo(entity, target);
    }

    @Override
    protected boolean shouldNotRun(Mob mobIn)
    {
        var pokemob = PokemobCaps.getPokemobFor(mobIn);
        return pokemob.getOwner() == null;
    }

    @Override
    public boolean shouldRun(Mob entity)
    {
        var pokemob = PokemobCaps.getPokemobFor(entity);
        // In a battle, so no follow, do battle
        if (pokemob.inCombat()) return false;
        // if not allowed to follow, skip
        if (!pokemob.isRoutineEnabled(AIRoutine.FOLLOW)) return false;
        // if unable to move, skip
        if (!TaskBase.canMove(pokemob)) return false;
        // if set to stay, skip
        if (pokemob.getGeneralState(GeneralStates.STAYING)) return false;
        final LivingEntity owner = pokemob.getOwner();
        // Nothing to follow
        if (owner == null) return false;
        double dr2 = entity.distanceToSqr(owner);
        // close enough, so skip
        if (dr2 < this.minDist * this.minDist) return false;
        this.petPathfinder = entity.getNavigation();
        // Follow owner.
        return true;
    }

}