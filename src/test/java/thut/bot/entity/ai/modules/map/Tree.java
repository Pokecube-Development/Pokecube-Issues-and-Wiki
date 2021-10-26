package thut.bot.entity.ai.modules.map;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
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

    public Map<UUID, Part> allParts = Maps.newHashMap();

    public AABB bounds;

    long regionSetTimer = 0;

    public int nodeCount = 0;

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
        this.allParts.values().forEach(p -> p.saved = false);
        final CompoundTag tag = new CompoundTag();
        final ListTag nodes = new ListTag();
        final ListTag edges = new ListTag();
        this.allParts.forEach((id, p) ->
        {
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
        for (final NodeType room : NodeType.values())
            this.rooms.put(room, Lists.newArrayList());

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

    public void add(final Node node)
    {
        this.rooms.get(node.type).add(node);
        this.nodeCount++;
        node.setTree(this);
        for (final Edge e : node.edges)
        {
            e.setTree(this);
            e.node1.setTree(this);
            e.node2.setTree(this);
        }
        if (this.bounds == null) this.bounds = node.getOutBounds();
        else this.bounds = this.bounds.minmax(node.getOutBounds());
    }

    public AABB getBounds()
    {
        if (this.bounds == null) return AABB.ofSize(Vec3.ZERO, 0, 0, 0);
        return this.bounds;
    }
}
