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
import net.minecraft.world.World;
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
                final double d0 = this.posX - this.mob.posX;
                final double d1 = this.posY - this.mob.posY;
                final double d2 = this.posZ - this.mob.posZ;
                final double d3 = d0 * d0 + d1 * d1 + d2 * d2;
                if (d3 < 2.5000003E-7F)
                {
                    this.mob.setMoveVertical(0.0F);
                    this.mob.setMoveForward(0.0F);
                    return;
                }

                final float f = (float) (MathHelper.atan2(d2, d0) * (180F / (float) Math.PI)) - 90.0F;
                this.mob.rotationYaw = this.limitAngle(this.mob.rotationYaw, f, 10.0F);
                float f1;
                if (this.mob.onGround) f1 = (float) (this.getSpeed() * this.mob.getAttribute(
                        SharedMonsterAttributes.MOVEMENT_SPEED).getValue());
                else f1 = (float) (this.getSpeed() * this.mob.getAttribute(SharedMonsterAttributes.FLYING_SPEED)
                        .getValue());
                this.mob.setAIMoveSpeed(f1);
                this.mob.jumpMovementFactor = f1 * 0.05f;
                final double d4 = MathHelper.sqrt(d0 * d0 + d2 * d2);
                final float f2 = (float) -(MathHelper.atan2(d1, d4) * (180F / (float) Math.PI));
                this.mob.rotationPitch = this.limitAngle(this.mob.rotationPitch, f2, 10.0F);
                this.mob.setMoveVertical(d1 > 0.0D ? f1 : -f1);
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
        this.flyPather = new FlyingPathNavigator(entity.getEntity(), entity.getEntity().getEntityWorld());
        this.walkPather = new GroundPathNavigator(entity.getEntity(), entity.getEntity().getEntityWorld());
        this.climbPather = new ClimberPathNavigator(entity.getEntity(), entity.getEntity().getEntityWorld());
        this.swimPather = new SwimmerPathNavigator(entity.getEntity(), entity.getEntity().getEntityWorld());

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
