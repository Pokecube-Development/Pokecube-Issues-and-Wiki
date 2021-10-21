package thut.bot.entity.map;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.INBTSerializable;

public class Tree implements INBTSerializable<CompoundTag>
{
    public static enum NodeType
    {
        NODE;
    }

    public Map<NodeType, List<Node>> rooms = Maps.newHashMap();

    public Map<BlockPos, Node> map = Maps.newHashMap();

    public Map<UUID, Part> allParts = Maps.newHashMap();

    // This list has order of when the rooms were added.
    public List<Node> allRooms = Lists.newArrayList();
    public Set<Edge>  allEdges = Sets.newHashSet();

    public AABB bounds;

    long regionSetTimer = 0;

    public Tree()
    {
        for (final NodeType room : NodeType.values())
            this.rooms.put(room, Lists.newArrayList());
    }

    public List<Node> get(final NodeType type)
    {
        return this.rooms.get(type);
    }

    @Override
    public CompoundTag serializeNBT()
    {
        final CompoundTag tag = new CompoundTag();
        final ListTag list = new ListTag();
        this.allRooms.forEach(n ->
        {
            final CompoundTag nbt = n.serializeNBT();
            list.add(nbt);
        });
        tag.put("map", list);
        return tag;
    }

    @Override
    public void deserializeNBT(final CompoundTag tag)
    {
        for (final NodeType room : NodeType.values())
            this.rooms.put(room, Lists.newArrayList());
        this.allRooms.clear();
        this.map.clear();
        this.allEdges.clear();

        // First we de-serialize the nodes, and stuff them in
        // loadedNodes, After we will need to sort through this, and
        // re-connect the edges accordingly
        final ListTag list = tag.getList("map", 10);
        for (int i = 0; i < list.size(); ++i)
        {
            final CompoundTag nbt = list.getCompound(i);
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
                            this.allParts.remove(e.id);
                            if (e.getBuildBounds().size() > edge.getBuildBounds().size())
                            {
                                edge.getBuildBounds().clear();
                                edge.getBuildBounds().addAll(e.getBuildBounds());
                                edge.digInd = e.digInd;
                            }
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
                            this.allParts.remove(e.id);
                            if (e.getBuildBounds().size() > edge.getBuildBounds().size())
                            {
                                edge.getBuildBounds().clear();
                                edge.getBuildBounds().addAll(e.getBuildBounds());
                                edge.digInd = e.digInd;
                            }
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

    public void add(final Node node)
    {
        final BlockPos mid = node.getCenter();
        if (this.map.containsKey(mid))
        {
            this.allRooms.removeIf(n ->
            {
                if (n.getCenter().equals(mid))
                {
                    this.allParts.remove(n.id);
                    n.edges.forEach(e -> this.allParts.remove(e.id));
                    return true;
                }
                return false;
            });
            this.rooms.forEach((r, l) -> l.removeIf(n ->
            {
                if (n.getCenter().equals(mid))
                {
                    this.allParts.remove(n.id);
                    n.edges.forEach(e -> this.allParts.remove(e.id));
                    return true;
                }
                return false;
            }));
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

    public AABB getBounds()
    {
        if (this.bounds == null) return AABB.ofSize(Vec3.ZERO, 0, 0, 0);
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
