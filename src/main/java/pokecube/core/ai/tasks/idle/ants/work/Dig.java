package pokecube.core.ai.tasks.idle.ants.work;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import com.google.common.collect.Maps;

import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.pathfinding.Path;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.core.ai.tasks.idle.ants.AbstractWorkTask;
import pokecube.core.ai.tasks.idle.ants.AntTasks;
import pokecube.core.ai.tasks.idle.ants.AntTasks.AntHabitat.Edge;
import pokecube.core.ai.tasks.idle.ants.AntTasks.AntHabitat.Node;
import pokecube.core.ai.tasks.idle.ants.AntTasks.AntJob;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import thut.api.maths.Vector3;

public class Dig extends AbstractWorkTask
{
    private static final Map<MemoryModuleType<?>, MemoryModuleStatus> mems = Maps.newHashMap();
    static
    {
        Dig.mems.put(AntTasks.JOB_INFO, MemoryModuleStatus.VALUE_PRESENT);
    }

    int progressTimer    = 0;
    int progressDistance = 0;

    Node n = null;
    Edge e = null;

    BlockPos            digChoice = null;
    final AtomicInteger valids    = new AtomicInteger(0);

    Predicate<BlockPos> hasEmptySpace = pos -> BlockPos.getAllInBox(pos.add(-1, -1, -1), pos.add(1, 1, 1)).anyMatch(
            p -> this.world.isAirBlock(p));

    public Dig(final IPokemob pokemob)
    {
        super(pokemob, Dig.mems, j -> j == AntJob.DIG);
    }

    @Override
    public void reset()
    {
        this.progressTimer = 0;
        this.progressDistance = 0;
        this.n = null;
        this.e = null;
        this.digChoice = null;
        this.valids.set(0);
        final Brain<?> brain = this.entity.getBrain();
        brain.removeMemory(AntTasks.WORK_POS);
        brain.removeMemory(AntTasks.JOB_INFO);
        brain.setMemory(AntTasks.NO_WORK_TIME, -20);
    }

    private void endTask(final boolean completed)
    {
        if (PokecubeMod.debug) PokecubeCore.LOGGER.debug("Need New Dig Site " + this.progressTimer);
        if (this.progressTimer > 300) this.entity.getBrain().setMemory(AntTasks.GOING_HOME, true);
        if (completed)
        {
            if (this.n != null) this.n.dig_done = this.world.getGameTime() + 2400;
            if (this.e != null) this.e.dig_done = this.world.getGameTime() + 2400;
        }
        this.reset();
    }

    private boolean checkJob()
    {
        final Brain<?> brain = this.entity.getBrain();

        boolean edge = this.e != null;
        boolean node = this.n != null;
        if (!(edge || node))
        {
            final CompoundNBT tag = brain.getMemory(AntTasks.JOB_INFO).get();
            edge = tag.getString("type").equals("edge");
            node = tag.getString("type").equals("node");
            final CompoundNBT data = tag.getCompound("data");
            if (edge)
            {
                this.e = new Edge();
                this.e.deserializeNBT(data);

                if (this.e.node1 == null || this.e.node2 == null)
                {
                    tag.remove("type");
                    tag.remove("data");
                    PokecubeCore.LOGGER.error("Corrupted Dig Edge Info!");
                    this.reset();
                    return false;
                }
                this.e.node1 = this.nest.hab.rooms.roomMap.get(this.e.node1.getCenter());
                this.e.node2 = this.nest.hab.rooms.roomMap.get(this.e.node2.getCenter());
            }
            if (node)
            {
                this.n = new Node();
                try
                {
                    this.n.deserializeNBT(data);
                    this.n = this.nest.hab.rooms.roomMap.get(this.n.getCenter());
                }
                catch (final Exception e1)
                {
                    e1.printStackTrace();
                    tag.remove("type");
                    tag.remove("data");
                    PokecubeCore.LOGGER.error("Corrupted Dig Node Info!");
                    this.reset();
                    return false;
                }
            }
        }
        if (!(edge || node))
        {
            PokecubeCore.LOGGER.debug("Invalid Dig Info!");
            this.reset();
            return false;
        }
        return true;
    }

