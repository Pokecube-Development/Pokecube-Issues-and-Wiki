package pokecube.core.ai.logic;

import net.minecraft.entity.ai.controller.FlyingMovementController;
import net.minecraft.entity.ai.controller.MovementController;
import net.minecraft.pathfinding.FlyingPathNavigator;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNavigator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.WorldWorkerManager;
import net.minecraftforge.common.WorldWorkerManager.IWorker;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.pathing.ClimbPathNavi;
import pokecube.core.ai.pathing.FlyPathNavi;
import pokecube.core.ai.pathing.SwimPathNavi;
import pokecube.core.ai.pathing.WalkPathNavi;
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
    private static class WalkController extends MovementController
    {

        public WalkController(final IPokemob mob)
        {
            super(mob.getEntity());
        }

        @Override
        public double getSpeedModifier()
        {
            return super.getSpeedModifier();
        }

        @Override
        public void tick()
        {
            super.tick();
        }

    }

    private static class SwimController extends MovementController
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

            if (this.operation == MovementController.Action.MOVE_TO && !this.mob.getNavigation().isDone())
            {
                this.operation = MovementController.Action.WAIT;

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
                final float dh = MathHelper.sqrt(dx * dx + dz * dz);
                final float ds = MathHelper.sqrt(ds2);

                final float f = (float) (MathHelper.atan2(dz, dx) * (180F / (float) Math.PI)) - 90.0F;
                this.mob.yRot = this.rotlerp(this.mob.yRot, f, 10.0F);

                float angleDiff = this.mob.yRot - f;
                angleDiff /= 180F / (float) Math.PI;

                final float dot = MathHelper.cos(angleDiff);
                float f1 = (float) (this.getSpeedModifier() * this.pokemob.getMovementSpeed());

                this.mob.setSpeed(f1 * dot);
                this.mob.flyingSpeed = (float) (f1 * 0.05);
                final float f2 = (float) -(MathHelper.atan2(dy, dh) * (180F / (float) Math.PI));
                this.mob.xRot = this.rotlerp(this.mob.xRot, f2, 10.0F);
                f1 *= Math.abs(dy / ds);

                // Speeds up upwards motion if this is too slow.
                if (dy < 2 && dy > 0) f1 = Math.max(f1 * 10, 0.1f);

                this.mob.setYya(dy > 0.0D ? f1 : -f1);

                // dampen the velocity so they don't orbit their destination
                // points.
                final float dh_hat = MathHelper.abs(dh / ds);
                final float dy_hat = (float) Math.abs(dy / ds);
                final Vector3d v = this.mob.getDeltaMovement();
                this.mob.setDeltaMovement(v.x * dh_hat * dot, v.y * dy_hat * dot, v.z * dh_hat * dot);
            }
            else this.mob.setSpeed(0.0F);
        }

    }

    private static class FlyController extends FlyingMovementController
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
            if (this.operation == MovementController.Action.MOVE_TO)
            {
                this.operation = MovementController.Action.WAIT;
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
                final float dh = MathHelper.sqrt(dx * dx + dz * dz);
                // Total distance
                final float ds = MathHelper.sqrt(ds2);

                final float f = (float) (MathHelper.atan2(dz, dx) * (180F / (float) Math.PI)) - 90.0F;
                this.mob.yRot = this.rotlerp(this.mob.yRot, f, 10.0F);

                float angleDiff = this.mob.yRot - f;
                angleDiff /= 180F / (float) Math.PI;

                final float dot = MathHelper.cos(angleDiff);

                float f1;
                f1 = (float) (this.getSpeedModifier() * this.pokemob.getMovementSpeed());

                this.mob.setSpeed(f1 * dot);
                this.mob.flyingSpeed = (float) (f1 * 0.05);
                final float f2 = (float) -(MathHelper.atan2(dy, dh) * (180F / (float) Math.PI));
                this.mob.xRot = this.rotlerp(this.mob.xRot, f2, 10.0F);
                f1 *= Math.abs(dy / ds);

                // Speeds up upwards motion if this is too slow.
                if (dy < 2 && dy > 0) f1 = Math.max(f1 * 10, 0.1f);

                this.mob.setYya(dy > 0.0D ? f1 : -f1);

                // dampen the velocity so they don't orbit their destination
                // points.
                final float dh_hat = MathHelper.abs(dh / ds);
                final float dy_hat = (float) Math.abs(dy / ds);
                final Vector3d v = this.mob.getDeltaMovement();
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

    private static class NaviUpdate implements IWorker
    {
        private final ServerWorld   world;
        private final PathNavigator oldNavi;
        private final PathNavigator newNavi;

        public NaviUpdate(final ServerWorld world, final PathNavigator oldNavi, final PathNavigator newNavi)
        {
            this.world = world;
            this.oldNavi = oldNavi;
            this.newNavi = newNavi;
        }

        @Override
        public boolean hasWork()
        {
            return false;
        }

        @Override
        public boolean doWork()
        {
            synchronized (this.world.navigations)
            {
                this.world.navigations.remove(this.oldNavi);
                this.world.navigations.add(this.newNavi);
            }
            return false;
        }

    }

    private static enum NaviState
    {
        FLY, SWIM, WALK;
    }

    NaviState state = null;

    Vector3 here = Vector3.getNewVector();

    // Navigators
    private final FlyingPathNavigator flyPather;

    private final PathNavigator walkPather;
    private final PathNavigator climbPather;
    private final PathNavigator swimPather;

    // Movement controllers
    private final MovementController flyController;
    private final MovementController walkController;
    private final MovementController swimController;

    // Path validators
    Vector3 lastPos     = Vector3.getNewVector();
    int     time_at_pos = 0;

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

        this.walkPather.setCanFloat(true);
        this.climbPather.setCanFloat(true);
        this.swimPather.setCanFloat(true);

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
    public void tick(final World world)
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
            if (nextVec.distToSq(hereVec) < 1 && path.getNextNodeIndex() + 1 < path.getNodeCount()) path
                    .setNextNodeIndex(path.getNextNodeIndex() + 1);
        }

        final PathNavigator oldNavi = this.entity.getNavigation();

        final boolean air = this.pokemob.floats() || this.pokemob.flys();
        final boolean water = this.pokemob.getEntity().isInWater() && this.pokemob.swims();

        if (air && this.entity.isAlive())
        {
            if (this.state != NaviState.FLY)
            {
                this.entity.setNoGravity(!this.pokemob.isGrounded());
                this.pokemob.getEntity().navigation = this.flyPather;
                this.pokemob.getEntity().moveControl = this.flyController;
            }
            this.state = NaviState.FLY;
        }
        else if (water && this.entity.isAlive())
        {
            if (this.state != NaviState.SWIM)
            {
                this.pokemob.getEntity().navigation = this.swimPather;
                this.pokemob.getEntity().moveControl = this.swimController;
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
            }
            this.state = NaviState.WALK;
        }
        final PathNavigator newNavi = this.entity.getNavigation();
        if (world instanceof ServerWorld && newNavi != oldNavi) WorldWorkerManager.addWorker(new NaviUpdate(
                (ServerWorld) world, oldNavi, newNavi));
    }
}
