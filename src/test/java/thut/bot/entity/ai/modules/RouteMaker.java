package thut.bot.entity.ai.modules;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.Lists;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import thut.api.Tracker;
import thut.api.terrain.StructureManager;
import thut.api.terrain.StructureManager.StructureInfo;
import thut.bot.ThutBot;
import thut.bot.entity.BotPlayer;
import thut.bot.entity.ai.BotAI;
import thut.bot.entity.ai.modules.map.Edge;
import thut.bot.entity.ai.modules.map.Node;
import thut.bot.entity.ai.modules.map.Part;
import thut.bot.entity.ai.modules.map.Tree;

@BotAI(key = "thutbot:routes")
public class RouteMaker extends AbstractBot
{

    // Counters for when to give up, etc
    int stuckTicks = 0;
    int pathTicks = 0;

    // Current target node
    Node targetNode = null;
    // Current edge to follow
    Edge currentEdge = null;
    // Map of all nodes and edges
    Tree map = null;
    // Maximum number of nodes to put in the map.
    public int maxNodes = 16;

    public ResourceLocation target = null;

    RoadBuilder road_maker;

    public RouteMaker(final BotPlayer player)
    {
        super(player);
        road_maker = new RoadBuilder(player);
    }

    private void updateTargetMemory(final BlockPos target)
    {
        double dx, dz, dh2;
        for (final Part p : this.getMap().allParts.values())
        {
            if (!(p instanceof final Node n)) continue;
            final BlockPos mid = n.getCenter();
            dx = mid.getX() - target.getX();
            dz = mid.getZ() - target.getZ();

            dh2 = dx * dx + dz * dz;
            if (dh2 < n.size)
            {
                n.setCenter(target, n.size);
                for (final Edge e : n.edges) e.setEnds(e.node1.getCenter(), e.node2.getCenter());
            }
        }
        getTag().put("tree_map", this.getMap().serializeNBT());
    }

    private void endTarget()
    {
        final Node n = this.getTargetNode();
        final Edge e = this.getCurrentEdge();
        if (n != null)
        {
            this.updateTargetMemory(n.getCenter());
            final Node other = e.node1 == n ? e.node2 : e.node1;
            other.started = true;
            n.dig_done = Tracker.instance().getTick();
            this.setTargetNode(null);
        }
        if (e != null)
        {
            e.dig_done = Tracker.instance().getTick();
            this.setCurrentEdge(null);
        }
        getTag().put("tree_map", this.getMap().serializeNBT());
    }

    private Tree getMap()
    {
        if (this.map == null)
        {
            final CompoundTag tag = getTag().getCompound("tree_map");
            this.map = new Tree();
            this.map.deserializeNBT(tag);
            if (this.map.nodeCount >= this.maxNodes) return this.map;
        }
        else if (getTag().getBoolean("made_map")) return this.map;

        // If no nodes yet, we start by populating the map with the nearby
        // towns.
        if (this.map.nodeCount < this.maxNodes)
        {
            if (this.player.tickCount % 20 == 0)
            {
                this.player.chat(String.format("Looking for a %s...", this.target));
                this.findNearestVillageNode(this.player.getOnPos(), this.map.nodeCount != 0);
            }
        }
        else
        {
            final List<Node> nodes = Lists.newArrayList();
            this.map.allParts.forEach((i, p) -> {
                if (p instanceof final Node n) nodes.add(n);
            });
            this.map.computeEdges(0.8, 4);
            getTag().putBoolean("made_map", true);
        }
        return null;
    }

    private Edge setCurrentEdge(final Edge e)
    {
        if (e != null) getTag().putUUID("t_edge", e.id);
        else getTag().remove("t_edge");
        return this.currentEdge = e;
    }

