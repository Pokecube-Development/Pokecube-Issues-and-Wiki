package pokecube.core.ai.tasks.ants.nest;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.brain.memory.WalkTarget;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.pathfinding.NodeProcessor;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathFinder;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Region;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.INBTSerializable;
import pokecube.core.ai.pathing.processors.SwimAndWalkNodeProcessor;
import pokecube.core.ai.tasks.ants.AntTasks.AntRoom;
import pokecube.core.world.IPathHelper;

public class Tree implements INBTSerializable<CompoundNBT>, IPathHelper
{
    public Map<AntRoom, List<Node>> rooms = Maps.newHashMap();

    public Map<BlockPos, Node> map = Maps.newHashMap();

    // This list has order of when the rooms were added.
    public List<Node> allRooms = Lists.newArrayList();
    public Set<Edge>  allEdges = Sets.newHashSet();

    public AxisAlignedBB bounds;

    NodeProcessor pather;
    PathFinder    finder;

    long regionSetTimer = 0;

    Region r = null;

    Map<Edge, Path> edgePaths = Maps.newHashMap();

    public Tree()
    {
        for (final AntRoom room : AntRoom.values())
            this.rooms.put(room, Lists.newArrayList());
    }

    public List<Node> get(final AntRoom type)
    {
        return this.rooms.get(type);
    }

    @Override
    public boolean shouldHelpPath(final MobEntity mob, final WalkTarget target)
    {
        final BlockPos from = mob.blockPosition();
        final BlockPos to = target.getTarget().currentBlockPosition();
        if (this.getBounds() == null) return false;
        // TODO also do similar if to is inside, first by pathing to the
        // entrance, then pathing the rest of the way.
        // Also, if a path is found from one node to another, save it in the
        // edgePaths map, and re-use that later if needed.
        return this.getBounds().contains(from.getX(), from.getY(), from.getZ()) && this.getBounds().contains(to.getX(),
                to.getY(), to.getZ());
    }

    @Override
    public Path getPath(final MobEntity mob, final WalkTarget target)
    {
        final ServerWorld world = (ServerWorld) mob.getCommandSenderWorld();
        final BlockPos to = target.getTarget().currentBlockPosition();
        this.pather = new SwimAndWalkNodeProcessor();
        this.pather.setCanPassDoors(true);
        this.finder = new PathFinder(this.pather, 256);

        // TODO also do similar if to is inside, first by pathing to the
        // entrance, then pathing the rest of the way.
        // Also, if a path is found from one node to another, save it in the
        // edgePaths map, and re-use that later if needed.
        if (this.r == null || this.regionSetTimer < world.getGameTime())
        {
            final BlockPos min = new BlockPos(this.bounds.minX, this.bounds.minY, this.bounds.minZ);
            final BlockPos max = new BlockPos(this.bounds.maxX, this.bounds.maxY, this.bounds.maxZ);
            this.r = new Region(world, min, max);
            this.regionSetTimer = world.getGameTime() + 20;
        }
        return this.finder.findPath(this.r, mob, ImmutableSet.of(to), 128, target.getCloseEnoughDist(), 10);
    }

    @Override
    public CompoundNBT serializeNBT()
    {
        final CompoundNBT tag = new CompoundNBT();
        final ListNBT list = new ListNBT();
        this.allRooms.forEach(n ->
        {
            final CompoundNBT nbt = n.serializeNBT();
            list.add(nbt);
        });
        tag.put("map", list);
        return tag;
    }

    @Override
    public void deserializeNBT(final CompoundNBT tag)
    {
        for (final AntRoom room : AntRoom.values())
            this.rooms.put(room, Lists.newArrayList());
        this.allRooms.clear();
        this.map.clear();
        this.allEdges.clear();

        // First we de-serialize the nodes, and stuff them in
        // loadedNodes, After we will need to sort through this, and
        // re-connect the edges accordingly
        final ListNBT list = tag.getList("map", 10);
        for (int i = 0; i < list.size(); ++i)
        {
            final CompoundNBT nbt = list.getCompound(i);
            Node n = new Node();
            n.setTree(this);
            n.deserializeNBT(nbt);
            n = this.map.get(n.getCenter());
            this.add(n);
        }

        // Now we need to re-build the tree from the loaded nodes. Edges
        // need to be replaced such that nodes share the same edges.
        this.allRooms.forEach(n ->
        {
            n.edges.forEach(edge ->
            {
                this.allEdges.add(edge);
                final Node n1 = edge.node1;
                final Node n2 = edge.node2;
                if (n1 != n)
                {
                    final AtomicBoolean had = new AtomicBoolean(false);
                    n1.edges.replaceAll(e ->
                    {
                        if (edge.areSame(e))
                        {
                            had.set(true);
                            return edge;
                        }
                        return e;
                    });
                    if (!had.get()) n1.edges.add(edge);
                }
                if (n2 != n)
                {
                    final AtomicBoolean had = new AtomicBoolean(false);
                    n2.edges.replaceAll(e ->
                    {
                        if (edge.areSame(e))
                        {
                            had.set(true);
                            return edge;
                        }
                        return e;
                    });
                    if (!had.get()) n2.edges.add(edge);
                }
            });
        });
    }

