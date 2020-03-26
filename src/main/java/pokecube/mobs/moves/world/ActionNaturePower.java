package pokecube.mobs.moves.world;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CactusBlock;
import net.minecraft.block.GrassBlock;
import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.pathfinding.PathType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.server.ServerWorld;
import pokecube.core.PokecubeCore;
import pokecube.core.handlers.events.MoveEventsHandler;
import pokecube.core.interfaces.IMoveAction;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.world.terrain.PokecubeTerrainChecker;
import thut.api.maths.Vector3;

public class ActionNaturePower implements IMoveAction
{
    public static class DesertChanger implements IBiomeChanger
    {
        static final Biome DESERT = Biomes.DESERT;

        public DesertChanger()
        {
        }

        @Override
        public boolean apply(final BlockPos pos, final ServerWorld world)
        {
            // This is the predicate we will use for checking whether something
            // is a valid spot.
            final Predicate<BlockPos> predicate = t ->
            {
                final BlockState stateHere = world.getBlockState(t);
                final BlockState stateUp = world.getBlockState(t.up());
                final Block blockHere = stateHere.getBlock();
                final Block blockUp = stateUp.getBlock();
                // If already desert biome, this isn't valid, so
                // we can return false.
                if (world.getBiome(t) == DesertChanger.DESERT) return false;
                // Only valid surface blocks are sand
                final boolean validHere = blockHere == Blocks.SAND;
                // Only counts as desert if air or cactus on top
                final boolean validUp = blockUp.isAir(stateUp, world, t.up()) || blockUp instanceof CactusBlock;
                return validHere && validUp;
            };
            // Used on a sand block, will only apply and return true if there is
            // some cactus found though.
            final BlockState state = world.getBlockState(pos);

            // Has to be used on sand
            if (state.getBlock() == Blocks.SAND)
            {
                final PointChecker checker = new PointChecker(world, Vector3.getNewVector().set(pos.down()), predicate);
                checker.checkPoints();
                // Check if any cactus is found, will only allow this change if
                // at least 1 is found.
                boolean cactus = false;
                for (final BlockState found : checker.states)
                {
                    cactus = found.getBlock() instanceof CactusBlock;
                    if (cactus) break;
                }
                if (!cactus) return false;
                return ActionNaturePower.applyChecker(checker, world, DesertChanger.DESERT);
            }
            return false;
        }
    }

    public static class ForestChanger implements IBiomeChanger
    {
        static final Biome FOREST = Biomes.FOREST;

        public ForestChanger()
        {
        }

        @Override
        public boolean apply(final BlockPos pos, final ServerWorld world)
        {
            // This is the predicate we will use for checking whether something
            // is a valid spot.
            final Predicate<BlockPos> predicate = t ->
            {
                final BlockState stateHere = world.getBlockState(t);
                final BlockState stateUp = world.getBlockState(t.up());
                final Block blockHere = stateHere.getBlock();
                final Block blockUp = stateUp.getBlock();

                // If already forest biome, this isn't valid, so
                // we can return false.
                if (world.getBiome(t) == ForestChanger.FOREST) return false;
                // Only valid surface blocks are dirt and grass
                // for this.
                final boolean validHere = blockHere == Blocks.GRASS || blockHere == Blocks.DIRT;
                // If it is dirt, it must be under a tree,
                // otherwise it can be under air or a plant.
                final boolean validUp = blockHere == Blocks.DIRT ? PokecubeTerrainChecker.isWood(stateUp)
                        : blockUp.isAir(stateUp, world, t.up()) || PokecubeTerrainChecker.isCutablePlant(stateUp);
                return validHere && validUp;
            };
            // Used on a tree, spreads outwards from tree along dirt and grass
            // blocks, and converts the area to forest.
            final BlockState state = world.getBlockState(pos);
            final BlockState below = world.getBlockState(pos.down());

            // Has to be wood on dirt, ie at least originally a tree.
            if (below.getBlock() == Blocks.DIRT && PokecubeTerrainChecker.isWood(state))
            {
                final PointChecker checker = new PointChecker(world, Vector3.getNewVector().set(pos.down()), predicate);
                checker.checkPoints();
                return ActionNaturePower.applyChecker(checker, world, ForestChanger.FOREST);
            }
            return false;
        }
    }

    public static class HillsChanger implements IBiomeChanger
    {
        final Biome HILLS = Biomes.MOUNTAINS;