    private Edge getCurrentEdge()
    {
        if (this.currentEdge != null && this.currentEdge.dig_done > 0) this.currentEdge = null;
        if (this.currentEdge != null) return this.currentEdge;
        if (getTag().hasUUID("t_edge"))
        {
            final Part p = this.getMap().allParts.get(getTag().getUUID("t_edge"));
            if (p instanceof final Edge e) return this.currentEdge = e;
        }
        else
        {
            final Node n = this.getTargetNode();
            if (n != null)
            {
                final Vec3 here = this.player.position();
                double min = Float.MAX_VALUE;
                Node c = null;
                Edge e_c = null;
                for (final Edge e : n.edges)
                {
                    final Node close = e.node2 == n ? e.node1 : e.node2;
                    final double d = here.distanceTo(close.mid);
                    if (e.dig_done > 0) continue;
                    if (d < min)
                    {
                        min = d;
                        c = close;
                        e_c = e;
                    }
                    if (here.distanceTo(close.mid) < 64) return this.currentEdge = e;
                }

                if (c != null) return this.currentEdge = e_c;
            }
        }
        return this.currentEdge;
    }

    private Node setTargetNode(final Node n)
    {
        if (n != null) getTag().putUUID("t_node", n.id);
        else getTag().remove("t_node");
        return this.targetNode = n;
    }

    private Node findNearestVillageNode(final BlockPos mid, final boolean skipKnownStructures)
    {
        final ResourceLocation location = target;
        final TagKey<ConfiguredStructureFeature<?, ?>> structure = TagKey
                .create(Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY, location);
        final ServerLevel world = (ServerLevel) this.player.level;
        BlockPos village = null;
        if (village == null) village = world.findNearestMapFeature(structure, mid, 50, skipKnownStructures);
        if (village != null && village.getY() < world.getSeaLevel())
        {
            world.getChunk(village);
            village = new BlockPos(village.getX(), world.getHeight(Types.WORLD_SURFACE, village.getX(), village.getZ()),
                    village.getZ());
            if (village.getY() < world.getSeaLevel())
                village = new BlockPos(village.getX(), world.getSeaLevel(), village.getZ());
        }

        final List<Node> nodes = Lists.newArrayList();
        this.map.allParts.forEach((i, p) -> {
            if (p instanceof final Node n) nodes.add(n);
        });
        for (final Node n : nodes)
        {
            final int dr = n.getCenter().atY(0).distManhattan(village);
            if (dr == 0)
            {
                ThutBot.LOGGER.error("Error with duplicate node! {}, {} {}", skipKnownStructures, mid, village);
                return null;
            }
        }
        if (village == null) return null;
        ThutBot.LOGGER.info("Adding node for a structure at: " + village);
        player.chat(String.format("Found a %s at %s", target, village));
        final Node newNode = this.addNode(village);
        return newNode;
    }

    private Node getTargetNode()
    {
        if (this.currentEdge != null)
        {
            if (targetNode == currentEdge.node1)
            {
                targetNode = currentEdge.node2;
            }
            return this.targetNode;
        }
        if (getTag().hasUUID("t_node"))
        {
            final Part p = this.getMap().allParts.get(getTag().getUUID("t_node"));
            if (p instanceof final Node e) return this.setTargetNode(e);
        }
        if (this.getMap().nodeCount >= this.maxNodes)
        {
            final List<Edge> edges = Lists.newArrayList();
            for (final Part o : this.getMap().allParts.values()) if (o instanceof final Edge e) edges.add(e);
            Collections.shuffle(edges);
            for (final Edge e : edges) if (e.getBuildBounds().isEmpty() && e.dig_done <= 0)
            {
                this.setCurrentEdge(e);
                return this.setTargetNode(e.node1);
            }
            this.setCurrentEdge(null);
            return this.setTargetNode(null);
        }
        return this.targetNode;
    }

    private Node addNode(final BlockPos next)
    {
        final ServerLevel world = (ServerLevel) this.player.level;
        int size = 32;
        final Set<StructureInfo> near = StructureManager.getNear(world.dimension(), next, 0);
        final ResourceLocation location = target;
        final IForgeRegistry<StructureFeature<?>> reg = ForgeRegistries.STRUCTURE_FEATURES;
        final StructureFeature<?> structure = reg.getValue(location);
        infos:
        for (final StructureInfo i : near) if (i.start.getFeature().feature == structure)
        {
            size = Math.max(i.start.getBoundingBox().getXSpan(), i.start.getBoundingBox().getZSpan());
            break infos;
        }
        final Node n1 = new Node();
        n1.setCenter(next, size);

        final List<Node> nodes = Lists.newArrayList();
        this.map.allParts.forEach((i, p) -> {
            if (p instanceof final Node n) nodes.add(n);
        });
        final BlockPos o0 = n1.getCenter().atY(0);
        nodes.sort((o1, o2) -> (o1.getCenter().atY(0).distManhattan(o0) - o2.getCenter().atY(0).distManhattan(o0)));
        for (final Node node : nodes)
        {
            final int dist = node.getCenter().atY(0).distManhattan(o0);
            if (dist == 0) return null;
        }
        this.map.add(n1);
        final CompoundTag tag = this.map.serializeNBT();
        getTag().put("tree_map", tag);
        return n1;
    }

