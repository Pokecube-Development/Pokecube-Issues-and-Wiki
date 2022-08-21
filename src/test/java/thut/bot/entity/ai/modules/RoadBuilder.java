package thut.bot.entity.ai.modules;

import java.util.BitSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.math.Vector3f;

import net.minecraft.commands.arguments.EntityAnchorArgument.Anchor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import pokecube.world.terrain.PokecubeTerrainChecker;
import thut.api.terrain.BiomeType;
import thut.api.terrain.StructureManager;
import thut.api.terrain.StructureManager.StructureInfo;
import thut.api.terrain.TerrainManager;
import thut.bot.entity.BotPlayer;
import thut.bot.entity.ai.BotAI;
import thut.core.common.ThutCore;
import thut.lib.TComponent;

@BotAI(key = "thutbot:road")
public class RoadBuilder extends AbstractBot
{
    private class PathStateProvider
    {

        public PathStateProvider()
        {}

        public BlockState getReplacement(BlockPos p)
        {
            ServerLevel level = RoadBuilder.this.player.getLevel();

            final FluidState fluid = level.getFluidState(p);
            final BlockState b = level.getBlockState(p);
            // Over sea level water, we place planks
            if (fluid.is(FluidTags.WATER) || b.is(BlockTags.ICE)) return Blocks.OAK_PLANKS.defaultBlockState();
            // Lave is replaced with cobble
            else if (fluid.is(FluidTags.LAVA)) return Blocks.COBBLESTONE.defaultBlockState();
            // air with planks
            else if (b.isAir() || shouldClear(b, p)) return Blocks.OAK_PLANKS.defaultBlockState();
            else if (blocks.contains(b.getBlock())) return null;
            else if (replaceable(b, p)) return paths.get(RoadBuilder.this.player.getRandom().nextInt(paths.size()));
            return null;
        }

        private boolean replaceable(BlockState b, BlockPos p)
        {
            return shouldClear(b, p) || PokecubeTerrainChecker.isTerrain(b) || PokecubeTerrainChecker.isRock(b)
                    || PokecubeTerrainChecker.isEdiblePlant(b) || PokecubeTerrainChecker.isEdiblePlant(b);
        }

        public boolean shouldClear(BlockState state, BlockPos pos)
        {
            return PokecubeTerrainChecker.isLeaves(state) || PokecubeTerrainChecker.isWood(state);
        }
    }

    protected boolean done = false;

    int expectedLength = 16;
    int lengthVariation = 8;

    // Counters for when to give up, etc
    int stuckTicks = 0;
    int pathTicks = 0;

    int tpTicks = 5;

    Vec3 next = null;
    Vec3 end = null;

    public String subbiome = "none";

    private PathStateProvider pathProvider = new PathStateProvider();

    int path_index = 0;
    List<BlockPos> path_nodes = Lists.newArrayList();

    List<Block> pathB = Lists.newArrayList();
    List<Block> slabB = Lists.newArrayList();
    List<Block> blocks = Lists.newArrayList();

    final List<BlockState> paths = Lists.newArrayList(
    // @formatter:off
            Blocks.COBBLESTONE.defaultBlockState(),
            Blocks.COBBLED_DEEPSLATE.defaultBlockState(),
            Blocks.MOSSY_COBBLESTONE.defaultBlockState()
    );
    // @formatter:on
    final List<BlockState> slabs = Lists.newArrayList(
    // @formatter:off
            Blocks.COBBLESTONE_SLAB.defaultBlockState(),
            Blocks.COBBLED_DEEPSLATE_SLAB.defaultBlockState(),
            Blocks.MOSSY_COBBLESTONE_SLAB.defaultBlockState()
    );
    // @formatter:on

    final List<BlockState> pathsBridge = Lists.newArrayList(
    // @formatter:off
            Blocks.OAK_PLANKS.defaultBlockState()
    );
    // @formatter:on
    final List<BlockState> slabsBridge = Lists.newArrayList(
    // @formatter:off
            Blocks.OAK_SLAB.defaultBlockState()
    );
    // @formatter:on