        public HillsChanger()
        {
        }

        @Override
        public boolean apply(final BlockPos pos, final ServerWorld world)
        {
            // Ensure that this is actually a "high" spot.
            if (pos.getY() < world.getActualHeight() / 2) return false;

            // This is the predicate we will use for checking whether something
            // is a valid spot.
            final Predicate<BlockPos> predicate = t ->
            {
                final BlockState stateHere = world.getBlockState(t);
                final Block blockHere = stateHere.getBlock();
                // If already hills biome, this isn't valid, so
                // we can return false.
                if (world.getBiome(t) == HillsChanger.this.HILLS) return false;
                // Only valid surface blocks are stone
                final boolean validHere = blockHere == Blocks.STONE;
                // Block must be the surface
                // TODO see if this works.
                final boolean validUp = ActionNaturePower.getTopSolidOrLiquidBlock(world, null, t.getX() & 15, t.getZ()
                        & 15).getY() <= t.getY();
                return validHere && validUp;
            };
            // Used on a stone, spreads sideways
            final BlockState state = world.getBlockState(pos);

            // Has to be used on stone.
            if (state.getBlock() == Blocks.STONE)
            {
                final PointChecker checker = new PointChecker(world, Vector3.getNewVector().set(pos), predicate);
                checker.checkPoints();
                return ActionNaturePower.applyChecker(checker, world, this.HILLS);
            }
            return false;
        }
    }

    /**
     * Implementers of this interface must have a public constructor that takes
     * no arguments.
     */
    public abstract interface IBiomeChanger
    {
        /**
         * This method should check whether it should apply a biome change, and
         * if it should, it should do so, then return true. It should return
         * false if it does not change anything. Only the first of these to
         * return true will be used, so if you need to re-order things, reorder
         * ActionNaturePower.changer_classes accordingly.
         */
        public boolean apply(BlockPos pos, ServerWorld world);
    }

    // public static void updateChunks(PlayerChunkMap chunkMap, Set<Chunk>
    // affected, int minY, int maxY)
    // {
    // Send updates about the chunk having changed. If this is not done,
    // the player will need to leave area and return to see the changes
    // on their end.
    // for (Chunk chunk : affected)
    // {
    // PlayerChunkMapEntry entry = chunkMap.getEntry(chunk.x, chunk.z);
    // if (entry != null)
    // {
    // // Reflection is fine here, as not too many chunks should be
    // // affected by a single use of naturepower.
    // ReflectionHelper.setPrivateValue(PlayerChunkMapEntry.class, entry,
    // false, "sentToPlayers",
    // "field_187290_j", "j");
    // entry.sendToPlayers();
    // ClassInheritanceMultiMap<Entity>[] entityLists =
    // chunk.getEntityLists();
    // // Ensure range is correct.
    // minY = Math.max(0, minY);
    // maxY = Math.min(entityLists.length - 1, maxY);
    // for (int y = minY; y <= maxY; y++)
    // {
    // // Iterate over the mobs here, and send updates to clients.
    // // This is needed, as somehow the entry.sendToPlayers();
    // // above removes the entities from the client side mob
    // // lists. This is the simplest way I found to re-add them to
    // // those lists.
    // ClassInheritanceMultiMap<Entity> e = entityLists[y];
    // Iterator<Entity> iter = e.iterator();
    // while (iter.hasNext())
    // {
    // Entity mob = iter.next();
    // PacketHandler.sendEntityUpdate(mob);
    // }
    // }
    // }
    // }
    // }

    public static class PlainsChanger implements IBiomeChanger
    {
        static final Biome PLAINS = Biomes.PLAINS;

        public PlainsChanger()
        {
        }

