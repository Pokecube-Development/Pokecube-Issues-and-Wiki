package thut.bot.entity.ai.modules;

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
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import pokecube.core.world.terrain.PokecubeTerrainChecker;
import thut.api.terrain.StructureManager;
import thut.api.terrain.StructureManager.StructureInfo;
import thut.bot.entity.BotPlayer;
import thut.bot.entity.ai.BotAI;

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
            if (fluid.is(FluidTags.WATER)) return Blocks.OAK_PLANKS.defaultBlockState();
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

    int expectedLength = 32;
    int lengthVariation = 16;

    // Counters for when to give up, etc
    int stuckTicks = 0;
    int pathTicks = 0;

    int tpTicks = 50;

    Vec3 next = null;
    Vec3 end = null;

    private PathStateProvider pathProvider = new PathStateProvider();

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
        }
        if (end.distanceToSqr(next) < 4)
        {
            this.done = true;
            return;
        }
        if (player.position().distanceToSqr(next) < 64)
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

        final double dx = (this.player.getRandom().nextDouble() - 0.5) * lengthVariation;
        final double dz = (this.player.getRandom().nextDouble() - 0.5) * lengthVariation;

        Vec3 randNear = next.add(dir.scale(r)).add(dx, 0, dz);
        BlockPos end = new BlockPos(randNear);

        end = this.player.level.getHeightmapPos(Types.WORLD_SURFACE, end);
        if (end.getY() < this.player.level.getSeaLevel())
        {
            end = new BlockPos(end.getX(), this.player.level.getSeaLevel(), end.getZ());
        }
        FluidState fluid = player.level.getFluidState(end.below());
        if (!fluid.isEmpty())
        {
            end = new BlockPos(end.getX(), end.getY() + 3, end.getZ());
        }

        dir = randNear.subtract(next);
        double dr = dir.length();

        if (Math.abs(dir.y) > dr * 0.3)
        {
            if (randNear.y > next.y) randNear = new Vec3(end.getX(), next.y + 0.3 * dr, end.getZ());
            else randNear = new Vec3(end.getX(), next.y - 0.3 * dr, end.getZ());
            end = new BlockPos(randNear);
        }

        Vec3 prev = next;
        next = new Vec3(end.getX(), end.getY(), end.getZ());
        dir = next.subtract(prev);
        dist = dir.length();
        dir = dir.normalize();

        BlockPos npos = new BlockPos(next);
        BlockPos epos = new BlockPos(end);
        getTag().put("next", NbtUtils.writeBlockPos(npos));
        getTag().put("end", NbtUtils.writeBlockPos(epos));

        buildRoute(prev, dir, dist);
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

        List<List<BlockPos>> layers = Lists.newArrayList();

        Map<BlockPos, Integer> y_cache = Maps.newHashMap();

        for (int i = 0; i < 7; i++) layers.add(Lists.newArrayList());

        for (double i = 0; i < dist; i += 0.25)
        {
            // Make torches every 5 blocks or so.
            boolean makeTorch = ((int) i) % 10 == 0 && (i - ((int) i)) < 0.25;

            h_loop:
            for (int h = -3; h <= 3; h++)
            {
                vec = start.add(dir.scale(i).add(dir_h.scale(h)));
                pos = new BlockPos(vec);

                // If too close to a structure, skip point
                final Set<StructureInfo> inside = StructureManager.getNear(level.dimension(), pos, 3);
                if (!inside.isEmpty()) continue;

                // check if we need this edge at all
                if (Math.abs(h) == 3)
                {
                    boolean doEdge = level.getHeight(Types.OCEAN_FLOOR, pos.getX(), pos.getZ()) < pos.getY() - 1;
                    if (doEdge) railings.add(pos.above());
                    else continue h_loop;
                }
                for (int y = -1; y <= 4; y++)
                {
                    vec = start.add(dir.scale(i).add(dir_h.scale(h))).add(0, y, 0);
                    pos = new BlockPos(vec);

                    if (makeTorch && Math.abs(h) == 2 && y == 1)
                    {
                        torches.add(pos);
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
                    level.setBlock(here, replacement, 2);
                }
            }
        }

        // First replace blocks with slabs on slopes
        for (BlockPos p : toFix.keySet())
        {
            List<BlockState> list = toFix.get(p);
            level.setBlock(p.below(), list.get(player.getRandom().nextInt(list.size())), 2);
        }

        // Next build cobblestone railings if needed
        for (BlockPos p : railings)
        {
            level.setBlock(p.below(), Blocks.COBBLESTONE_WALL.defaultBlockState(), 3);
        }

        // Then place the torches
        for (BlockPos p : torches)
        {
            level.setBlock(p.below(2), Blocks.COBBLESTONE.defaultBlockState(), 2);
            level.setBlock(p.below(), Blocks.COBBLESTONE_WALL.defaultBlockState(), 2);
            level.setBlock(p, Blocks.TORCH.defaultBlockState(), 2);
        }
    }
}
