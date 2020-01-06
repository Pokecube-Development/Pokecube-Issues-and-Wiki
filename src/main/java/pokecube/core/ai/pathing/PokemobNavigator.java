package pokecube.core.ai.pathing;

import net.minecraft.entity.Entity;
import net.minecraft.pathfinding.FlyingNodeProcessor;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathFinder;
import net.minecraft.pathfinding.PathNavigator;
import net.minecraft.pathfinding.SwimNodeProcessor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.pathing.node.WalkNodeLadderProcessor;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.interfaces.pokemob.ai.LogicStates;
import thut.api.maths.Vector3;

/**
 * This is overridden from the vanilla one to allow using a custom,
 * multi-threaded pathfinder. It also does some pokemob specific checks for
 * whether the pokemob can navigate, as well as checks to see if the pathing
 * should terminate early in certain situations, such as "low priority" paths
 * which are close enough to the end, like when a mob is just idling around.
 */
public class PokemobNavigator extends PathNavigator
{
    private final Vector3  v                 = Vector3.getNewVector();
    private final Vector3  v1                = Vector3.getNewVector();
    private boolean        canDive;
    private boolean        canFly;
    private final IPokemob pokemob;
    private PokedexEntry   lastEntry;
    private PathNavigator2 wrapped;
    private boolean        lastGroundedState = false;

    public PokemobNavigator(final IPokemob pokemob, final World world)
    {
        super(pokemob.getEntity(), world);
        this.pokemob = pokemob;
        this.canDive = pokemob.swims();
        this.checkValues();
    }

    /** If on ground or swimming and can swim */
    @Override
    public boolean canNavigate()
    {
        if (this.wrapped == null) this.checkValues();
        if (this.pokemob.getLogicState(LogicStates.SLEEPING) || (this.pokemob.getStatus()
                & IMoveConstants.STATUS_SLP) > 0 || (this.pokemob.getStatus() & IMoveConstants.STATUS_FRZ) > 0
                || this.pokemob.getGeneralState(GeneralStates.CONTROLLED) || this.pokemob.getLogicState(
                        LogicStates.NOPATHING)) return false;
        if (this.pokemob.getLogicState(LogicStates.SITTING)) return false;
        return this.entity.onGround || this.isInLiquid() || this.canFly;
    }

    private void checkValues()
    {
        PokedexEntry entry = this.pokemob.getPokedexEntry();
        final IPokemob transformed = CapabilityPokemob.getPokemobFor(this.pokemob.getTransformedTo());
        if (transformed != null) entry = transformed.getPokedexEntry();
        if (entry != this.lastEntry || this.lastGroundedState != this.pokemob.isGrounded() || this.wrapped == null)
        {
            this.lastGroundedState = this.pokemob.isGrounded();
            this.lastEntry = entry;
            this.canFly = this.pokemob.flys() || this.pokemob.floats();
            this.canFly = this.canFly && !this.lastGroundedState;
            this.canDive = entry.swims();
            if (this.canDive && this.canFly) this.wrapped = new MultiNodeNavigator(this.entity, this.world,
                    new FlyingNodeProcessor(), this.makeSwimingNavigator().getNodeProcessor(), this.canFly);
            if (this.canFly && !this.canDive) this.wrapped = this.makeFlyingNavigator();
            else if (this.canDive) this.wrapped = this.makeSwimingNavigator();
            else this.wrapped = new LadderWalkNavigator(this.entity, this.world, this.canFly);
        }
        this.wrapped.getNodeProcessor().setCanEnterDoors(true);
        this.wrapped.getNodeProcessor().setCanSwim(true);
        this.wrapped.getNodeProcessor().init(this.world, this.entity);
        this.wrapped.setSpeed(this.speed);
        this.nodeProcessor = this.wrapped.getNodeProcessor();
    }

    @Override
    protected Vec3d getEntityPosition()
    {
        return new Vec3d(this.entity.posX, this.entity.posY, this.entity.posZ);
    }

