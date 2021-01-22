package pokecube.core.ai.tasks.ants.tasks.work;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

import net.minecraft.util.math.BlockPos;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.tasks.ants.AntTasks.AntJob;
import pokecube.core.ai.tasks.ants.nest.Edge;
import pokecube.core.ai.tasks.ants.nest.Node;
import pokecube.core.ai.tasks.ants.nest.Part;
import pokecube.core.ai.tasks.ants.tasks.AbstractConstructTask;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;

public class Dig extends AbstractConstructTask
{
    public Dig(final IPokemob pokemob)
    {
        super(pokemob, j -> j == AntJob.DIG);
    }

    private boolean digPart(final Part part)
    {
        final long time = this.world.getGameTime();
        if (!part.shouldDig(time)) return false;
        this.valids.set(0);
        final AtomicBoolean shor = new AtomicBoolean(false);
        shor.set(false);

        // Start with a check of if the pos is inside.
        Predicate<BlockPos> isValid = p -> part.getTree().isInside(p);
        // If it is inside, and not diggable, we notify the node of the
        // dug spot, finally we check if there is space nearby to stand.
        isValid = isValid.and(p ->
        {
            if (this.diggable.test(this.world.getBlockState(p)))
            {
                this.valids.getAndIncrement();
                return this.hasEmptySpace.test(p);
            }
            return false;
        });
        Optional<BlockPos> valid = part.getDigBounds().stream().filter(isValid).findAny();
        if (valid.isPresent())
        {
            this.work_pos = valid.get().toImmutable();
            if (PokecubeMod.debug) PokecubeCore.LOGGER.debug("Found Dig Site!");
            return true;
        }
        final boolean done = part instanceof Edge ? this.valids.get() == 0 : this.valids.get() < 3;
        if (PokecubeMod.debug) PokecubeCore.LOGGER.debug("No Site " + this.valids.get() + " " + done);
        // None were valid to dig, so mark as done.
        if (done) part.setDigDone(time + (part instanceof Node ? 2400 : 1200));
        else if (shor.get())
        {
            // Start with a check of if the pos is inside.
            isValid = p -> part.getTree().isInside(p);
            // If it is inside, and not diggable, we notify the node of the
            // dug spot, finally we check if there is space nearby to stand.
            isValid = isValid.and(p ->
            {
                if (this.diggable.test(this.world.getBlockState(p)))
                {
                    this.valids.getAndIncrement();
                    return this.canStandNear.test(p);
                }
                return false;
            });
            valid = part.getDigBounds().stream().filter(isValid).findAny();
            if (valid.isPresent())
            {
                this.work_pos = valid.get().toImmutable();
                if (PokecubeMod.debug) PokecubeCore.LOGGER.debug("Found Dig Site!");
                return true;
            }
        }
        return false;
    }

    private boolean divert(final Part old)
    {
        final long time = this.world.getGameTime();
        this.n = null;
        this.e = null;
        if (old instanceof Edge)
        {
            final Edge edge = (Edge) old;
            Node next = edge.node1;
            if (next.shouldDig(time))
            {
                this.n = next;
                this.e = null;
                if (PokecubeMod.debug) PokecubeCore.LOGGER.debug("Switching to a node 1 " + this.n);
                return true;
            }
            next = edge.node2;
            if (next.shouldDig(time))
            {
                this.n = next;
                this.e = null;
                if (PokecubeMod.debug) PokecubeCore.LOGGER.debug("Switching to a node 2 " + this.n);
                return true;
            }
        }
        else if (old instanceof Node)
        {
            final Node node = (Node) old;
            Edge next = null;
            for (final Edge e : node.edges)
                if (e.shouldDig(time))
                {
                    next = e;
                    break;
                }
            if (next != null)
            {
                this.n = null;
                this.e = next;
                if (PokecubeMod.debug) PokecubeCore.LOGGER.debug("Switching to an edge 1 " + this.e);
                return true;
            }
        }
        // Try to find another open node or edge
        for (final Node n : this.nest.hab.rooms.allRooms)
            if (n.shouldDig(time))
            {
                this.n = n;
                if (PokecubeMod.debug) PokecubeCore.LOGGER.debug("Switching to a node 3 " + this.n);
                return true;
            }
        for (final Edge e : this.nest.hab.rooms.allEdges)
            if (e.shouldDig(time))
            {
                this.e = e;
                if (PokecubeMod.debug) PokecubeCore.LOGGER.debug("Switching to an edge 2 " + e);
                return true;
            }
        return false;
    }

    @Override
    protected boolean selectJobSite()
    {
        final boolean edge = this.e != null;
        dig_select:
        if (this.work_pos == null && (this.progressTimer % 5 == 0 || this.progressTimer < 0))
        {
            final Part part = edge ? this.e : this.n;
            if (PokecubeMod.debug) PokecubeCore.LOGGER.debug("Selecting dig site: " + part);
            if (this.digPart(part))
            {
                if (PokecubeMod.debug) PokecubeCore.LOGGER.debug("Selected dig site");
                break dig_select;
            }
            if (PokecubeMod.debug) PokecubeCore.LOGGER.debug("Site No Task!");
            if (!this.divert(part))
            {
                if (PokecubeMod.debug) PokecubeCore.LOGGER.debug("Job Done!");
                this.endTask();
            }
            return false;
        }
        return this.work_pos != null;
    }

    @Override
    protected void doWork()
    {
        final boolean dug = this.tryHarvest(this.work_pos, true);
        if (dug && this.n != null) this.n.dug.add(this.work_pos.toImmutable());
    }
}
