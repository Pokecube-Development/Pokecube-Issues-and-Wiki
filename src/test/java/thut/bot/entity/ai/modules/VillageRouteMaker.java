package thut.bot.entity.ai.modules;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.google.common.collect.Lists;

import net.minecraft.commands.arguments.EntityAnchorArgument.Anchor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
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
import pokecube.core.utils.EntityTools;
import pokecube.core.world.gen.jigsaw.CustomJigsawStructure;
import pokecube.core.world.terrain.PokecubeTerrainChecker;
import thut.api.Tracker;
import thut.api.terrain.StructureManager;
import thut.api.terrain.StructureManager.StructureInfo;
import thut.bot.entity.ai.IBotAI;
import thut.bot.entity.ai.helper.PathMob;
import thut.bot.entity.map.Edge;
import thut.bot.entity.map.Node;
import thut.bot.entity.map.Part;
import thut.bot.entity.map.Tree;
import thut.core.common.ThutCore;

public class VillageRouteMaker implements IBotAI
{
    private final ServerPlayer player;

    private PathfinderMob mob = null;

    int stuckTicks = 0;
    int pathTicks  = 0;

    int misc_1 = 0;
    int misc_2 = 0;

    Node targetNode = null;

    Edge currentEdge = null;

    Tree map = null;

    public VillageRouteMaker(final ServerPlayer player)
    {
        this.player = player;
    }

    private void telePlayer(final BlockPos tpTo)
    {
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

        for (final ServerPlayer player : readd)
            player.setCamera(player);

        EntityTools.copyPositions(this.mob, this.player);
        EntityTools.copyRotations(this.mob, this.player);
        EntityTools.copyEntityTransforms(this.mob, this.player);
    }

