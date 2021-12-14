package thut.bot.entity.ai.modules.map;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.INBTSerializable;
import thut.bot.ThutBot;

public class Tree implements INBTSerializable<CompoundTag>
{
    public static enum NodeType
    {
        NODE;
    }

    public Map<NodeType, List<Node>> rooms = Maps.newHashMap();

    public Map<UUID, Part> allParts = Maps.newHashMap();

    public AABB bounds;

    long regionSetTimer = 0;

    public int nodeCount = 0;

    public Tree()
    {
        for (final NodeType room : NodeType.values()) this.rooms.put(room, Lists.newArrayList());
    }

    public List<Node> get(final NodeType type)
    {
        return this.rooms.get(type);
    }

    @Override
    public CompoundTag serializeNBT()
    {
        this.allParts.values().forEach(p -> p.saved = false);
        final CompoundTag tag = new CompoundTag();
        final ListTag nodes = new ListTag();
        final ListTag edges = new ListTag();
        this.allParts.forEach((id, p) -> {
            final CompoundTag nbt = p.serializeNBT();
            if (p instanceof Edge) edges.add(nbt);
            else if (p instanceof Node) nodes.add(nbt);
        });
        tag.put("nodes", nodes);
        tag.put("edges", edges);
        return tag;
    }

    @Override
    public void deserializeNBT(final CompoundTag tag)
    {
        for (final NodeType room : NodeType.values()) this.rooms.put(room, Lists.newArrayList());

        this.allParts.clear();
        this.nodeCount = 0;

        // First we de-serialize the nodes, and stuff them in
        // the maps, so when we load edges, they can re-add themselves to the
        // nodes.
        final ListTag nodes = tag.getList("nodes", 10);
        for (int i = 0; i < nodes.size(); ++i)
        {
            final CompoundTag nbt = nodes.getCompound(i);
            final Node n = new Node();
            n.setTree(this);
            n.deserializeNBT(nbt);
            this.rooms.get(n.type).add(n);
            this.allParts.put(n.id, n);
            this.nodeCount++;
        }
        final ListTag edges = tag.getList("edges", 10);
        for (int i = 0; i < edges.size(); ++i)
        {
            final CompoundTag nbt = edges.getCompound(i);
            final Edge n = new Edge();
            n.setTree(this);
            n.deserializeNBT(nbt);
            this.allParts.put(n.id, n);
        }
    }

    public void clearEdges(final Node node)
    {
        final List<Edge> edges = Lists.newArrayList(node.edges);
        for (final Edge e : edges)
        {
            e.node1.edges.remove(e);
            e.node2.edges.remove(e);
            this.allParts.remove(e.id);
        }
    }

    /**
     * Computes edges for the tree, attempting to create @param targetEdges for
     * each node. Also ensures that no edges for a not are closer together
     * than @param maxCosTheta
     * 
     * @param maxCosTheta - metric of too close for a node
     * @param targetEdges - number of nodes to attempt to connect to
     */
    public void computeEdges(double maxCosTheta, int targetEdges)
    {
        List<Node> nodes = Lists.newArrayList();
        this.allParts.forEach((i, p) -> {
            if (p instanceof Node n)
            {
                clearEdges(n);
                nodes.add(n);
            }
        });
        List<Node> options = Lists.newArrayList(nodes);

        for (Node n : nodes)
        {
            // At y=0, as we currently only care about horizontal routes
            BlockPos o0 = n.getCenter().atY(0);
            options.sort((o1, o2) -> (int) (o1.getCenter().atY(0).distSqr(o0) - o2.getCenter().atY(0).distSqr(o0)));

            List<Node> skipped = Lists.newArrayList();

            int j = 0;
            // Index 0 should be here, so start at 1
            options:
            for (int i = 1; i < options.size() && j < targetEdges; i++)
            {
                Node n1 = options.get(i);
                if (n1 == n) throw new IllegalStateException();

                // Target already has enough ends
                if (n1.edges.size() > targetEdges)
                {
                    skipped.add(n1);
                    continue;
                }

                BlockPos dr = n1.getCenter().atY(0).subtract(o0);
                // Reference vector for direction of the road
                Vec3 d1 = new Vec3(dr.getX(), dr.getY(), dr.getZ()).normalize();

                for (Edge e : n.edges)
                {
                    dr = e.node1.getCenter().atY(0).subtract(e.node2.getCenter().atY(0));
                    Vec3 d2 = new Vec3(dr.getX(), dr.getY(), dr.getZ()).normalize();
                    double dot = d2.dot(d1);

                    // Already have an edge that goes too close to there!
                    if (Math.abs(dot) > maxCosTheta)
                    {
                        continue options;
                    }
                }

                Edge e = new Edge();
                e.node1 = n;
                e.node2 = n1;
                e.setEnds(n.getCenter(), n1.getCenter());

                this.allParts.forEach((id, p) -> {
                    if (p instanceof Edge e1)
                    {
                        if (((Edge) p).areSame(e)) throw new IllegalStateException("How are edges same???");
                    }
                });

                e.setTree(this);
                n.edges.add(e);
                n1.edges.add(e);

                j++;
            }

            // always at least 1 edge, to nearest one if we skipped one.
            ensure_edge:
            if (j == 0 && skipped.size() > 0 && n.edges.isEmpty())
            {
                Node n1 = skipped.get(0);
                Edge e = new Edge();
                e.node1 = n;
                e.node2 = n1;
                e.setEnds(n.getCenter(), n1.getCenter());

                AtomicBoolean duped = new AtomicBoolean(false);

                this.allParts.forEach((id, p) -> {
                    if (p instanceof Edge e1)
                    {
                        if (duped.get()) return;
                        if (((Edge) p).areSame(e)) duped.set(false);
                    }
                });

                // If we are duped edge, it means we did already have a matching
                // edge, so we exit.
                if (duped.get()) break ensure_edge;

                e.setTree(this);
                n.edges.add(e);
                n1.edges.add(e);

                ThutBot.LOGGER.warn("Warning, did not find node edge for {}, so auto mapping to {}", n, n1);
            }
        }
    }

    public void updateEdges(final Node node)
    {
        for (final Edge e : node.edges)
        {
            e.setTree(this);
            e.node1.setTree(this);
            e.node2.setTree(this);
        }
    }

    public void add(final Node node)
    {
        this.rooms.get(node.type).add(node);
        this.nodeCount++;
        node.setTree(this);
        this.updateEdges(node);
        if (this.bounds == null) this.bounds = node.getOutBounds();
        else this.bounds = this.bounds.minmax(node.getOutBounds());
    }

    public AABB getBounds()
    {
        if (this.bounds == null) return AABB.ofSize(Vec3.ZERO, 0, 0, 0);
        return this.bounds;
    }
}
