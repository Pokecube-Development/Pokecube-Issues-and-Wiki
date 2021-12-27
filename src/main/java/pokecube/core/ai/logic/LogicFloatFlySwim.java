package pokecube.core.ai.logic;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.pathing.ClimbPathNavi;
import pokecube.core.ai.pathing.FlyPathNavi;
import pokecube.core.ai.pathing.SwimPathNavi;
import pokecube.core.ai.pathing.WalkPathNavi;
import pokecube.core.interfaces.IMoveConstants.AIRoutine;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import thut.api.maths.Vector3;

/**
 * This is used instead of a Swimming AI task. It manages making mobs "jump" to
 * swim while in water. It also manages making floating mobs float a certain
 * distance above the ground, and manages terminating wandering paths for
 * floating, flying and swimming mobs if they get sufficiently close to their
 * destinations.
 */
public class LogicFloatFlySwim extends LogicBase
{
    private static class WalkController extends MoveControl
    {

        final IPokemob pokemob;

        public WalkController(final IPokemob mob)
        {
            super(mob.getEntity());
            this.pokemob = mob;
        }

        @Override
        public double getSpeedModifier()
        {
            return super.getSpeedModifier();
        }

        @Override
        public void tick()
        {
            if (pokemob.getController().blocksPathing()) return;
            super.tick();
        }

    }

    private static class SwimController extends MoveControl
    {
        final IPokemob pokemob;

        public SwimController(final IPokemob mob)
        {
            super(mob.getEntity());
            this.pokemob = mob;
        }

        @Override
        public double getSpeedModifier()
        {
            return super.getSpeedModifier() * PokecubeCore.getConfig().swimPathingSpeedFactor;
        }

        @Override
        public void tick()
        {
            this.mob.setNoGravity(this.mob.isInWater());
            
            if (pokemob.getController().blocksPathing()) return;

            if (this.operation == MoveControl.Operation.MOVE_TO && !this.mob.getNavigation().isDone())
            {
                this.operation = MoveControl.Operation.WAIT;

                final double dx = this.wantedX - this.mob.getX();
                final double dy = this.wantedY - this.mob.getY();
                final double dz = this.wantedZ - this.mob.getZ();

                // Total distance squared
                final double ds2 = dx * dx + dy * dy + dz * dz;
                if (ds2 < 0.01F)
                {
                    this.mob.setYya(0.0F);
                    this.mob.setZza(0.0F);
                    return;
                }
                // Horizontal distance
                final float dh = Mth.sqrt((float) (dx * dx + dz * dz));
                final float ds = Mth.sqrt((float) ds2);

                final float f = (float) (Mth.atan2(dz, dx) * (180F / (float) Math.PI)) - 90.0F;
                this.mob.yRot = this.rotlerp(this.mob.yRot, f, 10.0F);

                float angleDiff = this.mob.yRot - f;
                angleDiff /= 180F / (float) Math.PI;

                final float dot = Mth.cos(angleDiff);
                float f1 = (float) (this.getSpeedModifier() * this.pokemob.getMovementSpeed());

                this.mob.setSpeed(f1 * dot);
                this.mob.flyingSpeed = (float) (f1 * 0.05);
                final float f2 = (float) -(Mth.atan2(dy, dh) * (180F / (float) Math.PI));
                this.mob.xRot = this.rotlerp(this.mob.xRot, f2, 10.0F);
                f1 *= Math.abs(dy / ds);

                // Speeds up upwards motion if this is too slow.
                if (dy < 2 && dy > 0) f1 = Math.max(f1 * 10, 0.1f);

                this.mob.setYya(dy > 0.0D ? f1 : -f1);

                // dampen the velocity so they don't orbit their destination
                // points.
                final float dh_hat = Mth.abs(dh / ds);
                final float dy_hat = (float) Math.abs(dy / ds);
                final Vec3 v = this.mob.getDeltaMovement();
                this.mob.setDeltaMovement(v.x * dh_hat * dot, v.y * dy_hat * dot, v.z * dh_hat * dot);
            }
            else this.mob.setSpeed(0.0F);
        }

    }

    private static class FlyController extends FlyingMoveControl
    {
        final IPokemob pokemob;

        public FlyController(final IPokemob mob)
        {
            // The true here is only used for the default behaviour to flag it
            // as not-re-enable gravity when not moving somewhere.
            super(mob.getEntity(), 20, true);
            this.pokemob = mob;
        }

        @Override
        public double getSpeedModifier()
        {
            return super.getSpeedModifier() * PokecubeCore.getConfig().flyPathingSpeedFactor;
        }

