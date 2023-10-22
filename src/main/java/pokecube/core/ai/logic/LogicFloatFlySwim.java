package pokecube.core.ai.logic;

import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.control.SmoothSwimmingMoveControl;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;
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
    private static final UUID UIDF = UUID.fromString("4454b0d8-75ef-4689-8fce-daab61a7e1b3");

    private static class WalkController extends MoveControl
    {

        final IPokemob pokemob;

        public WalkController(final IPokemob mob)
        {
            super(mob.getEntity());
            this.pokemob = mob;
        }

        @Override
        public void tick()
        {
            if (pokemob.getController().blocksPathing()) return;
            super.tick();
        }

    }

    private static class SwimController extends SmoothSwimmingMoveControl
    {
        private AttributeModifier speed_boost = null;
        final IPokemob pokemob;

        public SwimController(final IPokemob mob)
        {
            super(mob.getEntity(), 85, 10, 0.1F, 0.1F, false);
            this.pokemob = mob;
            this.speed_boost = new AttributeModifier(UIDS, "pokecube:swim_speed",
                    PokecubeCore.getConfig().swimPathingSpeedFactor, AttributeModifier.Operation.MULTIPLY_BASE);
        }

        @Override
        public void tick()
        {
            this.mob.setNoGravity(this.mob.isInWater());
            if (pokemob.getController().blocksPathing() || !pokemob.getEntity().isAlive()) return;
            AttributeInstance attr = this.mob.getAttribute(Attributes.MOVEMENT_SPEED);
            if (!attr.hasModifier(speed_boost) && this.mob.isInWater()) attr.addTransientModifier(speed_boost);
            else if (attr.hasModifier(speed_boost) && !this.mob.isInWater()) attr.removeModifier(speed_boost);
            super.tick();
        }

    }

    private static class FlyController extends FlyingMoveControl
    {
        private AttributeModifier speed_boost = null;
        final IPokemob pokemob;

        public FlyController(final IPokemob mob)
        {
            // The true here is only used for the default behaviour to flag it
            // as not-re-enable gravity when not moving somewhere.
            super(mob.getEntity(), 80, true);
            this.pokemob = mob;
            this.speed_boost = new AttributeModifier(UIDF, "pokecube:fly_speed",
                    PokecubeCore.getConfig().flyPathingSpeedFactor, AttributeModifier.Operation.MULTIPLY_BASE);
        }

        @Override
        public void tick()
        {
            if (pokemob.getController().blocksPathing() || !pokemob.getEntity().isAlive()) return;
            AttributeInstance attr = this.mob.getAttribute(Attributes.FLYING_SPEED);
            if (!attr.hasModifier(speed_boost)) attr.addTransientModifier(speed_boost);
            super.tick();
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
        this.flyPather = new FlyPathNavi(entity.getEntity(), entity.getEntity().level());
        this.walkPather = new WalkPathNavi(entity.getEntity(), entity.getEntity().level());
        this.climbPather = new ClimbPathNavi(entity.getEntity(), entity.getEntity().level());
        this.swimPather = new SwimPathNavi(entity.getEntity(), entity.getEntity().level());

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

        this.world = this.pokemob.getEntity().level();
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
                if (this.time_at_pos > 100)
                {
                    this.entity.getNavigation().stop();
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