    private void buildRoute(final BlockPos origin, final boolean onAir)
    {
        final Set<StructureInfo> inside = StructureManager.getNear(this.player.level.dimension(), origin, 5);

        inside.removeIf(i -> !(i.start.getFeature() instanceof CustomJigsawStructure));

        // inside.clear();

        final List<BlockState> paths = Lists.newArrayList(
        // @formatter:off
                Blocks.COARSE_DIRT.defaultBlockState(),
                Blocks.COBBLESTONE.defaultBlockState(),
                Blocks.DIRT_PATH.defaultBlockState(),
                Blocks.COARSE_DIRT.defaultBlockState(),
                Blocks.COBBLESTONE.defaultBlockState(),
                Blocks.DIRT_PATH.defaultBlockState(),
                Blocks.COARSE_DIRT.defaultBlockState(),
                Blocks.COBBLESTONE.defaultBlockState(),
                Blocks.DIRT_PATH.defaultBlockState(),
                Blocks.COARSE_DIRT.defaultBlockState(),
                Blocks.COBBLESTONE.defaultBlockState(),
                Blocks.DIRT_PATH.defaultBlockState(),
                Blocks.STRUCTURE_VOID.defaultBlockState(),
                Blocks.STRUCTURE_VOID.defaultBlockState(),
                Blocks.STRUCTURE_VOID.defaultBlockState());
        // @formatter:on

        final Function<BlockPos, BlockState> sides_above = p ->
        {
            final FluidState fluid = this.player.level.getFluidState(p);
            if (fluid.is(FluidTags.WATER)) return Blocks.OAK_PLANKS.defaultBlockState();
            else if (fluid.is(FluidTags.LAVA)) return Blocks.COBBLESTONE.defaultBlockState();
            return Blocks.AIR.defaultBlockState();
        };

        final Function<BlockPos, BlockState> below = p ->
        {
            final FluidState fluid = this.player.level.getFluidState(p);
            final BlockState b = this.player.level.getBlockState(p);
            final boolean sea = p.getY() <= this.player.level.getSeaLevel();
            if (fluid.is(FluidTags.WATER) && sea) return Blocks.OAK_PLANKS.defaultBlockState();
            else if (fluid.is(FluidTags.WATER)) return Blocks.AIR.defaultBlockState();
            else if (fluid.is(FluidTags.LAVA)) return Blocks.COBBLESTONE.defaultBlockState();
            else if (b.isAir()) return Blocks.OAK_PLANKS.defaultBlockState();
            for (final BlockState block : paths)
                if (block.getBlock() == b.getBlock()) return Blocks.STRUCTURE_VOID.defaultBlockState();
            return paths.get(this.player.getRandom().nextInt(paths.size()));
        };

        final BiFunction<BlockState, BlockPos, Boolean> valid = (s, p) ->
        {
            final BlockState up = this.player.level.getBlockState(p.above());
            if (up.getBlock() == Blocks.TORCH) return false;
            return PokecubeTerrainChecker.isTerrain(s) || PokecubeTerrainChecker.isRock(s) || s
                    .getBlock() == Blocks.WATER || s.getBlock() == Blocks.DIRT_PATH || s.is(BlockTags.ICE) || s.isAir();
        };

        final BiFunction<BlockState, BlockPos, Boolean> remove = (s, p) ->
        {
            return PokecubeTerrainChecker.isLeaves(s) || PokecubeTerrainChecker.isWood(s);
        };

        if (inside.isEmpty())
        {

            final BlockPos r = origin;
            // First try removing any leaves that are sticking us
            final Iterable<BlockPos> blocks = BlockPos.betweenClosed(r.north(2).east(2).above(3), r.south(2).west(2)
                    .below(3));
            for (final BlockPos b : blocks)
            {
                final BlockState block = this.player.level.getBlockState(b);
                if (PokecubeTerrainChecker.isCutablePlant(block) || PokecubeTerrainChecker.isLeaves(block)
                        || PokecubeTerrainChecker.isWood(block)) this.player.level.destroyBlock(b, false);
            }

            final BlockPos onPos = this.player.getOnPos();
            final BlockState torch = Blocks.TORCH.defaultBlockState();
            if (Block.canSupportCenter(this.player.level, onPos, Direction.UP)) this.player.level.setBlock(onPos
                    .above(), torch, 3);

            final Iterable<BlockPos> iter = BlockPos.betweenClosed(origin.north().east(), origin.south().west());

            iter.forEach(pos ->
            {
                pos = pos.immutable();
                BlockPos p;
                BlockState s, place;
                for (int i = 1; i < 2; i++)
                {
                    p = pos.below(i);
                    s = this.player.level.getBlockState(p);
                    place = below.apply(p);
                    boolean placeGround = (valid.apply(s, p)) && place.getBlock() != Blocks.STRUCTURE_VOID;
                    placeGround = placeGround || (onAir && i < 0);
                    placeGround = (valid.apply(s, p)) && place.getBlock() != Blocks.STRUCTURE_VOID;
                    if (remove.apply(s, p)) this.player.level.removeBlock(p, false);
                    else if (placeGround) this.player.level.setBlock(p, place, 2);
                }
                for (int i = 0; i <= 3; i++)
                {
                    p = pos.above(i);
                    s = this.player.level.getBlockState(p);
                    if (remove.apply(s, p)) this.player.level.removeBlock(p, false);
                    else if (valid.apply(s, p)) this.player.level.setBlock(p, sides_above.apply(p), 2);
                }
                p = pos.above(4);
                s = this.player.level.getBlockState(p);
                if (!s.isAir() && valid.apply(s, p))
                {
                    place = below.apply(p);
                    this.player.level.setBlock(p, place, 2);
                }

            });
        }
    }

