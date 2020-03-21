package pokecube.core.ai.tasks;

import net.minecraft.entity.LivingEntity;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNavigator;
import net.minecraft.util.math.Vec3d;
import pokecube.core.interfaces.IMoveConstants.AIRoutine;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.interfaces.pokemob.ai.LogicStates;
import thut.api.maths.Vector3;

/**
 * This attempts to make the mob follow the owner around in the world. It
 * triggers if the owner gets too far away from the mob, and the mob is set to
 * follow.
 */
public class AIFollowOwner extends AIBase
{
    public static double speedMult = 2;

    private LivingEntity theOwner;

    private double        speed;
    private PathNavigator petPathfinder;
    private int           cooldown;
    private boolean       pathing  = false;
    float                 maxDist;
    float                 minDist;
    Vector3               ownerPos = Vector3.getNewVector();
    Vector3               v        = Vector3.getNewVector();
    Vector3               v1       = Vector3.getNewVector();

    public AIFollowOwner(final IPokemob entity, final float min, final float max)
    {
        super(entity);
        this.minDist = min;
        this.maxDist = max;
        this.speed = 1;
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
            this.cooldown = 0;
            this.ownerPos.set(this.theOwner);
            this.pathing = true;
        }
        // Look at owner.
        if (Vector3.isVisibleEntityFromEntity(this.entity, this.theOwner)) this.entity.getLookController()
                .setLookPositionWithEntity(this.theOwner, 10.0F, this.entity.getVerticalFaceSpeed());
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
            this.entity.getLookController().setLookPosition(x, y, z, 10, this.entity.getVerticalFaceSpeed());
        }
        // Only path every couple ticks, or when owner has moved.
        if (--this.cooldown <= 0)
        {
            this.cooldown = 2;
            final double dl = this.v.set(this.theOwner).distToSq(this.ownerPos);
            if (dl < 1) return;
            this.ownerPos.set(this.theOwner);
            final Vec3d v = this.theOwner.getMotion();
            this.speed = Math.sqrt(v.x * v.x + v.z * v.z);
            this.speed = Math.max(0.6, this.speed);
            this.speed *= AIFollowOwner.speedMult;
            final Path path = this.petPathfinder.getPathToEntityLiving(this.theOwner, 0);
            if (path != null) this.addEntityPath(this.entity, path, this.speed);
        }
    }

    @Override
    public boolean shouldRun()
    {
        if (!this.pokemob.isRoutineEnabled(AIRoutine.FOLLOW)) return false;
        final LivingEntity LivingEntity = this.pokemob.getOwner();
        this.petPathfinder = this.entity.getNavigator();
        // Nothing to follow
        if (LivingEntity == null) return false;
        else if (this.pokemob.getLogicState(LogicStates.SITTING)) return false;
        else if (this.pokemob.getGeneralState(GeneralStates.STAYING)) return false;
        else if (this.pathing && this.entity.getDistanceSq(LivingEntity) > this.maxDist * this.maxDist) return true;
        else if (this.entity.getAttackTarget() != null || this.pokemob.getCombatState(CombatStates.EXECUTINGMOVE))
            return false;
        else if (this.entity.getDistanceSq(LivingEntity) < this.minDist * this.minDist) return false;
        else if (Vector3.getNewVector().set(LivingEntity).distToSq(this.ownerPos) < this.minDist * this.minDist)
            return false;
        else if (!this.petPathfinder.noPath())
        {
            final Vector3 p = this.v1.set(this.petPathfinder.getPath().getFinalPathPoint());
            this.v.set(LivingEntity);
            if (p.distToSq(this.v) <= 2) return false;
            return true;
        }
        // Follow owner.
        else return true;
    }

}