    public RoadBuilder(BotPlayer player)
    {
        super(player);

        for (final BlockState block : paths)
        {
            blocks.add(block.getBlock());
            pathB.add(block.getBlock());
        }
        for (final BlockState block : slabs)
        {
            blocks.add(block.getBlock());
            slabB.add(block.getBlock());
        }

        for (final BlockState block : pathsBridge)
        {
            blocks.add(block.getBlock());
            pathB.add(block.getBlock());
        }
        for (final BlockState block : slabsBridge)
        {
            blocks.add(block.getBlock());
            slabB.add(block.getBlock());
        }
    }

    @Override
    public void botTick(ServerLevel world)
    {
        if (this.done) return;

        if (end == null || next == null)
        {
            if (!getTag().contains("next"))
            {
                done = true;
                return;
            }
            BlockPos next = NbtUtils.readBlockPos(getTag().getCompound("next"));
            BlockPos end = NbtUtils.readBlockPos(getTag().getCompound("end"));

            this.end = new Vec3(end.getX(), end.getY(), end.getZ());
            this.next = new Vec3(next.getX(), next.getY(), next.getZ());
            initPath();
        }
        if (end.distanceToSqr(next) < 4)
        {
            this.done = true;
            return;
        }
        if (player.position().distanceToSqr(next) < 256)
        {
            teleBot(new BlockPos(next));
            this.mob.getNavigation().stop();
            updateNext();
            pathTicks = 0;
        }
        else if (stuckTicks++ > tpTicks)
        {
            teleBot(new BlockPos(next));
            this.mob.getNavigation().stop();
            stuckTicks = 0;
        }

        this.player.lookAt(Anchor.EYES, next);
        if (mob.getNavigation().isDone())
        {
            tryPath(next);
            validatePath(next, 9);
        }
    }

    static Pattern build_route = Pattern.compile("(build)" + SPACE + RSRC + SPACE

            + INT + SPACE + INT + SPACE

            + INT + SPACE + INT);

    static Pattern build_route_speed = Pattern.compile("(build)" + SPACE + RSRC + SPACE

            + INT + SPACE + INT + SPACE

            + INT + SPACE + INT + SPACE + INT);

    @Override
    public boolean init(String args)
    {
        Matcher match = build_route_speed.matcher(args);
        if (match.find())
        {
            int x = Integer.parseInt(match.group(5));
            int z = Integer.parseInt(match.group(7));
            int y = player.level.getHeight(Types.WORLD_SURFACE, x, z);
            next = new Vec3(x, y, z);

            x = Integer.parseInt(match.group(9));
            z = Integer.parseInt(match.group(11));
            y = player.level.getHeight(Types.WORLD_SURFACE, x, z);
            end = new Vec3(x, y, z);
            initPath();

            this.tpTicks = Integer.parseInt(match.group(13));

            return true;
        }
        match = build_route.matcher(args);
        if (match.find())
        {
            int x = Integer.parseInt(match.group(5));
            int z = Integer.parseInt(match.group(7));
            int y = player.level.getHeight(Types.WORLD_SURFACE, x, z);
            next = new Vec3(x, y, z);

            x = Integer.parseInt(match.group(9));
            z = Integer.parseInt(match.group(11));
            y = player.level.getHeight(Types.WORLD_SURFACE, x, z);
            end = new Vec3(x, y, z);
            initPath();

            return true;
        }
        return false;
    }

    @Override
    public boolean canComplete()
    {
        return true;
    }

    @Override
    public boolean isCompleted()
    {
        return done;
    }

