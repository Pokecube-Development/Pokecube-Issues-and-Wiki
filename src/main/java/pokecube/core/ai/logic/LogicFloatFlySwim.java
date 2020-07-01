package pokecube.core.ai.logic;

import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.controller.FlyingMovementController;
import net.minecraft.entity.ai.controller.MovementController;
import net.minecraft.pathfinding.ClimberPathNavigator;
import net.minecraft.pathfinding.FlyingPathNavigator;
import net.minecraft.pathfinding.GroundPathNavigator;
import net.minecraft.pathfinding.SwimmerPathNavigator;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
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
    private static class SwimController extends MovementController
    {
        private final MobEntity entity;

        final IPokemob pokemob;

        public SwimController(final IPokemob mob)
        {
            super(mob.getEntity());
            this.entity = mob.getEntity();
            this.pokemob = mob;
        }

        @Override
        public void tick()
        {
            this.entity.setNoGravity(this.entity.isInWater());

            if (this.action == MovementController.Action.MOVE_TO && !this.entity.getNavigator().noPath())
            {
                this.action = MovementController.Action.WAIT;

                final double dx = this.posX - this.mob.getPosX();
                final double dy = this.posY - this.mob.getPosY();
                final double dz = this.posZ - this.mob.getPosZ();
                // Total distance squared
                final double ds2 = dx * dx + dy * dy + dz * dz;
                if (ds2 < 0.01F)
                {
                    this.mob.setMoveVertical(0.0F);
                    this.mob.setMoveForward(0.0F);
                    return;
                }
                // Horizontal distance
                final float dh = MathHelper.sqrt(dx * dx + dz * dz);
                final float ds = MathHelper.sqrt(ds2);

                final float f = (float) (MathHelper.atan2(dz, dx) * (180F / (float) Math.PI)) - 90.0F;
                this.mob.rotationYaw = this.limitAngle(this.mob.rotationYaw, f, 10.0F);

                float angleDiff = this.mob.rotationYaw - f;
                angleDiff /= 180F / (float) Math.PI;

                final float dot = MathHelper.cos(angleDiff);
                float f1 = (float) (this.getSpeed() * this.pokemob.getMovementSpeed());

                this.mob.setAIMoveSpeed(f1 * dot);
                this.mob.jumpMovementFactor = (float) (f1 * 0.05);
                final float f2 = (float) -(MathHelper.atan2(dy, dh) * (180F / (float) Math.PI));
                this.mob.rotationPitch = this.limitAngle(this.mob.rotationPitch, f2, 10.0F);
                f1 *= Math.abs(dy / ds);
                this.mob.setMoveVertical(dy > 0.0D ? f1 : -f1);

                // dampen the velocity so they don't orbit their destination
                // points.
                final float dh_hat = MathHelper.abs(dh / ds);
                final float dy_hat = (float) Math.abs(dy / ds);
                final Vec3d v = this.mob.getMotion();
                this.mob.setMotion(v.x * dh_hat * dot, v.y * dy_hat * dot, v.z * dh_hat * dot);
            }
            else this.entity.setAIMoveSpeed(0.0F);
        }

    }

    private static class FlyMovementController extends FlyingMovementController
    {
        final IPokemob pokemob;

        public FlyMovementController(final IPokemob mob)
        {
            // TODO check what the bool here should be
            super(mob.getEntity(), 20, true);
            this.pokemob = mob;
        }

        @Override
        public void tick()
        {
            if (this.action == MovementController.Action.MOVE_TO)
            {
                this.action = MovementController.Action.WAIT;
                this.mob.setNoGravity(true);
                final double dx = this.posX - this.mob.getPosX();
                final double dy = this.posY - this.mob.getPosY();
                final double dz = this.posZ - this.mob.getPosZ();
                // Total distance squared
                final double ds2 = dx * dx + dy * dy + dz * dz;
                if (ds2 < 0.01F)
                {
                    this.mob.setMoveVertical(0.0F);
                    this.mob.setMoveForward(0.0F);
                    return;
                }
                // Horizontal distance
                final float dh = MathHelper.sqrt(dx * dx + dz * dz);
                final float ds = MathHelper.sqrt(ds2);

                final float f = (float) (MathHelper.atan2(dz, dx) * (180F / (float) Math.PI)) - 90.0F;
                this.mob.rotationYaw = this.limitAngle(this.mob.rotationYaw, f, 10.0F);

                float angleDiff = this.mob.rotationYaw - f;
                angleDiff /= 180F / (float) Math.PI;

                final float dot = MathHelper.cos(angleDiff);

                float f1;
                f1 = (float) (this.getSpeed() * this.pokemob.getMovementSpeed());

                this.mob.setAIMoveSpeed(f1 * dot);
                this.mob.jumpMovementFactor = (float) (f1 * 0.05);
                final float f2 = (float) -(MathHelper.atan2(dy, dh) * (180F / (float) Math.PI));
                this.mob.rotationPitch = this.limitAngle(this.mob.rotationPitch, f2, 10.0F);
                f1 *= Math.abs(dy / ds);
                this.mob.setMoveVertical(dy > 0.0D ? f1 : -f1);

                // dampen the velocity so they don't orbit their destination
                // points.
                final float dh_hat = MathHelper.abs(dh / ds);
                final float dy_hat = (float) Math.abs(dy / ds);
                final Vec3d v = this.mob.getMotion();
                this.mob.setMotion(v.x * dh_hat * dot, v.y * dy_hat * dot, v.z * dh_hat * dot);
            }
            else
            {
                this.mob.jumpMovementFactor = 0.02f;
                this.mob.setMoveVertical(0.0F);
                this.mob.setMoveForward(0.0F);
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
    private final FlyingPathNavigator  flyPather;
    private final GroundPathNavigator  walkPather;
    private final ClimberPathNavigator climbPather;
    private final SwimmerPathNavigator swimPather;

    // Movement controllers
    private final MovementController flyController;
    private final MovementController walkController;
    private final MovementController swimController;

    public LogicFloatFlySwim(final IPokemob entity)
    {
        super(entity);
        this.flyPather = new FlyPathNavi(entity.getEntity(), entity.getEntity().getEntityWorld());
        this.walkPather = new WalkPathNavi(entity.getEntity(), entity.getEntity().getEntityWorld());
        this.climbPather = new ClimbPathNavi(entity.getEntity(), entity.getEntity().getEntityWorld());
        this.swimPather = new SwimPathNavi(entity.getEntity(), entity.getEntity().getEntityWorld());

        this.flyPather.setCanOpenDoors(false);
        this.flyPather.setCanSwim(true);
        this.flyPather.setCanEnterDoors(true);

        this.walkPather.setCanSwim(true);
        this.climbPather.setCanSwim(true);
        this.swimPather.setCanSwim(true);

        this.flyController = new FlyMovementController(entity);
        this.walkController = new MovementController(entity.getEntity());
        this.swimController = new SwimController(entity);

        this.world = this.pokemob.getEntity().getEntityWorld();

        if (this.world instanceof ServerWorld) ((ServerWorld) this.world).navigations.remove(this.entity
                .getNavigator());
        this.pokemob.getEntity().navigator = this.walkPather;
        this.pokemob.getEntity().moveController = this.walkController;
        if (this.world instanceof ServerWorld) ((ServerWorld) this.world).navigations.add(this.entity.getNavigator());
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

        if (world instanceof ServerWorld) synchronized (((ServerWorld) world).navigations)
        {
            ((ServerWorld) world).navigations.remove(this.entity.getNavigator());
        }

        if (this.pokemob.floats() || this.pokemob.flys())
        {
            if (this.state != NaviState.FLY)
            {
                this.entity.setNoGravity(!this.pokemob.isGrounded());
                this.pokemob.getEntity().navigator = this.flyPather;
                this.pokemob.getEntity().moveController = this.flyController;
            }
            this.state = NaviState.FLY;
        }
        else if (this.pokemob.getEntity().isInWater() && this.pokemob.swims())
        {
            if (this.state != NaviState.SWIM)
            {
                this.pokemob.getEntity().navigator = this.swimPather;
                this.pokemob.getEntity().moveController = this.swimController;
            }
            this.state = NaviState.SWIM;
        }
        else
        {
            if (this.state != NaviState.WALK)
            {
                this.entity.setNoGravity(false);
                this.pokemob.getEntity().navigator = this.walkPather;
                this.pokemob.getEntity().moveController = this.walkController;
            }
            this.state = NaviState.WALK;
        }

        if (world instanceof ServerWorld) synchronized (((ServerWorld) world).navigations)
        {
            ((ServerWorld) world).navigations.add(this.entity.getNavigator());
        }
    }
}