    public static final Pattern startPattern = Pattern.compile(START + SPACE + RSRC);
    public static final Pattern startPattern_num = Pattern.compile(START + SPACE + RSRC + SPACE + INT);
    public static final Pattern startPattern_num_speed = Pattern
            .compile(START + SPACE + RSRC + SPACE + INT + SPACE + INT);

    @Override
    public boolean init(String args)
    {
        Matcher match = startPattern_num_speed.matcher(args);

        if (match.find())
        {
            try
            {
                target = new ResourceLocation(match.group(5));
                maxNodes = Integer.parseInt(match.group(7));
                road_maker.tpTicks = Integer.parseInt(match.group(9));
            }
            catch (Exception e)
            {
                player.chat(e.getLocalizedMessage());
            }
        }
        else if ((match = startPattern_num.matcher(args)).find())
        {
            try
            {
                target = new ResourceLocation(match.group(5));
                maxNodes = Integer.parseInt(match.group(7));
            }
            catch (Exception e)
            {
                player.chat(e.getLocalizedMessage());
            }
        }
        else if ((match = startPattern.matcher(args)).find())
        {
            try
            {
                target = new ResourceLocation(match.group(5));
            }
            catch (Exception e)
            {
                player.chat(e.getLocalizedMessage());
            }
        }
        this.getTag().putInt("max_n", this.maxNodes);
        return super.init(args);
    }

    @Override
    public void start(ServerPlayer commander)
    {
        super.start(commander);
        this.maxNodes = this.getTag().getInt("max_n");
    }

    @Override
    public void botTick(final ServerLevel world)
    {
        if (this.getMap() == null) return;

        if (this.player.tickCount % 20 == 0)
        {
            final CompoundTag tag = this.getMap().serializeNBT();
            getTag().put("tree_map", tag);
        }

        Node targetNode = this.getTargetNode();
        Edge traverse = this.getCurrentEdge();

        if (targetNode == null || traverse == null)
        {
            if (this.player.tickCount % 200 == 0)
            {
                Vec3 rand = LandRandomPos.getPos(mob, 32, 8);
                if (rand != null) tryPath(rand);
            }
            return;
        }

        if (this.player.tickCount % 200 == 0)
            player.chat("Bot Builds Roads. " + player.tickCount + " " + this.player.getOnPos());

        // What we need to do:

        /*
         * Check if road_maker is completed, if not, exit early
         * 
         * Check if we have a non-completed edge. If so, assign it to road_maker
         * 
         * If no more non-completed edges, we are done.
         */

        if (road_maker.isCompleted())
        {
            // Find an edge to work on.
            this.endTarget();

            traverse = this.getCurrentEdge();
            if (traverse != null)
            {
                BlockPos e1 = traverse.getEnd1();
                BlockPos e2 = traverse.getEnd2();
                BlockPos next = e1.distManhattan(player.getOnPos()) < e2.distManhattan(player.getOnPos()) ? e1 : e2;
                BlockPos end = next == e1 ? e2 : e1;

                road_maker.done = false;
                road_maker.end = new Vec3(end.getX(), end.getY(), end.getZ());
                road_maker.next = new Vec3(next.getX(), next.getY(), next.getZ());
            }
            else
            {
                player.chat("No Target, I am idle!");
            }
        }
        else
        {
            road_maker.mob = this.mob;
            road_maker.setKey(getKey());
            road_maker.botTick(world);
        }

        if (this.mob.isInWater()) this.mob.setDeltaMovement(this.mob.getDeltaMovement().add(0, 0.05, 0));
        else this.mob.setDeltaMovement(this.mob.getDeltaMovement().add(0, -0.08, 0));
    }
}