        @Override
        public boolean apply(final BlockPos pos, final ServerWorld world)
        {
            // This is the predicate we will use for checking whether something
            // is a valid spot.
            final Predicate<BlockPos> predicate = t ->
            {
                final BlockState stateHere = world.getBlockState(t);
                final BlockState stateUp = world.getBlockState(t.up());
                final Block blockHere = stateHere.getBlock();
                // If already plains biome, this isn't valid, so
                // we can return false.
                if (world.getBiome(t) == PlainsChanger.PLAINS) return false;
                // Only valid surface blocks are grass
                // for this.
                final boolean validHere = blockHere instanceof GrassBlock;
                // Only counts as plains if it has plants on grass, so say
                // flowers, tall grass, etc
                final boolean validUp = PokecubeTerrainChecker.isCutablePlant(stateUp);
                return validHere && validUp;
            };
            // Used on a grass, spreads sideways and only converts blocks that
            // have plants on top of the grass.
            final BlockState state = world.getBlockState(pos);

            // Has to be used on grass.
            if (state.getBlock() instanceof GrassBlock)
            {
                final PointChecker checker = new PointChecker(world, Vector3.getNewVector().set(pos), predicate);
                checker.checkPoints();
                return ActionNaturePower.applyChecker(checker, world, PlainsChanger.PLAINS);
            }
            return false;
        }
    }

    /**
     * Very basic tree finder, it finds all connected blocks that match the
     * validCheck predicate.
     */
    public static class PointChecker
    {
        World   world;
        Vector3 centre;
        // we use lists here for faster iteration, sets are faster lookups for
        // contains, but lists iterate more GC friendly.
        List<Vector3>             blocks  = new LinkedList<>();
        List<Vector3>             checked = new LinkedList<>();
        List<BlockState>          states  = Lists.newArrayList();
        final Predicate<BlockPos> validCheck;
        boolean                   yaxis   = false;
        int                       maxRSq  = 8 * 8;

        public PointChecker(final World world, final Vector3 pos, final Predicate<BlockPos> validator)
        {
            this.world = world;
            this.centre = pos;
            this.validCheck = validator;
        }

        public void checkPoints()
        {
            this.populateList(this.centre);
        }

        public void clear()
        {
            this.blocks.clear();
            this.checked.clear();
        }

        private boolean nextPoint(final Vector3 prev, final List<Vector3> tempList)
        {
            boolean ret = false;
            final Vector3 temp = Vector3.getNewVector();
            // Check the connected blocks, see if they match predicate, if they
            // do, add them to the list. This also checks diagonally connected
            // blocks.
            for (int i = -1; i <= 1; i++)
                for (int j = -1; j <= 1; j++)
                    // If yaxis, also check vertical connections, for
                    // naturepower, we usually only care about horizontal.
                    if (this.yaxis) for (int k = -1; k <= 1; k++)
                    {
                        temp.set(prev).addTo(i, k, j);
                        if (this.validCheck.test(temp.getPos())) if (temp.distToSq(this.centre) <= this.maxRSq)
                        {
                            tempList.add(temp.copy());
                            this.states.add(temp.getBlockState(this.world));
                            ret = true;
                        }
                    }
                    else
                    {
                        temp.set(prev).addTo(i, 0, j);
                        if (this.validCheck.test(temp.getPos())) if (temp.distToSq(this.centre) <= this.maxRSq)
                        {
                            tempList.add(temp.copy());
                            this.states.add(temp.getBlockState(this.world));
                            ret = true;
                        }
                    }
            this.checked.add(prev);
            return ret;
        }

        private void populateList(final Vector3 base)
        {
            // Add the initial block.
            this.blocks.add(base);
            // Loop untill no new blocks have been added.
            while (this.checked.size() < this.blocks.size())
            {
                final List<Vector3> toAdd = new ArrayList<>();
                // Add all connecting blocks that match, unless they have
                // already been checked.
                for (final Vector3 v : this.blocks)
                    if (!this.checked.contains(v)) this.nextPoint(v, toAdd);
                // Add any blocks that are new to the list.
                for (final Vector3 v : toAdd)
                    if (!this.blocks.contains(v)) this.blocks.add(v);
            }
        }
    }

    /**
     * This class will reset the biomes back to whatever worldgen says they
     * should be, it goes out 8 blocks, and checks what the biome is, what it
     * should be, and sets it back. It must be used on a diamond block.
     */
    public static class ResetChanger implements IBiomeChanger
    {
        public ResetChanger()
        {
        }

