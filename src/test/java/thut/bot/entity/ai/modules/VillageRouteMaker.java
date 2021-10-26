package thut.bot.entity.ai.modules;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.math.Vector3f;

import net.minecraft.commands.arguments.EntityAnchorArgument.Anchor;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import pokecube.core.PokecubeCore;
import pokecube.core.utils.EntityTools;
import pokecube.core.world.gen.jigsaw.CustomJigsawStructure;
import pokecube.core.world.terrain.PokecubeTerrainChecker;
import thut.api.Tracker;
import thut.api.terrain.StructureManager;
import thut.api.terrain.StructureManager.StructureInfo;
import thut.bot.entity.ai.IBotAI;
import thut.bot.entity.ai.helper.PathMob;
import thut.bot.entity.ai.modules.map.Edge;
import thut.bot.entity.ai.modules.map.Node;
import thut.bot.entity.ai.modules.map.Part;
import thut.bot.entity.ai.modules.map.Tree;
import thut.core.common.ThutCore;

public class VillageRouteMaker implements IBotAI
{
    // The bot that goes with this routemaker
    private final ServerPlayer player;

    // The mob used for pathfinding
    private PathfinderMob mob = null;

    // Counters for when to give up, etc
    int stuckTicks = 0;
    int pathTicks  = 0;

    int misc_1 = 0;
    int misc_2 = 0;

    // Current target node
    Node targetNode = null;
    // Current edge to follow
    Edge currentEdge = null;
    // Map of all nodes and edges
    Tree map = null;
    // Maximum number of nodes to put in the map.
    public int maxNodes = 16;

    public VillageRouteMaker(final ServerPlayer player)
    {
        this.player = player;
    }

    /**
     * Here we will teleport the botplayer to a location. This will also
     * transfer all spectators as well.
     *
     * @param tpTo
     *            pos to teleport botplayer to
     */
    private void telePlayer(final BlockPos tpTo)
    {
        // Collect and remove the spectators, prevent them from spectating while
        // this transfer is done.
        final List<ServerPlayer> readd = Lists.newArrayList();
        for (final ServerPlayer player : ((ServerLevel) this.player.level).players())
            if (player.getCamera() == this.player && player != this.player)
            {
                player.setCamera(player);
                player.teleportTo(tpTo.getX(), tpTo.getY(), tpTo.getZ());
                readd.add(player);
            }
        // Move us to the nearest village to the target.
        this.player.teleportTo(tpTo.getX(), tpTo.getY(), tpTo.getZ());

        // Re-add the specators
        for (final ServerPlayer player : readd)
            player.setCamera(this.player);

        // Transfer the pathing mob as well.
        EntityTools.copyPositions(this.mob, this.player);
        EntityTools.copyRotations(this.mob, this.player);
        EntityTools.copyEntityTransforms(this.mob, this.player);
    }