        @Override
        public void tick()
        {
            if (pokemob.getController().blocksPathing()) return;
            
            if (this.operation == MoveControl.Operation.MOVE_TO)
            {
                this.operation = MoveControl.Operation.WAIT;
                this.mob.setNoGravity(true);
                final double dx = this.wantedX - this.mob.getX();
                final double dy = this.wantedY - this.mob.getY();
                final double dz = this.wantedZ - this.mob.getZ();
                // Total distance squared
                final double ds2 = dx * dx + dy * dy + dz * dz;
                if (ds2 < 0.01F)
                {
                    this.mob.setYya(0.0F);
                    this.mob.setZza(0.0F);
                    return;
                }
                // Horizontal distance
                final float dh = Mth.sqrt((float) (dx * dx + dz * dz));
                // Total distance
                final float ds = Mth.sqrt((float) ds2);

                final float f = (float) (Mth.atan2(dz, dx) * (180F / (float) Math.PI)) - 90.0F;
                this.mob.yRot = this.rotlerp(this.mob.yRot, f, 10.0F);

                float angleDiff = this.mob.yRot - f;
                angleDiff /= 180F / (float) Math.PI;

                final float dot = Mth.cos(angleDiff);

                float f1;
                f1 = (float) (this.getSpeedModifier() * this.pokemob.getMovementSpeed());

                this.mob.setSpeed(f1 * dot);
                this.mob.flyingSpeed = (float) (f1 * 0.05);
                final float f2 = (float) -(Mth.atan2(dy, dh) * (180F / (float) Math.PI));
                this.mob.xRot = this.rotlerp(this.mob.xRot, f2, 10.0F);
                f1 *= Math.abs(dy / ds);

                // Speeds up upwards motion if this is too slow.
                if (dy < 2 && dy > 0) f1 = Math.max(f1 * 10, 0.1f);

                this.mob.setYya(dy > 0.0D ? f1 : -f1);

                // dampen the velocity so they don't orbit their destination
                // points.
                final float dh_hat = Mth.abs(dh / ds);
                final float dy_hat = (float) Math.abs(dy / ds);
                final Vec3 v = this.mob.getDeltaMovement();
                this.mob.setDeltaMovement(v.x * dh_hat * dot, v.y * dy_hat * dot, v.z * dh_hat * dot);
            }
            else
            {
                this.mob.flyingSpeed = 0.02f;
                this.mob.setYya(0.0F);
                this.mob.setZza(0.0F);
            }
        }
    }

    private static enum NaviState
    {
        FLY, SWIM, WALK;
    }

    NaviState state = null;

    Vector3 here = Vector3.getNewVector();

    // Navigators
    private final FlyingPathNavigation flyPather;

    private final WalkPathNavi walkPather;
    private final ClimbPathNavi climbPather;
    private final SwimPathNavi swimPather;

    // Movement controllers
    private final MoveControl flyController;
    private final MoveControl walkController;
    private final MoveControl swimController;

    // Path validators
    Vector3 lastPos = Vector3.getNewVector();
    int time_at_pos = 0;

    public LogicFloatFlySwim(final IPokemob entity)
    {
        super(entity);
        this.flyPather = new FlyPathNavi(entity.getEntity(), entity.getEntity().getCommandSenderWorld());
        this.walkPather = new WalkPathNavi(entity.getEntity(), entity.getEntity().getCommandSenderWorld());
        this.climbPather = new ClimbPathNavi(entity.getEntity(), entity.getEntity().getCommandSenderWorld());
        this.swimPather = new SwimPathNavi(entity.getEntity(), entity.getEntity().getCommandSenderWorld());

        this.flyPather.setCanOpenDoors(false);
        this.flyPather.setCanFloat(true);
        this.flyPather.setCanPassDoors(true);

        this.swimPather.setCanOpenDoors(false);
        this.swimPather.setCanFloat(true);

        this.walkPather.setCanOpenDoors(false);
        this.walkPather.setCanFloat(true);

        this.flyController = new FlyController(entity);
        this.walkController = new WalkController(entity);
        this.swimController = new SwimController(entity);

        this.world = this.pokemob.getEntity().getCommandSenderWorld();
    }

    @Override
    public boolean shouldRun()
    {
        return !this.pokemob.getGeneralState(GeneralStates.CONTROLLED);
    }

    @Override
    public void tick(final Level world)
    {
        super.tick(world);

        final Path path = this.entity.getNavigation().getPath();
        if (path != null && !path.isDone())
        {
            final BlockPos next = path.getNextNodePos();
            final Vector3 hereVec = Vector3.getNewVector().set(this.entity);
            final Vector3 nextVec = Vector3.getNewVector().set(next);

            if (hereVec.distToSq(this.lastPos) < 1)
            {
                this.time_at_pos++;
                if (this.time_at_pos > 100)
                {
                    final double dr = nextVec.distanceTo(hereVec);
                    if (dr < 3) nextVec.moveEntity(this.entity);
                    else this.entity.getNavigation().stop();
                    this.time_at_pos = 0;
                }
            }
            else
            {
                this.lastPos.set(this.entity);
                this.time_at_pos = 0;
            }
            if (nextVec.distToSq(hereVec) < 1 && path.getNextNodeIndex() + 1 < path.getNodeCount())
                path.setNextNodeIndex(path.getNextNodeIndex() + 1);
        }

        final boolean air = this.pokemob.floats() || this.pokemob.flys();
        final boolean water = this.pokemob.getEntity().isInWater() && this.pokemob.swims();

        if (air && this.entity.isAlive())
        {
            if (this.state != NaviState.FLY)
            {
                this.entity.setNoGravity(!this.pokemob.isGrounded());
                this.pokemob.getEntity().navigation = this.flyPather;
                this.pokemob.getEntity().moveControl = this.flyController;
                this.flyPather.setCanOpenDoors(this.pokemob.isRoutineEnabled(AIRoutine.USEDOORS));
            }
            this.state = NaviState.FLY;
        }
        else if (water && this.entity.isAlive())
        {
            if (this.state != NaviState.SWIM)
            {
                this.pokemob.getEntity().navigation = this.swimPather;
                this.pokemob.getEntity().moveControl = this.swimController;
                this.swimPather.setCanOpenDoors(this.pokemob.isRoutineEnabled(AIRoutine.USEDOORS));
            }
            this.state = NaviState.SWIM;
        }
        else
        {
            if (this.state != NaviState.WALK)
            {
                this.entity.setNoGravity(false);
                this.pokemob.getEntity().navigation = this.climbPather;
                this.pokemob.getEntity().moveControl = this.walkController;
                this.climbPather.setCanOpenDoors(this.pokemob.isRoutineEnabled(AIRoutine.USEDOORS));
            }
            this.state = NaviState.WALK;
        }
    }
}