    private void initPath()
    {
        this.path_index = 0;
        this.path_nodes.clear();

        double dr = this.next.distanceTo(this.end);

        if (dr == 0)
        {
            this.done = true;
            return;
        }

        int num_segs = (int) Math.ceil(dr / expectedLength);
        int length = (int) Math.ceil(dr / num_segs);

        Vec3 dir = this.end.subtract(this.next).normalize();

        Vec3 next = this.next;
        List<BlockPos> path_opts = Lists.newArrayList();
        BlockPos next_pos = new BlockPos(next).atY(0);
        path_opts.add(next_pos);
        BlockPos end_pos = new BlockPos(end).atY(0);
        boolean done = false;
        int n = 0;

        // Find a random set of points to decide to use for the road.
        while (!done && n++ < 1e5)
        {
            final double dx = (this.player.getRandom().nextDouble() - 0.5) * lengthVariation;
            final double dz = (this.player.getRandom().nextDouble() - 0.5) * lengthVariation;
            Vec3 next_next = next.add(dir.x * length + dx, 0, dir.z * length + dz);
            BlockPos next_next_pos = new BlockPos(next_next).atY(0);
            path_opts.add(next_next_pos);
            next = next_next;
            next_pos = next_next_pos;
            dir = this.end.subtract(next).normalize();
            if (Math.sqrt(next_pos.distSqr(end_pos)) < expectedLength)
            {
                path_opts.add(end_pos);
                done = true;
                break;
            }
        }

        // Now ensure they are a reasonable y distance apart. The ends must be
        // fixed.
        int[] ys = new int[path_opts.size()];
        int i = 0;
        BitSet fixedPoints = new BitSet();
        fixedPoints.flip(0);
        fixedPoints.flip(ys.length - 1);

        for (var v : path_opts)
        {
            int y = this.player.level.getMinBuildHeight();
            int cx = SectionPos.blockToSectionCoord(v.getX());
            int cz = SectionPos.blockToSectionCoord(v.getZ());
            if (!this.player.level.hasChunk(cx, cz))
            {
                ChunkAccess chunk = this.player.level.getChunk(cx, cz, ChunkStatus.SURFACE);
                y = chunk.getHeight(Types.OCEAN_FLOOR_WG, v.getX(), v.getZ());
            }
            else
            {
                y = this.player.level.getHeightmapPos(Types.OCEAN_FLOOR_WG, v).getY();
            }
            if (y < this.player.level.getSeaLevel() - 2) y = this.player.level.getSeaLevel() + 2;
            y = Math.max(y, this.player.level.getSeaLevel());
            BlockPos pos = v.atY(y);
            if (!StructureManager.getNear(player.level.dimension(), pos, 5).isEmpty())
            {
                fixedPoints.set(i);
            }
            ys[i++] = y;
        }

        n = 0;
        int m = 0;
        int dy = 1;
        while (dy > 0 && n++ < 1e5)
        {
            dy = 0;
            // Repeatedly relax the system untill all slopes are "acceptable"
            for (i = 1; i < ys.length - 1; i++)
            {
                // fixed points do not get relaxed, so skip them for the check.
                if (fixedPoints.get(i)) continue;

                BlockPos p0 = path_opts.get(i).atY(ys[i]);
                BlockPos pb = path_opts.get(i - 1).atY(ys[i - 1]);
                BlockPos pa = path_opts.get(i + 1).atY(ys[i + 1]);

                double dx = (pa.getX() - p0.getX());
                double dz = (pa.getZ() - p0.getZ());

                double dh_a = Math.sqrt(dx * dx + dz * dz);
                double dy_a = Math.abs(p0.getY() - pa.getY());

                if (dy_a / dh_a > 0.4)
                {
                    int new_y0 = (p0.getY() + pa.getY()) / 2;
                    dy += Math.abs(new_y0 - ys[i]);
                    ys[i] = new_y0;
                    p0 = p0.atY(new_y0);
                    m++;
                }

                dx = (pb.getX() - p0.getX());
                dz = (pb.getZ() - p0.getZ());

                double dh_b = Math.sqrt(dx * dx + dz * dz);
                double dy_b = Math.abs(p0.getY() - pb.getY());

                if (dy_b / dh_b > 0.4)
                {
                    int new_y0 = (p0.getY() + pb.getY()) / 2;
                    dy += Math.abs(new_y0 - ys[i]);
                    ys[i] = new_y0;
                    m++;
                }
            }
        }

        int max_dr = 0;
        int max_dy = 0;
        for (i = 0; i < ys.length - 1; i++)
        {
            dy = Math.abs(ys[i] - ys[i + 1]);
            max_dy = Math.max(max_dy, dy);
            BlockPos p0 = path_opts.get(i);
            BlockPos pa = path_opts.get(i + 1);
            double dr_2 = Math.sqrt(p0.distSqr(pa));
            max_dr = (int) Math.max(max_dr, dr_2);
        }
        System.out.println("took " + n + " relaxation steps (" + m + ")");
        System.out.println("max dy: " + max_dy + " max_dr: " + max_dr);
        System.out.println("size: " + ys.length);

        if (max_dr > 60 || max_dy > 30)
        {
            this.path_index = 0;
            this.path_nodes.clear();
            initPath();
            return;
        }

        // Update the positions accordingly.
        for (i = 0; i < ys.length; i++)
        {
            path_nodes.add(path_opts.get(i).atY(ys[i]));
        }
    }

