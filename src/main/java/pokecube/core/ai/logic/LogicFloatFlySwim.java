package pokecube.core.ai.logic;

import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.controller.FlyingMovementController;
import net.minecraft.entity.ai.controller.MovementController;
import net.minecraft.pathfinding.ClimberPathNavigator;
import net.minecraft.pathfinding.FlyingPathNavigator;
import net.minecraft.pathfinding.GroundPathNavigator;
import net.minecraft.pathfinding.SwimmerPathNavigator;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
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

        public SwimController(final IPokemob mob)
        {
            super(mob.getEntity());
            this.entity = mob.getEntity();
        }

        @Override
        public void tick()
        {
            this.entity.setNoGravity(this.entity.isInWater());
            this.speed = 1;

            if (this.action == MovementController.Action.MOVE_TO && !this.entity.getNavigator().noPath())
            {
                final double d0 = this.posX - this.entity.posX;
                double d1 = this.posY - this.entity.posY;
                final double d2 = this.posZ - this.entity.posZ;
                final double d3 = MathHelper.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
                d1 = d1 / d3;
                final float f = (float) (MathHelper.atan2(d2, d0) * (180F / (float) Math.PI)) - 90.0F;
                this.entity.rotationYaw = this.limitAngle(this.entity.rotationYaw, f, 90.0F);
                this.entity.renderYawOffset = this.entity.rotationYaw;
                final float f1 = (float) (this.speed * this.entity.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED)
                        .getValue());
                this.entity.setAIMoveSpeed(MathHelper.lerp(0.125F, this.entity.getAIMoveSpeed(), f1));
                this.entity.setMotion(this.entity.getMotion().add(0.0D, this.entity.getAIMoveSpeed() * d1 * 0.1D,
                        0.0D));
            }
            else this.entity.setAIMoveSpeed(0.0F);
        }

    }

    private static class FlyMovementController extends FlyingMovementController
    {
        public FlyMovementController(final IPokemob mob)
        {
            super(mob.getEntity());
        }

        @Override
        public void tick()
        {
            if (this.action == MovementController.Action.MOVE_TO)
            {
                this.action = MovementController.Action.WAIT;
                this.mob.setNoGravity(true);
                final double dx = this.posX - this.mob.posX;
                final double dy = this.posY - this.mob.posY;
                final double dz = this.posZ - this.mob.posZ;
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
                if (this.mob.onGround) f1 = (float) (this.getSpeed() * this.mob.getAttribute(
                        SharedMonsterAttributes.MOVEMENT_SPEED).getValue());
                else f1 = (float) (this.getSpeed() * this.mob.getAttribute(SharedMonsterAttributes.FLYING_SPEED)
                        .getValue());

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
        if (this.pokemob.floats() || this.pokemob.flys())
        {
            this.entity.setNoGravity(!this.pokemob.isGrounded());
            this.pokemob.getEntity().navigator = this.flyPather;
            this.pokemob.getEntity().moveController = this.flyController;
        }
        else if (this.pokemob.getEntity().isInWater())
        {
            this.pokemob.getEntity().navigator = this.swimPather;
            this.pokemob.getEntity().moveController = this.swimController;
        }
        else
        {
            this.entity.setNoGravity(false);
            this.pokemob.getEntity().navigator = this.walkPather;
            this.pokemob.getEntity().moveController = this.walkController;
        }
    }
}