    /**
     * Builds a route starting from start, in direction dir.
     *
     * @param start
     *            - where to start building
     * @param dir
     *            - direction to build in
     * @param dist
     *            - distance to build for
     */
    private void buildRoute(final Vec3 start, final Vec3 dir, final double dist)
    {
        final List<BlockState> paths = Lists.newArrayList(
        // @formatter:off
                Blocks.DIRT_PATH.defaultBlockState()
        );
        // @formatter:on

        final ServerLevel level = (ServerLevel) this.player.level;

        // Returns the valid blocks to replace below with
        final Function<BlockPos, BlockState> below = p ->
        {
            final FluidState fluid = level.getFluidState(p);
            final BlockState b = level.getBlockState(p);
            final boolean sea = p.getY() <= level.getSeaLevel();
            // Over sea level water, we place planks
            if (fluid.is(FluidTags.WATER) && sea) return Blocks.OAK_PLANKS.defaultBlockState();
            // But if replacing through water, we just replace with air
            else if (fluid.is(FluidTags.WATER)) return Blocks.AIR.defaultBlockState();
            // Lave is replaced with cobble
            else if (fluid.is(FluidTags.LAVA)) return Blocks.COBBLESTONE.defaultBlockState();
            // air with planks
            else if (b.isAir()) return Blocks.OAK_PLANKS.defaultBlockState();
            for (final BlockState block : paths)
                if (block.getBlock() == b.getBlock()) return Blocks.STRUCTURE_VOID.defaultBlockState();
            return paths.get(this.player.getRandom().nextInt(paths.size()));
        };

        final BiFunction<BlockState, BlockPos, Boolean> canEdit = (s, p) ->
        {
            final BlockState up = level.getBlockState(p.above());
            if (up.getBlock() == Blocks.TORCH) return false;
            return PokecubeTerrainChecker.isTerrain(s) || PokecubeTerrainChecker.isRock(s) || s
                    .getBlock() == Blocks.WATER || s.getBlock() == Blocks.DIRT_PATH || s.is(BlockTags.ICE) || s.isAir();
        };

        final BiFunction<BlockState, BlockPos, Boolean> shouldRemove = (s, p) ->
        {
            return PokecubeTerrainChecker.isLeaves(s) || PokecubeTerrainChecker.isWood(s);
        };

        BlockPos pos;
        Vec3 vec = start;

        final Vector3f up = Vector3f.YP;
        final Vector3f dr = new Vector3f(dir);
        final Vector3f r_h = up.copy();
        // This is horizontal direction to the path.
        r_h.cross(dr);
        final Vec3 dir_h = new Vec3(r_h);

        // This ensures that we don't double place. The loop below is +=0.25 as
        // a lazy way to account for corners, etc.
        final Set<BlockPos> done = Sets.newHashSet();

        // Loop down the path
        for (double i = 0; i < dist; i += 0.25)
        {
            vec = start.add(dir.scale(i));
            pos = new BlockPos(vec);

            // If too close to a structure, skip point
            final Set<StructureInfo> inside = StructureManager.getNear(level.dimension(), pos, 3);
            if (!inside.isEmpty()) continue;

            // Make torches every 5 blocks or so.
            final boolean makeTorch = i % 5 == 0;

            // Loop horizontally across the path.
            for (int h = -2; h <= 2; h++)
            {
                vec = start.add(dir.scale(i).add(dir_h.scale(h)));
                pos = new BlockPos(vec);

                // Only do each column once, this ensures that.
                if (!done.add(pos)) continue;

                BlockPos here;
                BlockState state;

                // Ensure there is a cieling if needed.
                here = pos.above(4);
                // First check if not air, then also check if maybe is a tree,
                // in that case, no need to place the roof.
                if (!(state = level.getBlockState(here)).isAir() && !shouldRemove.apply(state, here))
                {
                    here = pos.above(3);
                    state = level.getBlockState(here);
                    final boolean editable = canEdit.apply(state, here);
                    // for now we just place cobble, can decide on something
                    // better later.
                    if (editable) level.setBlock(here, Blocks.COBBLESTONE.defaultBlockState(), 2);
                }

                // Edges are first and last of this loop
                final boolean onEdge = Math.abs(h) >= 2;
                // Edges will occasionally have a torch, but otherwise be at
                // ground level
                if (onEdge)
                {
                    // Handle building the edge
                    for (int y = -1; y <= 2; y++)
                    {
                        here = pos.above(y);
                        state = level.getBlockState(here);
                        final boolean editable = canEdit.apply(state, here);
                        final boolean remove = y > 0 && shouldRemove.apply(state, here);
                        // Clear the area above the edge
                        if (remove) level.removeBlock(here, false);
                        // Otherwise put a cobblestone "wall" there
                        else if (y <= 0 && editable)
                        {
                            state = Blocks.COBBLESTONE.defaultBlockState();
                            level.setBlock(here, state, 2);
                        }
                    }
                    // Place a stone brick wall, and a torch on top of that.
                    if (makeTorch)
                    {
                        level.setBlock(pos.above(1), Blocks.COBBLESTONE_WALL.defaultBlockState(), 2);
                        level.setBlock(pos.above(2), Blocks.TORCH.defaultBlockState(), 2);
                    }
                }
                // Otherwise make the base path, and clear area above it.
                else for (int y = -1; y <= 2; y++)
                {
                    here = pos.above(y);
                    state = level.getBlockState(here);
                    final boolean editable = canEdit.apply(state, here);
                    final boolean remove = shouldRemove.apply(state, here);
                    if (y >= 0 && (remove || editable)) level.removeBlock(here, false);
                    else if ((y < 0 && editable) || (y <= 0 && remove))
                    {
                        state = below.apply(here);
                        if (state.getBlock() != Blocks.STRUCTURE_VOID) level.setBlock(here, state, 2);
                    }
                }
            }
        }
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
                for (final Edge e : n.edges)
                    e.setEnds(e.node1.getCenter(), e.node2.getCenter());
            }
        }
        this.player.getPersistentData().put("tree_map", this.getMap().serializeNBT());
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
        this.player.getPersistentData().put("tree_map", this.getMap().serializeNBT());
    }

    private Tree getMap()
    {
        if (this.map == null)
        {
            final CompoundTag tag = this.player.getPersistentData().getCompound("tree_map");
            this.map = new Tree();
            this.map.deserializeNBT(tag);
        }
        else return this.map;
        // If no nodes yet, we start by populating the map with the nearby
        // towns.
        if (this.getMap().nodeCount == 0)
        {
            for (int i = 0; i <= this.maxNodes; i++)
                this.findNearestVillageNode(this.player.getOnPos(), i != 0);
            final List<Node> nodes = Lists.newArrayList();
            this.map.allParts.forEach((i, p) ->
            {
                if (p instanceof final Node n) nodes.add(n);
            });
            // First remove all edges for the nodes
            nodes.forEach(n -> this.getMap().clearEdges(n));
            // Next add them back, the delay is so that all edges are properly
            // cleared before adding new ones.
            nodes.forEach(n -> this.addNodeEdges(n));
        }
        return this.map;
    }

    private Edge setCurrentEdge(final Edge e)
    {
        if (e != null) this.player.getPersistentData().putUUID("t_edge", e.id);
        else this.player.getPersistentData().remove("t_edge");
        return this.currentEdge = e;
    }

    private Edge getCurrentEdge()
    {
        if (this.currentEdge != null) return this.currentEdge;
        if (this.player.getPersistentData().hasUUID("t_edge"))
        {
            final Part p = this.getMap().allParts.get(this.player.getPersistentData().getUUID("t_edge"));
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
                    if (!e.getBuildBounds().isEmpty()) continue;
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
        if (n != null) this.player.getPersistentData().putUUID("t_node", n.id);
        else this.player.getPersistentData().remove("t_node");
        return this.targetNode = n;
    }

    private Node findNearestVillageNode(final BlockPos mid, final boolean skipKnownStructures)
    {
        final ResourceLocation location = new ResourceLocation("pokecube:village");
        final IForgeRegistry<StructureFeature<?>> reg = ForgeRegistries.STRUCTURE_FEATURES;
        final StructureFeature<?> structure = reg.getValue(location);
        final ServerLevel world = (ServerLevel) this.player.level;
        BlockPos village = null;

        if (!skipKnownStructures)
        {
            final Set<StructureInfo> nearby = StructureManager.getNear(world.dimension(), mid, 0);
            infos:
            for (final StructureInfo i : nearby)
                if (i.start.getFeature() == structure)
                {
                    village = i.start.getLocatePos();
                    break infos;
                }
        }
        if (village == null) village = world.findNearestMapFeature(structure, mid, 50, skipKnownStructures);
        if (village != null && village.getY() < world.getSeaLevel())
        {
            world.getChunk(village);
            village = new BlockPos(village.getX(), world.getHeight(Types.WORLD_SURFACE, village.getX(), village.getZ()),
                    village.getZ());
            if (village.getY() < world.getSeaLevel()) village = new BlockPos(village.getX(), world.getSeaLevel(),
                    village.getZ());
        }

        final List<Node> nodes = Lists.newArrayList();
        this.getMap().allParts.forEach((i, p) ->
        {
            if (p instanceof final Node n) nodes.add(n);
        });
        for (final Node n : nodes)
        {
            final int dr = n.getCenter().atY(0).distManhattan(village);
            if (dr == 0)
            {
                ThutCore.LOGGER.error("Error with duplicate node! {}, {} {}", skipKnownStructures, mid, village);
                return null;
            }
        }
        if (village == null) return null;
        PokecubeCore.LOGGER.info("Adding node for a village at: " + village);
        final Node newNode = this.addNode(village);
        return newNode;
    }

    private Node getTargetNode()
    {
        if (this.currentEdge != null) return this.targetNode;
        if (this.player.getPersistentData().hasUUID("t_node"))
        {
            final Part p = this.getMap().allParts.get(this.player.getPersistentData().getUUID("t_node"));
            if (p instanceof final Node e) return this.setTargetNode(e);
        }

        if (this.getMap().nodeCount >= this.maxNodes)
        {
            final List<Edge> edges = Lists.newArrayList();
            for (final Part o : this.getMap().allParts.values())
                if (o instanceof final Edge e) edges.add(e);
            Collections.shuffle(edges);
            for (final Edge e : edges)
                if (e.getBuildBounds().isEmpty())
                {
                    System.out.println(e.getBuildBounds() + " " + e);
                    this.setCurrentEdge(e);
                    return this.setTargetNode(e.node1);
                }
            this.setCurrentEdge(null);
            return this.setTargetNode(null);
        }

        final Node newNode = this.findNearestVillageNode(BlockPos.ZERO, true);
        if (newNode != null)
        {
            this.setTargetNode(newNode);
            if (this.getTargetNode() == null)
            {
                System.out.println("Null Node!!");
                return null;
            }
            final List<Edge> edges = Lists.newArrayList(this.targetNode.edges);
            Node prev = null;
            Edge running = null;
            final List<Node> options = Lists.newArrayList();
            final Map<UUID, Edge> opts_e = Maps.newHashMap();
            // Check the edges to find some nodes to try to go from.
            for (final Edge e : edges)
            {
                final Node other = e.node1 == this.targetNode ? e.node2 : e.node1;
                if (!other.started) continue;
                options.add(other);
                opts_e.put(other.id, e);
                break;
            }

            // Sort the found nodes, and pick closest one to do.
            if (!options.isEmpty())
            {
                final BlockPos o0 = newNode.getCenter().atY(0);
                options.sort((o1, o2) -> (int) (o1.getCenter().atY(0).distSqr(o0) - o2.getCenter().atY(0).distSqr(o0)));
                prev = options.get(0);
                running = opts_e.get(prev.id);
            }

            if (prev == null || prev.getCenter().getY() == 0)
            {
                ThutCore.LOGGER.error("Warning, no started node connected to us!");
                return this.targetNode;
            }
            if (running != null) running.started = true;

            final BlockPos tpTo = prev.getCenter();
            this.telePlayer(tpTo);
        }
        return this.targetNode;
    }

    private void addNodeEdges(final Node n1)
    {
        final Vec3 mid1 = new Vec3(n1.mid.x, 0, n1.mid.z);
        // Collect the nodes, then sort them by distance from the new one.
        final List<Node> nodes = Lists.newArrayList();
        this.getMap().allParts.forEach((i, p) ->
        {
            // Selects for all the nodes, except the passed in one.
            if (p instanceof final Node n && n != n1) nodes.add(n);
        });
        final BlockPos o0 = n1.getCenter().atY(0);
        // For this step, we only want nodes with few edges already.
        nodes.removeIf(n -> n.edges.size() > 2);
        // This is a fairly quick way to sort by distance
        nodes.sort((o1, o2) -> (o1.getCenter().atY(0).distManhattan(o0) - o2.getCenter().atY(0).distManhattan(o0)));
        for (final Node node : nodes)
        {
            final int dist = node.getCenter().atY(0).distManhattan(o0);
            if (dist == 0) throw new IllegalStateException("Duplicate node added!");
        }

        int n = 0;
        nodes:
        // Add edges to the nearest up to 3 nodes, or until we run out of nodes.
        for (int i = 0; i < nodes.size() && n < 3; i++)
        {
            final Node n2 = nodes.get(i);
            final Vec3 mid2 = new Vec3(n2.mid.x, 0, n2.mid.z);
            final Vec3 dir1_2 = mid2.subtract(mid1).normalize();

            final List<Edge> edges = Lists.newArrayList(n1.edges);

            final Edge e = new Edge();

            e.node1 = n1;
            e.node2 = n2;
            e.setEnds(n1.getCenter(), n2.getCenter());

            // If we have already added an edge to the nearest village, ensure
            // the next villages are not also in too straight of a line.
            for (final Edge e2 : edges)
            {
                final Node n3 = e2.node1 == n1 ? e2.node2 : e2.node1;
                final Vec3 mid3 = new Vec3(n3.mid.x, 0, n3.mid.z);
                final Vec3 dir1_3 = mid3.subtract(mid1).normalize();
                // |v.dot(v)| will give an indication of parallelness
                final double dot = Math.abs(dir1_3.dot(dir1_2));
                // Lets say 0.7 is too parallel.
                if (dot > 0.7 || e.equals(e2))
                {
                    n--;
                    continue nodes;
                }
            }
            n++;
            n1.edges.add(e);
            n2.edges.add(e);
        }
        this.getMap().updateEdges(n1);
    }

    private Node addNode(final BlockPos next)
    {
        final ServerLevel world = (ServerLevel) this.player.level;
        int size = 32;
        final Set<StructureInfo> near = StructureManager.getNear(world.dimension(), next, 0);
        final ResourceLocation location = new ResourceLocation("pokecube:village");
        final IForgeRegistry<StructureFeature<?>> reg = ForgeRegistries.STRUCTURE_FEATURES;
        final StructureFeature<?> structure = reg.getValue(location);
        infos:
        for (final StructureInfo i : near)
            if (i.start.getFeature() == structure)
            {
                size = Math.max(i.start.getBoundingBox().getXSpan(), i.start.getBoundingBox().getZSpan());
                break infos;
            }
        final Node n1 = new Node();
        n1.setCenter(next, size);

        final List<Node> nodes = Lists.newArrayList();
        this.getMap().allParts.forEach((i, p) ->
        {
            if (p instanceof final Node n) nodes.add(n);
        });
        final BlockPos o0 = n1.getCenter().atY(0);
        nodes.sort((o1, o2) -> (o1.getCenter().atY(0).distManhattan(o0) - o2.getCenter().atY(0).distManhattan(o0)));
        for (final Node node : nodes)
        {
            final int dist = node.getCenter().atY(0).distManhattan(o0);
            if (dist == 0) return null;
        }
        this.addNodeEdges(n1);
        this.getMap().add(n1);
        final CompoundTag tag = this.getMap().serializeNBT();
        this.player.getPersistentData().put("tree_map", tag);
        return n1;
    }

    private void findStart()
    {
        final Edge e = this.getCurrentEdge();
        final Node n = this.getTargetNode();
        if (e == null || n == null) return;
        final Node other = e.node1 == n ? e.node2 : e.node1;

        final Vec3 near = other.mid;
        final Vec3 far = n.mid;

        for (int r = 32; r < 96; r += 8)
        {
            final Vec3 dir = far.subtract(near).normalize();
            final int d = 16;
            final double dx = (this.player.getRandom().nextDouble() - 0.5) * d;
            final double dz = (this.player.getRandom().nextDouble() - 0.5) * d;
            final Vec3 randNear = near.add(dir.scale(r)).add(dx, 0, dz);
            BlockPos end = new BlockPos(randNear);
            end = this.player.level.getHeightmapPos(Types.MOTION_BLOCKING_NO_LEAVES, end);
            if (end.getY() < this.player.level.getSeaLevel()) end = new BlockPos(end.getX(), this.player.level
                    .getSeaLevel(), end.getZ());

            final Set<StructureInfo> nearSet = StructureManager.getNear(this.player.level.dimension(), end, 0);
            if (nearSet.isEmpty())
            {
                e.getBuildBounds().add(end);
                return;
            }
        }
    }

    private void directPath()
    {
        final Edge e = this.getCurrentEdge();
        final Node n = this.getTargetNode();

        if (e == null || n == null) return;

        final boolean rev = n == e.node1;

        int nextIndex = e.digInd + 1;
        int prevIndex = e.digInd;

        if (e.getBuildBounds().size() < 1)
        {
            this.findStart();
            return;
        }
        Vec3 vecHere = rev ? e.node2.mid : e.node1.mid;

        if (rev)
        {
            // In this case, we are counting the list backwards
            nextIndex = e.digInd - 1;
            prevIndex = e.digInd;

            if (nextIndex < 0)
            {
                this.computeNextEdgePoint(rev, e, n, vecHere);
                return;
            }
        }
        else if (nextIndex > e.getBuildBounds().size() - 1)
        {
            this.computeNextEdgePoint(rev, e, n, vecHere);
            return;
        }

        if (e.getBuildBounds().size() < 2)
        {
            this.computeNextEdgePoint(rev, e, n, vecHere);
            return;
        }

        final BlockPos prev = e.getBuildBounds().get(prevIndex);
        final BlockPos next = e.getBuildBounds().get(nextIndex);

        final Vec3 targ = new Vec3(next.getX(), next.getY(), next.getZ());
        if (prev == next)
        {
            this.tryPath(targ);
            System.out.println("wat");
            return;
        }

        vecHere = new Vec3(prev.getX(), prev.getY(), prev.getZ());

        if (this.player.distanceToSqr(vecHere) > 9)
        {
            this.tryPath(vecHere);
            this.pathTicks++;

            if (this.pathTicks > 20)
            {
                final BlockPos tpTo = new BlockPos(vecHere);
                this.telePlayer(tpTo);
                this.pathTicks = 0;
            }
            return;
        }

        this.pathTicks = 0;

        final List<ServerPlayer> readd = Lists.newArrayList();
        for (final ServerPlayer player : ((ServerLevel) this.player.level).players())
            if (player.getCamera() == this.player && player != this.player) readd.add(player);

        Vec3 randNear = new Vec3(next.getX(), next.getY(), next.getZ());

        Vec3 dir = randNear.subtract(vecHere);
        double dr = dir.length();

        if (Math.abs(dir.y) > dr * 0.8)
        {
            if (randNear.y > vecHere.y) randNear = new Vec3(next.getX(), prev.getY() + 0.8 * dr, next.getZ());
            else randNear = new Vec3(next.getX(), prev.getY() - 0.8 * dr, next.getZ());
            dir = randNear.subtract(vecHere);
            dr = dir.length();
            e.getBuildBounds().set(nextIndex, new BlockPos(randNear));
        }

        if (dr > 64)
        {
            System.out.println("Error with distance! " + dr + " " + e.getBuildBounds().size() + " " + rev);
            if (e.getBuildBounds().size() < 3) e.getBuildBounds().clear();
            this.endTarget();
            return;
        }
        // if (this.player.tickCount % 20 == 0) System.out.println("Doing Node!
        // " + dr + " " + e.getBuildBounds().size()
        // + " " + rev);

        dir = dir.normalize();
        this.buildRoute(vecHere, dir, dr);
        this.computeNextEdgePoint(rev, e, n, randNear);
    }

    private void computeNextEdgePoint(final boolean rev, final Edge e, final Node n, final Vec3 prev)
    {
        final Node far = rev ? e.node1 : e.node2;
        final Vec3 targ = far.mid;

        final Vec3 dir = targ.subtract(prev).normalize();
        final int d = 16;
        final int r = 32;

        final double dx = (this.player.getRandom().nextDouble() - 0.5) * d;
        final double dz = (this.player.getRandom().nextDouble() - 0.5) * d;

        final Vec3 randNear = prev.add(dir.scale(r)).add(dx, 0, dz);
        BlockPos end = new BlockPos(randNear);

        end = this.player.level.getHeightmapPos(Types.MOTION_BLOCKING_NO_LEAVES, end);
        if (end.getY() < this.player.level.getSeaLevel()) end = new BlockPos(end.getX(), this.player.level
                .getSeaLevel(), end.getZ());

        if (rev)
        {
            e.digInd = 1;
            e.getBuildBounds().add(0, end);
        }
        else
        {
            e.digInd = e.getBuildBounds().size() - 1;
            e.getBuildBounds().add(end);
        }
    }

    private void tryPath(final Vec3 targ)
    {
        final PathNavigation navi = this.mob.getNavigation();
        final BlockPos pos = new BlockPos(targ);

        if (navi.isInProgress() && navi.getTargetPos().closerThan(pos, 1)) return;

        final Path p = navi.createPath(pos, 16, 16);
        if (p != null)
        {
            final double spd = this.player.getPersistentData().getBoolean("pathing") ? 1.2 : 0.6;
            navi.moveTo(p, spd);
        }
    }

    @Override
    public void tick()
    {
        this.player.setGameMode(GameType.CREATIVE);
        this.player.setInvulnerable(true);
        if (ForgeHooks.onLivingUpdate(this.player)) return;
        if (!(this.player.level instanceof final ServerLevel world)) return;

        final PathfinderMob old = this.mob;
        if (this.mob == null) this.mob = new PathMob(world);
        final boolean init = this.mob != old;
        if (init)
        {
            this.mob.setInvulnerable(true);
            this.mob.setInvisible(true);
            EntityTools.copyPositions(this.mob, this.player);
            EntityTools.copyRotations(this.mob, this.player);
            EntityTools.copyEntityTransforms(this.mob, this.player);
        }
        final List<ServerPlayer> readd = Lists.newArrayList();
        for (final ServerPlayer player : world.players())
            if (player.getCamera() == this.player && player != this.player) readd.add(player);

        if (readd.isEmpty())
        {
            EntityTools.copyPositions(this.mob, this.player);
            EntityTools.copyRotations(this.mob, this.player);
            EntityTools.copyEntityTransforms(this.mob, this.player);
            return;
        }

        if (this.player.tickCount % 20 == 0)
        {
            final CompoundTag tag = this.getMap().serializeNBT();
            this.player.getPersistentData().put("tree_map", tag);
        }
        if (this.getMap() == null) return;

        Vec3 targ = this.player.position;

        final Node targetNode = this.getTargetNode();
        final Edge traverse = this.getCurrentEdge();

        if (targetNode == null || traverse == null)
        {
            if (this.player.tickCount % 20 == 0) System.out.println("No Target, I am idle! " + targetNode + " "
                    + traverse);
            return;
        }

        targ = targetNode.mid;

        BlockPos origin = this.player.getOnPos();

        BlockPos hmap = new BlockPos(this.player.getOnPos());
        hmap = world.getHeightmapPos(Types.WORLD_SURFACE, hmap);
        if (hmap.getY() < world.getSeaLevel()) hmap = new BlockPos(hmap.getX(), world.getSeaLevel(), hmap.getZ());
        final double dy = hmap.getY() - this.player.getOnPos().getY();
        final PathNavigation navi = this.mob.getNavigation();

        if (dy > 0) origin = origin.above();

        // This value is "close enough" to the end to give up
        final int d1 = 32;
        // This is too close to a village structure to start path building
        final int d2 = 2;
        // This value is how far to consider too far for normal pathing, and
        // instead will make relatively straight paths.
        final int d3 = 4096;
        final double rr = targ.distanceToSqr(this.player.position);

        final Set<StructureInfo> inside = StructureManager.getNear(this.player.level.dimension(), origin, d2);
        inside.removeIf(i -> !(i.start.getFeature() instanceof CustomJigsawStructure));

        boolean inVillage = false;
        for (final StructureInfo i : inside)
            if (i.name.contains("village")) inVillage = true;

        final boolean doRun = true;

        if (rr < d1 || rr < d3) this.endTarget();
        else if (rr > d3)
        {
            this.player.getPersistentData().putBoolean("pathing", true);
            this.directPath();
        }
        else if (!navi.isInProgress() && rr > d1)
        {
            this.player.getPersistentData().putBoolean("pathing", false);
            final Vec3 prev = this.player.position();
            final Vec3 dir = targ.subtract(prev).normalize();
            final int d = 4;
            final int r = 8;

            final double dx = (this.player.getRandom().nextDouble() - 0.5) * d;
            final double dz = (this.player.getRandom().nextDouble() - 0.5) * d;
            final Vec3 randNear = prev.add(dir.scale(r)).add(dx, 0, dz);

            this.tryPath(randNear);
        }

        this.player.lookAt(Anchor.EYES, targ);

        boolean naviing = false;
        final int diff = 1;
        if (navi.isInProgress())
        {
            naviing = true;
            this.validatePath(targ, rr);
        }

        this.mob.setSilent(true);
        this.mob.maxUpStep = 1.25f;

        // Prevent the mob from having its own ideas of what to do
        this.mob.getBrain().removeAllBehaviors();

        this.mob.setOldPosAndRot();
        this.mob.tickCount = this.player.tickCount;

        if (doRun)
        {
            this.player.getPersistentData().putBoolean("pathing", true);
            this.player.getPersistentData().remove("end_segment");
            this.mob.tick();
            if (this.mob.getNavigation().isInProgress() || inVillage) this.checkStuck(targ, hmap, rr, naviing, diff);
        }

        if (this.mob.isInWater()) this.mob.setDeltaMovement(this.mob.getDeltaMovement().add(0, 0.05, 0));
        else this.mob.setDeltaMovement(this.mob.getDeltaMovement().add(0, -0.08, 0));

        EntityTools.copyEntityTransforms(this.player, this.mob);
        EntityTools.copyPositions(this.player, this.mob);
        EntityTools.copyRotations(this.player, this.mob);

    }

    private void checkStuck(final Vec3 targ, final BlockPos origin, final double rr, final boolean naviing,
            final int dy)
    {
        if (this.player.tickCount % 200 != 0) return;
        final int diff = 2;
        final ServerLevel world = (ServerLevel) this.player.level;
        final double dh = this.mob.position().subtract(this.player.position()).horizontalDistance();

        if (dh < 0.15) this.stuckTicks++;
        else this.stuckTicks = 0;

        int py = world.getHeight(Types.MOTION_BLOCKING_NO_LEAVES, this.mob.getBlockX(), this.mob.getBlockZ());
        py = Math.max(py, world.getSeaLevel());

        if (Math.abs(this.mob.getY() - py) > 10) this.mob.setPos(this.mob.getX(), py, this.mob.getZ());

        if (this.stuckTicks > 1)
        {
            if (this.stuckTicks > 3 && naviing && rr > diff)
            {

                final Vec3 prev = this.player.position();
                final Vec3 dir = targ.subtract(prev).normalize();
                final int d = 4;
                final int r = 8;

                final double dx = (this.player.getRandom().nextDouble() - 0.5) * d;
                final double dz = (this.player.getRandom().nextDouble() - 0.5) * d;

                final Vec3 randNear = targ.add(dir).scale(r).add(dx, 0, dz);
                this.tryPath(randNear);
            }
            if (this.stuckTicks > 10) this.stuckTicks = 0;
            if (this.stuckTicks % 2 == 99)
            {
                this.player.getPersistentData().putBoolean("pathing", false);
                Vec3 pos = LandRandomPos.getPos(this.mob, 5, 5);
                if (this.mob.getNavigation().isInProgress())
                {
                    final Path p = this.mob.getNavigation().getPath();
                    pos = p.getNextEntityPos(this.mob);
                }
                if (pos != null) this.mob.setPos(pos.x, pos.y, pos.z);
                else this.mob.setPos(this.mob.getX(), py, this.mob.getZ());
            }
        }
    }

    private void validatePath(Vec3 targ, final double rr)
    {
        final int diff = 2;
        final PathNavigation navi = this.mob.getNavigation();
        final ServerLevel world = (ServerLevel) this.player.level;
        if (targ.y == 0 && rr < 256 * 256)
        {
            BlockPos village = new BlockPos(targ);
            world.getChunk(village);
            village = new BlockPos(village.getX(), world.getHeight(Types.WORLD_SURFACE, village.getX(), village.getZ()),
                    village.getZ());
            if (village.getY() < world.getSeaLevel()) village = new BlockPos(village.getX(), world.getSeaLevel(),
                    village.getZ());
            targ = new Vec3(village.getX(), village.getY(), village.getZ());
            this.updateTargetMemory(village);
        }

        final BlockPos next = navi.getPath().getNextNodePos();
        final BlockPos here = this.player.getOnPos();
        if (next.getY() <= here.getY() && here.closerThan(next, diff)) navi.getPath().advance();
    }
}
