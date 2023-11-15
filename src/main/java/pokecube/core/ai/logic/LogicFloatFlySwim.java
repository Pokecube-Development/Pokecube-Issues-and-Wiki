package pokecube.core.ai.logic;

import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.ai.AIRoutine;
import pokecube.api.entity.pokemob.ai.GeneralStates;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.pathing.ClimbPathNavi;
import pokecube.core.ai.pathing.FlyPathNavi;
import pokecube.core.ai.pathing.SwimPathNavi;
import pokecube.core.ai.pathing.WalkPathNavi;
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
    private static final UUID UIDS = UUID.fromString("4454b0d8-75ef-4689-8fce-daab61a7e1b2");

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
            if (this.operation == MoveControl.Operation.MOVE_TO)
            {
                this.operation = MoveControl.Operation.WAIT;
                double d0 = this.wantedX - this.mob.getX();
                double d1 = this.wantedZ - this.mob.getZ();
                double d2 = this.wantedY - this.mob.getY();
                double d3 = d0 * d0 + d2 * d2 + d1 * d1;

                if (d3 < 0.001F)
                {
                    this.mob.setYya(0.0F);
                    this.mob.setXxa(0.0F);
                    this.mob.setSpeed(0.0F);
                    Path path = this.mob.getNavigation().getPath();
                    if (path != null) path.advance();
                    return;
                }

                float f9 = (float) (Mth.atan2(d1, d0) * (180F / (float) Math.PI)) - 90.0F;
                this.mob.setYRot(this.rotlerp(this.mob.getYRot(), f9, 90.0F));
                this.mob.setSpeed((float) (this.speedModifier * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED)));
                BlockPos blockpos = this.mob.blockPosition();
                BlockState blockstate = this.mob.level.getBlockState(blockpos);
                VoxelShape voxelshape = blockstate.getCollisionShape(this.mob.level, blockpos);
                if (d2 > this.mob.getStepHeight() && d0 * d0 + d1 * d1 < Math.max(1.0F, this.mob.getBbWidth())
                        || !voxelshape.isEmpty() && this.mob.getY() < voxelshape.max(Direction.Axis.Y) + blockpos.getY()
                                && !blockstate.is(BlockTags.DOORS) && !blockstate.is(BlockTags.FENCES))
                {
                    this.mob.getJumpControl().jump();
                    this.operation = MoveControl.Operation.JUMPING;
                }
            }
            else super.tick();
        }

    }

    private static class SwimController extends MoveControl
    {
        private AttributeModifier speed_boost = null;
        final IPokemob pokemob;

        public SwimController(final IPokemob mob)
        {
            super(mob.getEntity());
            this.pokemob = mob;
            this.speed_boost = new AttributeModifier(UIDS, "pokecube:swim_speed",
                    PokecubeCore.getConfig().swimPathingSpeedFactor, AttributeModifier.Operation.MULTIPLY_BASE);
        }

        @Override
        public double getSpeedModifier()
        {
            return super.getSpeedModifier() * 0.25;
        }

        @Override
        public void tick()
        {
            this.mob.setNoGravity(this.mob.isInWater());
            if (pokemob.getController().blocksPathing() || !pokemob.getEntity().isAlive()) return;
            AttributeInstance attr = this.mob.getAttribute(Attributes.MOVEMENT_SPEED);
            this.speed_boost = new AttributeModifier(UIDS, "pokecube:swim_speed",
                    PokecubeCore.getConfig().swimPathingSpeedFactor, AttributeModifier.Operation.MULTIPLY_BASE);
            if (!attr.hasModifier(speed_boost) && this.mob.isInWater()) attr.addTransientModifier(speed_boost);
            else if (attr.hasModifier(speed_boost) && !this.mob.isInWater()) attr.removeModifier(speed_boost);

            if (this.operation == MoveControl.Operation.MOVE_TO && !this.mob.getNavigation().isDone())
            {
                this.operation = MoveControl.Operation.WAIT;

                final double dx = this.wantedX - this.mob.getX();
                final double dy = this.wantedY - this.mob.getY();
                final double dz = this.wantedZ - this.mob.getZ();

                // Total distance squared
                final double ds2 = dx * dx + dy * dy + dz * dz;

                if (ds2 < 0.001F)
                {
                    this.mob.setYya(0.0F);
                    this.mob.setXxa(0.0F);
                    this.mob.setSpeed(0.0F);
                    Path path = this.mob.getNavigation().getPath();
                    if (path != null) path.advance();
                    path.advance();
                    return;
                }
                // Horizontal distance
                final float dh = Mth.sqrt((float) (dx * dx + dz * dz));
                final float ds = Mth.sqrt((float) ds2);

                final float f = (float) (Mth.atan2(dz, dx) * (180F / (float) Math.PI)) - 90.0F;
                this.mob.yRot = this.rotlerp(this.mob.yRot, f, 10.0F);

                float angleDiff = this.mob.yRot - f;
                angleDiff /= 180F / (float) Math.PI;

                final float cos = Mth.cos(angleDiff);
                float f1 = (float) (this.getSpeedModifier() * this.pokemob.getMovementSpeed());

                float fwd = f1 * cos;
                this.mob.setSpeed(fwd);

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
                this.mob.setDeltaMovement(v.x * dh_hat * cos, v.y * dy_hat * cos, v.z * dh_hat * cos);
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
            if (pokemob.getController().blocksPathing() || !pokemob.getEntity().isAlive()) return;

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

    Vector3 here = new Vector3();

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
    final Vector3 lastPos = new Vector3();
    final Vector3 hereVec = new Vector3();
    final Vector3 nextVec = new Vector3();
    int time_at_pos = 0;

    public LogicFloatFlySwim(final IPokemob entity)
    {
        super(entity);
        this.flyPather = new FlyPathNavi(entity.getEntity(), entity.getEntity().getLevel());
        this.walkPather = new WalkPathNavi(entity.getEntity(), entity.getEntity().getLevel());
        this.climbPather = new ClimbPathNavi(entity.getEntity(), entity.getEntity().getLevel());
        this.swimPather = new SwimPathNavi(entity.getEntity(), entity.getEntity().getLevel());

        this.flyPather.setCanOpenDoors(false);
        this.flyPather.setCanFloat(true);
        this.flyPather.setCanPassDoors(true);

        this.swimPather.setCanOpenDoors(false);
        this.swimPather.setCanFloat(true);

        this.walkPather.setCanOpenDoors(false);
        this.walkPather.setCanFloat(true);

        this.climbPather.setCanOpenDoors(false);
        this.climbPather.setCanFloat(true);

        this.flyController = new FlyController(entity);
        this.walkController = new WalkController(entity);
        this.swimController = new SwimController(entity);

        this.world = this.pokemob.getEntity().getLevel();
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
        boolean hasPath = path != null && !path.isDone();
        if (hasPath)
        {
            final BlockPos next = path.getNextNodePos();
            hereVec.set(this.entity);
            nextVec.set(next);

            if (hereVec.distToSq(this.lastPos) < 1)
            {
                this.time_at_pos++;
                if (this.time_at_pos > 10)
                {
                    path.advance();
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
        boolean floats = this.pokemob.floats();
        boolean air = (floats || this.pokemob.flys());
        boolean water = this.pokemob.getEntity().isInWater() && this.pokemob.swims();

        if (floats && !hasPath && !this.pokemob.isGrounded())
        {
            hereVec.set(this.entity);
            nextVec.set(0, -1, 0);
            Vector3 next = Vector3.getNextSurfacePoint(world, hereVec, nextVec, pokemob.getFloatHeight());
            double vy = entity.getDeltaMovement().y;
            Vector3 push = hereVec.set(0, 0.01, 0);
            if (next == null) push.set(0, -0.01, 0);
            if (Math.signum(vy) != Math.signum(push.y)) push.addVelocities(entity);
        }

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
        else if (water)
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
