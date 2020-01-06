package pokecube.core.ai.logic;

import net.minecraft.block.Block;
import net.minecraft.pathfinding.Path;
import net.minecraft.util.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IMoveConstants.AIRoutine;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.interfaces.pokemob.ai.LogicStates;
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
    Vector3 here = Vector3.getNewVector();

    public LogicFloatFlySwim(final IPokemob entity)
    {
        super(entity);
    }

    private void doFloatFly(final Vector3 here)
    {
        IPokemob pokemob = this.pokemob;
        final IPokemob transformed = CapabilityPokemob.getPokemobFor(pokemob.getTransformedTo());
        if (transformed != null) pokemob = transformed;
        final PokedexEntry entry = pokemob.getPokedexEntry();
        boolean canFloat = pokemob.floats();
        final Vec3d v = this.entity.getMotion();
        final double vx = v.x;
        double vy = v.y;
        final double vz = v.z;
        if (canFloat && !pokemob.getLogicState(LogicStates.INWATER))
        {
            final float floatHeight = (float) entry.preferedHeight;
            final Vector3 down = Vector3.getNextSurfacePoint(this.entity.getEntityWorld(), here.set(pokemob
                    .getEntity()), Vector3.secondAxisNeg, floatHeight);
            if (down != null) here.set(down);
            final boolean solidDown = Block.hasSolidSide(here.getBlockState(this.world), this.world, here.getPos(),
                    Direction.UP);
            if (!solidDown && !pokemob.getLogicState(LogicStates.SLEEPING)) vy += 0.005;
            else vy -= 0.01;
            if (down == null || pokemob.getLogicState(LogicStates.SITTING)) vy -= 0.02;
            here.set(pokemob.getEntity());
        }
        if ((canFloat || pokemob.flys()) && !pokemob.getCombatState(CombatStates.ANGRY))
        {
            final float floatHeight = (float) entry.preferedHeight;
            final Path path = this.entity.getNavigator().getPath();
            if (path != null)
            {
                final Vector3 end = Vector3.getNewVector().set(path.getFinalPathPoint());
                final double dhs = (here.x - end.x) * (here.x - end.x) + (here.z - end.z) * (here.z - end.z);
                final double dvs = (here.y - end.y) * (here.y - end.y);
                final double width = Math.max(0.5, pokemob.getSize() * entry.length / 4);
                if (dhs < width * width && dvs <= floatHeight * floatHeight) this.entity.getNavigator().clearPath();
            }
        }
        canFloat = canFloat || pokemob.flys();
        canFloat = canFloat && pokemob.isRoutineEnabled(AIRoutine.AIRBORNE);
        this.entity.setNoGravity(canFloat);
        if (canFloat && here.offset(Direction.DOWN).getBlockState(this.entity.getEntityWorld()).getMaterial()
                .isLiquid())
        {
            if (vy < -0.1) vy = 0;
            vy += 0.05;
        }
        this.entity.setMotion(vx, vy, vz);
    }

    private void doSwim(final Vector3 here)
    {
        if (!(this.entity.isInWater() || this.entity.isInLava())) return;
        IPokemob pokemob = this.pokemob;
        final IPokemob transformed = CapabilityPokemob.getPokemobFor(pokemob.getTransformedTo());
        if (transformed != null) pokemob = transformed;
        final PokedexEntry entry = pokemob.getPokedexEntry();
        final boolean isWaterMob = pokemob.getPokedexEntry().swims();
        if (!isWaterMob)
        {
            if (this.entity.getRNG().nextFloat() < 0.8F) this.entity.getJumpController().setJumping();
        }
        else if (isWaterMob) if (!pokemob.getCombatState(CombatStates.ANGRY))
        {
            final float floatHeight = (float) 0.5;
            final Path path = this.entity.getNavigator().getPath();
            if (path != null)
            {
                final Vector3 end = Vector3.getNewVector().set(path.getFinalPathPoint());
                final double dhs = (here.x - end.x) * (here.x - end.x) + (here.z - end.z) * (here.z - end.z);
                final double dvs = (here.y - end.y) * (here.y - end.y);
                final double width = Math.max(0.5, pokemob.getSize() * entry.length / 4);
                if (dhs < width * width && dvs <= floatHeight * floatHeight) this.entity.getNavigator().clearPath();
            }
        }
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
        if (!this.shouldRun()) return;
        this.here.set(this.entity);
        if (this.pokemob.getDirectionPitch() == 0) this.entity.setMoveVertical(0);
        if (this.entity.getNavigator().noPath()) this.doFloatFly(this.here);
        this.doSwim(this.here);
    }
}