    private boolean tryLine(final BlockPos start, final BlockPos end, final Edge e)
    {
        final Vector3 x0 = Vector3.getNewVector();
        final Vector3 dr = Vector3.getNewVector();
        x0.set(start);
        dr.set(end).subtractFrom(x0);
        final double r = dr.mag();
        dr.scalarMultBy(1 / r);
        if ((int) e.digged >= (int) r)
        {
            final boolean node1 = e.node1.started;
            e.digged = 0.0;
            final Node next = node1 ? e.node2 : e.node1;
            next.started = true;
            e.dig_done = this.world.getGameTime() + 2400;
            return false;
        }
        final Vector3 tmp = Vector3.getNewVector();
        for (double i = 0; i <= r; i++)
        {
            e.digged = i;
            x0.set(start).addTo(dr.x * i, dr.y * i, dr.z * i);
            if (x0.sameBlock(tmp)) continue;
            tmp.set(x0);
            BlockPos pos = x0.getPos();
            // final boolean hasRoom = this.hasEmptySpace.test(pos);
            // if (!hasRoom)
            // {
            // e.digged = 0;
            // return false;
            // }
            final BlockPos min = pos.add(-2, -2, -2);
            final BlockPos max = pos.add(2, 2, 2);
            final Optional<BlockPos> valid = BlockPos.getAllInBox(min, max).filter(p -> this.diggable.test(this.world
                    .getBlockState(p)) && e.isInside(p)).findAny();
            if (!valid.isPresent()) continue;
            pos = valid.get();
            this.digChoice = pos.toImmutable();
            this.progressTimer = -60;
            return true;
        }

        final boolean node1 = e.node1.started;
        e.digged = 0.0;
        final Node next = node1 ? e.node2 : e.node1;
        next.started = true;
        e.dig_done = this.world.getGameTime() + 2400;
        return false;
    }

    private boolean selectJobSite()
    {
        final boolean edge = this.e != null;
        final long time = this.world.getGameTime();
        dig_select:
        if (this.digChoice == null && this.progressTimer % 5 == 0)
        {
            if (PokecubeMod.debug) PokecubeCore.LOGGER.debug("Selecting dig site");
            if (edge)
            {
                if (!this.e.shouldDig(time))
                {
                    this.e.node1.started = true;
                    this.e.node2.started = true;
                    Node next = this.e.node1;
                    if (next.shouldDig(time))
                    {
                        this.n = next;
                        this.e = null;
                        if (PokecubeMod.debug) PokecubeCore.LOGGER.debug("Switching to a node 1 " + this.n.type);
                        return false;
                    }
                    next = this.e.node2;
                    if (next.shouldDig(time))
                    {
                        this.n = next;
                        this.e = null;
                        if (PokecubeMod.debug) PokecubeCore.LOGGER.debug("Switching to a node 2 " + this.n.type);
                        return false;
                    }
                    this.e = null;
                    // Try to find another open node or edge
                    for (final Node n : this.nest.hab.rooms.allRooms)
                        if (n.shouldDig(time))
                        {
                            this.n = n;
                            if (PokecubeMod.debug) PokecubeCore.LOGGER.debug("Switching to a node 3 " + this.n.type);
                            return false;
                        }
                    for (final Edge e : this.nest.hab.rooms.allEdges)
                        if (e.shouldDig(time))
                        {
                            this.e = e;
                            if (PokecubeMod.debug) PokecubeCore.LOGGER.debug("Switching to an edge 2");
                            return false;
                        }
                    this.endTask(false);
                    return false;
                }
                final boolean node1 = this.e.node1.started;
                final BlockPos start = node1 ? this.e.end1 : this.e.end2;
                final BlockPos end = node1 ? this.e.end2 : this.e.end1;
                this.valids.set(0);
                if (this.tryLine(start, end, this.e)) break dig_select;
                // None were valid to dig, so mark as done.
                if (this.valids.get() == 0 && this.e != null) this.e.dig_done = time + 2400;
            }
            else
            {
                if (!this.n.shouldDig(time))
                {
                    Edge next = null;
                    for (final Edge e : this.n.edges)
                    {
                        e.started = true;
                        if (e.shouldDig(time)) next = e;
                    }
                    if (next != null)
                    {
                        this.n = null;
                        this.e = next;
                        if (PokecubeMod.debug) PokecubeCore.LOGGER.debug("Switching to an edge 1");
                        return false;
                    }
                    this.endTask(false);
                    return false;
                }
                final AxisAlignedBB box = new AxisAlignedBB(this.n.getCenter().add(-4, -1, -4), this.n.getCenter().add(
                        4, 3, 4));
                this.valids.set(0);
                // Start with a check of if the pos is inside.
                Predicate<BlockPos> isValid = pos -> this.n.isInside(pos);
                // If it is inside, and not diggable, we notify the node of the
                // dug spot, finally we check if there is space nearby to stand.
                isValid = isValid.and(pos ->
                {
                    if (this.diggable.test(this.world.getBlockState(pos)))
                    {
                        this.valids.getAndIncrement();
                        return true;
                    }
                    this.n.dug.add(pos.toImmutable());
                    return false;
                }).and(this.hasEmptySpace);

                final Optional<BlockPos> pos = BlockPos.getAllInBox(box).filter(isValid).findAny();
                if (pos.isPresent())
                {
                    this.digChoice = pos.get();
                    this.progressTimer = -600;
                    break dig_select;
                }
                // None were valid to dig, so mark as done.
                if (this.valids.get() == 0) this.n.dig_done = time + 2400;
            }
        }
        this.progressTimer++;
        final Brain<?> brain = this.entity.getBrain();
        if (this.digChoice == null)
        {
            final Optional<GlobalPos> room = brain.getMemory(AntTasks.WORK_POS);
            if (!brain.hasMemory(MemoryModules.WALK_TARGET))
            {
                final double dist = room.get().getPos().distanceSq(this.entity.getPosition());
                this.pokemob.setPokemonNickname(this.job + " PATH! " + dist);
                if (dist > 3) this.setWalkTo(room.get().getPos(), 1, 1);
            }
            // We give up
            if (this.progressTimer > 400) this.endTask(true);
        }
        else
        {
            final GlobalPos pos = GlobalPos.getPosition(this.world.getDimensionKey(), this.digChoice);
            brain.setMemory(AntTasks.WORK_POS, pos);
        }
        return this.digChoice != null;
    }