    private void updateTargetMemory(final BlockPos target)
    {
        double dx, dz, dh2;
        for (final Node n : this.getMap().allRooms)
        {
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
        if (this.map.allRooms.isEmpty())
        {
            final ServerLevel world = (ServerLevel) this.player.level;
            final ResourceLocation location = new ResourceLocation("pokecube:village");
            final IForgeRegistry<StructureFeature<?>> reg = ForgeRegistries.STRUCTURE_FEATURES;
            final StructureFeature<?> structure = reg.getValue(location);
            BlockPos village = null;

            final Set<StructureInfo> nearby = StructureManager.getNear(world.dimension(), this.player.getOnPos(), 0);
            infos:
            for (final StructureInfo i : nearby)
                if (i.start.getFeature() == structure)
                {
                    village = i.start.getLocatePos();
                    break infos;
                }
            if (village == null) village = world.findNearestMapFeature(structure, this.player.getOnPos(), 50, false);
            if (village != null)
            {
                int size = 32;
                final Set<StructureInfo> near = nearby.isEmpty() ? StructureManager.getNear(world.dimension(), village,
                        0) : nearby;
                infos:
                for (final StructureInfo i : near)
                    if (i.start.getFeature() == structure)
                    {
                        size = Math.max(i.start.getBoundingBox().getXSpan(), i.start.getBoundingBox().getZSpan());
                        break infos;
                    }
                final Node n = new Node();
                n.started = true;
                n.dig_done = Tracker.instance().getTick();
                n.setCenter(village, size);
                this.map.add(n);
                final CompoundTag tag = this.getMap().serializeNBT();
                this.player.getPersistentData().put("tree_map", tag);
            }
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

    private Node getTargetNode()
    {
        if (this.currentEdge != null) return this.targetNode;
        if (this.player.getPersistentData().hasUUID("t_node"))
        {
            final Part p = this.getMap().allParts.get(this.player.getPersistentData().getUUID("t_node"));
            if (p instanceof final Node e) return this.setTargetNode(e);
        }
        final ResourceLocation location = new ResourceLocation("pokecube:village");
        final IForgeRegistry<StructureFeature<?>> reg = ForgeRegistries.STRUCTURE_FEATURES;
        final StructureFeature<?> structure = reg.getValue(location);
        final ServerLevel world = (ServerLevel) this.player.level;
        final BlockPos village = world.findNearestMapFeature(structure, BlockPos.ZERO, 50, true);
        if (village != null)
        {
            this.setTargetNode(this.addNode(village));
            final List<Edge> edges = Lists.newArrayList(this.targetNode.edges);
            Collections.shuffle(edges);
            Node prev = null;
            Edge running = null;
            for (final Edge e : edges)
            {
                final Node other = e.node1 == this.targetNode ? e.node2 : e.node1;
                if (!other.started) continue;
                prev = other;
                running = e;
                break;
            }
            if (prev == null || prev.getCenter().getY() == 0)
            {
                ThutCore.LOGGER.error("Warning, no started node connected to us!");
                if (this.targetNode != null) this.player.getPersistentData().put("targ_pos", NbtUtils.writeBlockPos(
                        this.targetNode.getCenter()));
                return this.targetNode;
            }
            if (running != null) running.started = true;

            final BlockPos tpTo = prev.getCenter();
            this.telePlayer(tpTo);
        }
        return this.targetNode;
    }

    private Node addNode(final BlockPos next)
    {
        final ServerLevel world = (ServerLevel) this.player.level;
        final List<Node> nodes = Lists.newArrayList(this.getMap().allRooms);
        final BlockPos o0 = next.atY(0);
        nodes.sort((o1, o2) -> (int) (o1.getCenter().atY(0).distSqr(o0) - o2.getCenter().atY(0).distSqr(o0)));
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
        for (int i = 0; i < Math.min(nodes.size(), 3); i++)
        {
            final Node n2 = nodes.get(i);
            final Edge e = new Edge();
            n2.started = true;
            e.node1 = n1;
            e.node2 = n2;
            e.setEnds(n1.getCenter(), n2.getCenter());
            n1.edges.add(e);
            n2.edges.add(e);
        }
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
        Vec3 vecHere = this.player.position();

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

        dir = dir.normalize();
        BlockPos p2;
        for (int j = 0; j < dr; j++)
        {
            randNear = vecHere.add(dir.scale(j));
            p2 = new BlockPos(randNear);
            this.buildRoute(p2, true);
        }
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
        if (this.player.tickCount % 20 == 0)
        {
            final CompoundTag tag = this.getMap().serializeNBT();
            this.player.getPersistentData().put("tree_map", tag);
        }
        final List<ServerPlayer> readd = Lists.newArrayList();
        for (final ServerPlayer player : world.players())
            if (player.getCamera() == this.player && player != this.player) readd.add(player);

        if (readd.isEmpty() || this.mob != null)
        {
            EntityTools.copyPositions(this.mob, this.player);
            EntityTools.copyRotations(this.mob, this.player);
            EntityTools.copyEntityTransforms(this.mob, this.player);
            return;
        }

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