    public boolean shouldCheckBuild(final BlockPos pos, final long time)
    {
        for (final Part part : this.allRooms)
            if (part.shouldCheckBuild(pos, time)) return true;
        for (final Part part : this.allEdges)
            if (part.shouldCheckBuild(pos, time)) return true;
        return false;
    }

    public boolean shouldCheckDig(final BlockPos pos, final long time)
    {
        for (final Part part : this.allRooms)
            if (part.shouldCheckDig(pos, time)) return true;
        for (final Part part : this.allEdges)
            if (part.shouldCheckDig(pos, time)) return true;
        return false;
    }

    public void add(final Node node)
    {
        final BlockPos mid = node.getCenter();
        if (this.map.containsKey(mid))
        {
            this.allRooms.removeIf(n -> n.getCenter().equals(mid));
            this.rooms.forEach((r, l) -> l.removeIf(n -> n.getCenter().equals(mid)));
        }
        this.rooms.get(node.type).add(node);
        this.allRooms.add(node);
        this.map.put(node.getCenter(), node);
        node.setTree(this);
        for (final Edge e : node.edges)
        {
            e.setTree(this);
            e.node1.setTree(this);
            e.node2.setTree(this);
        }
        // Re-initialize the bounds for the rooms, etc
        for (final Part p : this.allRooms)
        {
            p.setInBounds(p.getInBounds());
            p.setOutBounds(p.getOutBounds());
        }
        if (this.bounds == null) this.bounds = node.getOutBounds();
        else this.bounds = this.bounds.minmax(node.getOutBounds());
    }

    public AxisAlignedBB getBounds()
    {
        if (this.bounds == null) return AxisAlignedBB.ofSize(0, 0, 0);
        return this.bounds;
    }

    public Node getEffectiveNode(final BlockPos pos, final Part from)
    {
        if (from instanceof Node) return (Node) from;
        for (final Node room : this.allRooms)
        {
            final BlockPos mid = room.getCenter();
            final boolean onShell = room.isOnShell(pos);
            if (onShell)
            {
                if (pos.getY() == mid.getY() - 1) return room;
                if (from == null) return room;
            }
        }
        if (from instanceof Edge)
        {
            final Edge e = (Edge) from;
            if (e.node2.isOnShell(pos)) return e.node2;
            return e.node1;
        }
        else
        {
            for (final Edge e : this.allEdges)
            {
                if (e.node1.isOnShell(pos)) return e.node1;
                if (e.node2.isOnShell(pos)) return e.node2;
                if (e.isOnShell(pos))
                {
                    final double ds2_1 = e.node1.getCenter().distSqr(pos);
                    final double ds2_2 = e.node2.getCenter().distSqr(pos);
                    return ds2_1 < ds2_2 ? e.node1 : e.node2;
                }
            }
            return null;
        }
    }

    public boolean isInside(final BlockPos pos)
    {
        if (!this.bounds.contains(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5)) return false;
        for (final Node room : this.allRooms)
            if (room.isInside(pos)) return true;
        for (final Edge room : this.allEdges)
            if (room.isInside(pos)) return true;
        return false;
    }

    public boolean isOnShell(final BlockPos pos)
    {
        if (!this.bounds.contains(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5)) return false;
        boolean onShell = false;
        for (final Node room : this.allRooms)
        {
            if (room.isInside(pos)) return false;
            onShell = onShell || room.isOnShell(pos);
        }
        for (final Edge room : this.allEdges)
        {
            if (room.isInside(pos)) return false;
            onShell = onShell || room.isOnShell(pos);
        }
        return onShell;
    }
}