        @Override
        public boolean apply(final BlockPos pos, final ServerWorld world)
        {
            if (world.getBlockState(pos).getBlock() != Blocks.DIAMOND_BLOCK) return false;
            boolean mod = false;
            final Set<IChunk> affected = Sets.newHashSet();
            final Vector3 vec = Vector3.getNewVector().set(pos);
            for (int i = -8; i <= 8; i++)
                for (int j = -8; j <= 8; j++)
                {
                    vec.addTo(i, 0, j);
                    final Biome here = vec.getBiome(world);
                    final Biome natural = world.getChunkProvider().getChunkGenerator().getBiomeProvider().getBiome(vec
                            .getPos());
                    if (natural != here)
                    {
                        vec.setBiome(natural, world);
                        affected.add(world.getChunk(vec.getPos()));
                        mod = true;
                    }
                }
            final ServerWorld sWorld = world;
            sWorld.getChunkProvider().markBlockChanged(pos);
            return mod;
        }

    }

    public static final List<Class<? extends IBiomeChanger>> changer_classes = Lists.newArrayList();

    static
    {
        ActionNaturePower.changer_classes.add(ForestChanger.class);
        ActionNaturePower.changer_classes.add(PlainsChanger.class);
        ActionNaturePower.changer_classes.add(DesertChanger.class);
        ActionNaturePower.changer_classes.add(HillsChanger.class);
        ActionNaturePower.changer_classes.add(ResetChanger.class);
    }

    public static boolean applyChecker(final PointChecker checker, final World world, final Biome biome)
    {
        // Check if > 1 as it will always at least contain the center.
        if (checker.blocks.size() > 1)
        {
            final Set<IChunk> affected = Sets.newHashSet();
            // ServerWorld sWorld = (ServerWorld) world;
            // sWorld.getServer().getPlayerList()
            // PlayerChunkMap chunkMap = sWorld.getPlayerChunkMap();
            int minY = Integer.MAX_VALUE;
            int maxY = Integer.MIN_VALUE;
            // Apply the biome to all the locations.
            for (final Vector3 loc : checker.blocks)
            {
                loc.setBiome(biome, world);
                affected.add(world.getChunk(loc.getPos()));
                // sWorld.getPlayerChunkMap().markBlockForUpdate(loc.getPos());
                minY = Math.min(minY, loc.intY() / 16);
                maxY = Math.max(maxY, loc.intY() / 16);
            }
            // updateChunks(chunkMap, affected, minY - 1, maxY + 1);
            return true;
        }
        return false;
    }

    private static BlockPos getTopSolidOrLiquidBlock(final IWorldReader p_208498_0_,
            @Nullable final EntityType<?> p_208498_1_, final int p_208498_2_, final int p_208498_3_)
    {
        final BlockPos blockpos = new BlockPos(p_208498_2_, p_208498_0_.getHeight(EntitySpawnPlacementRegistry
                .func_209342_b(p_208498_1_), p_208498_2_, p_208498_3_), p_208498_3_);
        final BlockPos blockpos1 = blockpos.down();
        return p_208498_0_.getBlockState(blockpos1).allowsMovement(p_208498_0_, blockpos1, PathType.LAND) ? blockpos1
                : blockpos;
    }

    /**
     * This is filled with new instances of whatever is in changer_classes. It
     * will have same ordering as changer_classes, and the first of these to
     * return true for a location is the only one that will be used.
     */
    private final List<IBiomeChanger> changers = Lists.newArrayList();

    public ActionNaturePower()
    {
    }

    @Override
    public boolean applyEffect(final IPokemob attacker, final Vector3 location)
    {
        if (attacker.getCombatState(CombatStates.ANGRY)) return false;
        if (!(attacker.getOwner() instanceof ServerPlayerEntity)) return false;
        if (!MoveEventsHandler.canEffectBlock(attacker, location)) return false;
        final long time = attacker.getEntity().getPersistentData().getLong("lastAttackTick");
        if (time + 20 * 3 > attacker.getEntity().getEntityWorld().getGameTime()) return false;
        final BlockPos pos = location.getPos();
        final ServerWorld world = (ServerWorld) attacker.getEntity().getEntityWorld();
        // Check the changers in order, and apply the first one that returns
        // true. TODO hunger cost added here.
        for (final IBiomeChanger changer : this.changers)
            if (changer.apply(pos, world)) return true;
        return false;
    }

    @Override
    public String getMoveName()
    {
        return "naturepower";
    }

    @Override
    public void init()
    {
        for (final Class<? extends IBiomeChanger> clazz : ActionNaturePower.changer_classes)
            try
            {
                this.changers.add(clazz.newInstance());
            }
            catch (final Exception e)
            {
                PokecubeCore.LOGGER.error("error with changer " + clazz, e);
            }
    }
}