    @Override
    public void run()
    {
        this.pokemob.setPokemonNickname(this.job + " IDLE");
        if (!this.checkJob()) return;
        this.pokemob.setPokemonNickname(this.job + " VALID " + (this.e != null ? "EDGE" : "NODE"));
        if (!this.selectJobSite()) return;
        this.pokemob.setPokemonNickname(this.job + " DIG!");

        final BlockPos pos = this.digChoice;
        final double dr = pos.distanceSq(this.entity.getPosition());
        if (dr > 25)
        {
            if (!this.entity.getBrain().hasMemory(MemoryModules.WALK_TARGET)) this.setWalkTo(pos, 1, 2);
            // Here we should try to walk to the node if too far, so should do
            // some tree analysis to determine pathing to that, to speed up the
            // checks that the vanilla stuff does, which gets confused above
            // ground.

            if (this.progressTimer > 100 && this.entity.getNavigator().hasPath())
            {
                final Path p = this.entity.getNavigator().getPath();
                final BlockPos targ = p.getTarget();
                final BlockPos end = p.getFinalPathPoint().func_224759_a();
                final int dist = end.manhattanDistance(targ);
                if (dist > 1)
                {
                    BlockPos p2 = null;
                    if (this.n != null)
                    {
                        p2 = this.n.getCenter();
                        for (final Edge e : this.n.edges)
                        {
                            if (e.started && e.node1 == this.n)
                            {
                                p2 = e.end1;
                                break;
                            }
                            if (e.started && e.node2 == this.n)
                            {
                                p2 = e.end2;
                                break;
                            }
                        }
                    }
                    else if (this.e != null) if (this.e.node1.started) p2 = this.e.end1;
                    else if (this.e.node2.started) p2 = this.e.end2;
                    if (p2 != null)
                    {
                        this.setWalkTo(p2, 1, 2);
                        this.entity.getBrain().setMemory(AntTasks.WORK_POS, GlobalPos.getPosition(this.world
                                .getDimensionKey(), p2));
                        if (this.progressTimer > 300)
                        {
                            this.entity.setPosition(p2.getX(), p2.getY(), p2.getZ());
                            this.progressTimer = 0;
                        }
                    }
                }
                if (this.progressDistance == 0) this.progressDistance = dist;
            }
        }
        this.progressTimer = 0;
        final boolean dug = this.tryHarvest(pos, true);
        if (dug && this.n != null) this.n.dug.add(this.digChoice.toImmutable());
        this.digChoice = null;
    }
}