    private void validatePath(Vec3 targ, final double rr)
    {
        final PathNavigation navi = this.mob.getNavigation();

        if (pathTicks++ > tpTicks)
        {
            teleBot(new BlockPos(next));
            this.mob.getNavigation().stop();
            pathTicks = 0;
            return;
        }

        if (!navi.isInProgress()) return;

        final BlockPos next = navi.getPath().getNextNodePos();
        final BlockPos here = this.player.getOnPos();
        final int diff = 2;
        if (next.getY() <= here.getY() && here.closerThan(next, diff)) navi.getPath().advance();
    }

    private void updateNext()
    {
        double dist = next.distanceTo(end);
        double r = expectedLength + (this.player.getRandom().nextDouble()) * lengthVariation;
        Vec3 dir = end.subtract(next).normalize();
        if (dist < r)
        {
            buildRoute(next, dir, dist);
            next = end;
            this.done = true;
            return;
        }
        if (this.path_nodes.isEmpty()) this.initPath();
        System.out.println((path_index + 1) + "/" + this.path_nodes.size());
        int next_index = path_index;
        if (path_index >= path_nodes.size())
        {
            path_index = 0;
            path_nodes.clear();
            done = true;
        }
        else if (next_index < this.path_nodes.size())
        {
            Vec3 prev = next;
            BlockPos next_pos = path_nodes.get(next_index);
            next = new Vec3(next_pos.getX(), next_pos.getY(), next_pos.getZ());
            dir = next.subtract(prev);
            dist = dir.length();
            dir = dir.normalize();
            BlockPos npos = new BlockPos(next);
            BlockPos epos = new BlockPos(end);
            getTag().put("next", NbtUtils.writeBlockPos(npos));
            getTag().put("end", NbtUtils.writeBlockPos(epos));
            buildRoute(prev, dir, dist);
            this.path_index++;
        }
    }

