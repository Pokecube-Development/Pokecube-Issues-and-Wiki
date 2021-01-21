package pokecube.core.ai.tasks.ants.nest;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.entity.MobEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.pathfinding.Path;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.INBTSerializable;
import pokecube.core.ai.tasks.ants.AntTasks.AntRoom;

public class Tree implements INBTSerializable<CompoundNBT>
{
    public Map<AntRoom, List<Node>> rooms = Maps.newHashMap();

    public Map<BlockPos, Node> map = Maps.newHashMap();

    // This list has order of when the rooms were added.
    public List<Node> allRooms = Lists.newArrayList();
    public Set<Edge>  allEdges = Sets.newHashSet();

    public AxisAlignedBB bounds;

    public Tree()
    {
        for (final AntRoom room : AntRoom.values())
            this.rooms.put(room, Lists.newArrayList());
    }

    public List<Node> get(final AntRoom type)
    {
        return this.rooms.get(type);
    }

    public Path getPath(final BlockPos from, final BlockPos to, final ServerWorld world, final MobEntity mob)
    {
        final Path p = mob.getNavigator().getPathToPos(to, 0);
        if (p.reachesTarget()) return p;

        return null;
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

    public void add(final Node node)
    {
        this.rooms.get(node.type).add(node);
        this.allRooms.add(node);
        this.map.put(node.getCenter(), node);
        node.setTree(this);
        for (final Edge e : node.edges)
            e.setTree(this);
        if (this.bounds == null) this.bounds = node.outBounds;
        else this.bounds = this.bounds.union(node.outBounds);
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