    @Override
    protected PathFinder getPathFinder(final int num)
    {
        if (this.pokemob != null) this.checkValues();
        if (this.wrapped != null) return this.wrapped.getPathFinder(num);
        return null;
    }

    @Override
    public Path getPathToEntityLiving(final Entity entityIn, final int arg)
    {
        if (!this.canNavigate() || entityIn == null) return null;
        this.checkValues();
        try
        {
            return this.wrapped.getPathToEntityLiving(entityIn, arg);
        }
        catch (final Exception e)
        {
            PokecubeCore.LOGGER.warn("Error making path for " + this.entity + " to " + entityIn + " " + this.wrapped,
                    e);
            return null;
        }
    }

    @Override
    public Path getPathToPos(final BlockPos pos, final int arg)
    {
        if (!this.canNavigate() || pos == null) return null;
        this.checkValues();
        if (this.shouldPath(pos)) try
        {
            return this.wrapped.getPathToPos(pos, arg);
        }
        catch (final Exception e)
        {
            PokecubeCore.LOGGER.warn("Error making path for " + this.entity + " to " + pos + " " + this.wrapped, e);
            return null;
        }
        else return this.getPath();
    }

    /**
     * Returns true when an entity of specified size could safely walk in a
     * straight line between the two points. Args: pos1, pos2, entityXSize,
     * entityYSize, entityZSize
     */
    @Override
    public boolean isDirectPathBetweenPoints(final Vec3d start, final Vec3d end, final int sizeX, final int sizeY,
            final int sizeZ)
    {
        final Vector3 v1 = Vector3.getNewVector().set(start);
        final Vector3 v2 = Vector3.getNewVector().set(end);
        final boolean ground = !this.canFly;

        // TODO check to see that all blocks in direction have same path
        // weighting
        if (ground) return false;
        final double dx = sizeX / 2d;
        final double dy = sizeY;
        final double dz = sizeZ / 2d;

        v1.set(start).addTo(0, 0, 0);
        v2.set(end).addTo(0, 0, 0);
        if (!v1.isVisible(this.world, v2)) return false;
        v1.set(start).addTo(0, dy, 0);
        v2.set(end).addTo(0, dy, 0);
        if (!v1.isVisible(this.world, v2)) return false;

        v1.set(start).addTo(dx, 0, 0);
        v2.set(end).addTo(dx, 0, 0);
        if (!v1.isVisible(this.world, v2)) return false;
        v1.set(start).addTo(-dx, 0, 0);
        v2.set(end).addTo(-dx, 0, 0);
        if (!v1.isVisible(this.world, v2)) return false;
        v1.set(start).addTo(0, 0, dz);
        v2.set(end).addTo(0, 0, dz);
        if (!v1.isVisible(this.world, v2)) return false;
        v1.set(start).addTo(0, 0, -dz);
        v2.set(end).addTo(0, 0, -dz);
        return true;
    }

    private PathNavigator2 makeFlyingNavigator()
    {
        return new MultiNodeNavigator(this.entity, this.world, new FlyingNodeProcessor(), new WalkNodeLadderProcessor(),
                this.canFly);
    }

    private PathNavigator2 makeSwimingNavigator()
    {
        return new MultiNodeNavigator(this.entity, this.world, new SwimNodeProcessor(true),
                new WalkNodeLadderProcessor(), this.canFly);
    }

    private boolean shouldPath(final BlockPos pos)
    {
        final Path current = this.noPath() ? null : this.getPath();
        if (current != null && !this.pokemob.getCombatState(CombatStates.ANGRY))
        {
            final Vector3 p = this.v.set(current.getFinalPathPoint());
            final Vector3 v = this.v1.set(pos);
            if (p.distToSq(v) <= 1) return false;
        }
        return true;
    }

    @Override
    public void tick()
    {
        super.tick();
    }
}