    /**
     * Builds a route starting from start, in direction dir.
     *
     * @param start - where to start building
     * @param dir   - direction to build in
     * @param dist  - distance to build for
     */
    private void buildRoute(final Vec3 start, final Vec3 dir, final double dist)
    {
        if (dist > 100)
        {
            ThutCore.LOGGER.error("Road segment too long! " + start + " " + dir + " " + dist + "" + this.path_index);
            return;
        }

        final ServerLevel level = (ServerLevel) this.player.level;

        BlockPos pos;
        Vec3 vec = start;

        final Vector3f up = Vector3f.YP;
        final Vector3f dr = new Vector3f(dir);
        final Vector3f r_h = up.copy();
        // This is horizontal direction to the path.
        r_h.cross(dr);
        final Vec3 dir_h = new Vec3(r_h);

        List<BlockPos> toAffect = Lists.newArrayList();

        List<BlockPos> torches = Lists.newArrayList();
        List<BlockPos> railings = Lists.newArrayList();
        List<BlockPos> signs = Lists.newArrayList();

        List<List<BlockPos>> layers = Lists.newArrayList();

        Map<BlockPos, Integer> y_cache = Maps.newHashMap();

        for (int i = 0; i < 7; i++) layers.add(Lists.newArrayList());

        boolean makeSign = this.player.getRandom().nextDouble() < 0.5;

        for (double i = -1; i <= dist + 1; i += 0.25)
        {
            // Make torches every 5 blocks or so.
            boolean makeTorch = ((int) i) % 10 == 0 && (i - ((int) i)) < 0.25;

            h_loop:
            for (int dh = -3; dh <= 3; dh++)
            {
                vec = start.add(dir.scale(i).add(dir_h.scale(dh)));
                pos = new BlockPos(vec);

                // If too close to a structure, skip point
                final Set<StructureInfo> inside = StructureManager.getNear(level.dimension(), pos, 2);
                if (!inside.isEmpty()) continue;

                // check if we need this edge at all
                if (Math.abs(dh) == 3)
                {
                    boolean doEdge = level.getHeight(Types.OCEAN_FLOOR_WG, pos.getX(), pos.getZ()) < pos.getY() - 1;
                    if (doEdge) railings.add(pos.above());
                    else continue h_loop;
                }
                for (int y = -1; y <= 4; y++)
                {
                    vec = start.add(dir.scale(i).add(dir_h.scale(dh))).add(0, y, 0);
                    pos = new BlockPos(vec);

                    if (makeTorch && Math.abs(dh) == 2 && y == 1)
                    {
                        torches.add(pos);
                    }
                    else if (dh == 1 && ((int) i == 1) && y == 0 && makeSign)
                    {
                        signs.add(pos);
                        makeSign = false;
                    }
                    if (!toAffect.contains(pos))
                    {
                        toAffect.add(pos);
                        layers.get(y + 2).add(pos);
                        y_cache.put(pos, y);
                    }
                }
            }
        }

        Map<BlockPos, List<BlockState>> toFix = Maps.newHashMap();

        List<Direction> nextDir = Lists.newArrayList();

        Iterator<Direction> iter = Direction.Plane.HORIZONTAL.iterator();
        while (iter.hasNext())
        {
            Direction direction = iter.next();
            Vec3 d = new Vec3(direction.getStepX(), direction.getStepY(), direction.getStepZ());
            double dot = d.dot(dir);
            if ((dir.y > 0 && dot < 0)) nextDir.add(direction);
            if ((dir.y < 0 && dot > 0)) nextDir.add(direction);
        }

        for (int i = 0; i < layers.size(); i++)
        {
            List<BlockPos> toEdit = layers.get(i);
            int y = i - 2;

            for (int j = 0; j < toEdit.size(); j++)
            {
                BlockPos here = toEdit.get(j);
                BlockState state = level.getBlockState(here);
                BlockState replacement = pathProvider.getReplacement(here);
                boolean remove = y > 0 && pathProvider.shouldClear(state, here);
                boolean editable = remove || replacement != null;

                if (y == 0) for (Direction d : nextDir)
                {
                    BlockPos p = here.offset(d.getStepX(), d.getStepY(), d.getStepZ());
                    if (!y_cache.containsKey(p))
                    {
                        continue;
                    }
                    else
                    {
                        Integer k = y_cache.get(p);
                        if (k != y)
                        {
                            toFix.put(here, slabs.contains(replacement) ? slabs : slabsBridge);
                            break;
                        }
                    }
                }
                if (y >= 0 && (remove || editable)) level.removeBlock(here, false);
                else if ((y < 0 && editable && replacement != null))
                {
                    this.setBlock(level, here, replacement, 2);
                }
            }
        }

        // First replace blocks with slabs on slopes
        for (BlockPos p : toFix.keySet())
        {
            List<BlockState> list = toFix.get(p);
            this.setBlock(level, p.below(), list.get(player.getRandom().nextInt(list.size())), 2);
        }

        // Next build cobblestone railings if needed
        for (BlockPos p : railings)
        {
            this.setBlock(level, p.below(), Blocks.COBBLESTONE_WALL.defaultBlockState(), 3);
        }

        // Then place the torches
        for (BlockPos p : torches)
        {
            this.setBlock(level, p.below(2), Blocks.COBBLESTONE.defaultBlockState(), 2);
            this.setBlock(level, p.below(), Blocks.COBBLESTONE_WALL.defaultBlockState(), 2);
            this.setBlock(level, p, Blocks.TORCH.defaultBlockState(), 2);
        }

        // Last place signs
        for (BlockPos p : signs)
        {
            double facing = -Math.atan2(dir.x, dir.z) * 180 / Math.PI;
            int rot = Mth.floor((double) ((90.0F + facing) * 16.0F / 360.0F) + 0.5D) & 15;
            this.setBlock(level, p, Blocks.OAK_SIGN.defaultBlockState().setValue(StandingSignBlock.ROTATION, rot), 2);
            var opt = level.getBlockEntity(p, BlockEntityType.SIGN);
            if (opt.isPresent())
            {
                opt.get().setMessage(0, TComponent.translatable(this.subbiome));
            }
        }
    }

    private void setBlock(ServerLevel level, BlockPos p, BlockState state, int flags)
    {
        level.setBlock(p, state, flags);
        TerrainManager.getInstance().getTerrain(level, p).setBiome(p, BiomeType.getBiome(subbiome));
    }

}
