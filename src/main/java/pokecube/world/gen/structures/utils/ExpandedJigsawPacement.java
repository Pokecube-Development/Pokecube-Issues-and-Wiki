package pokecube.world.gen.structures.utils;

import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import org.apache.commons.lang3.mutable.MutableObject;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.JigsawBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.JigsawBlockEntity.JointType;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.Structure.GenerationContext;
import net.minecraft.world.level.levelgen.structure.Structure.GenerationStub;
import net.minecraft.world.level.levelgen.structure.pools.EmptyPoolElement;
import net.minecraft.world.level.levelgen.structure.pools.JigsawJunction;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool.Projection;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import pokecube.api.PokecubeAPI;
import pokecube.mixin.accessors.ChunkAccessAcessor;
import pokecube.mixin.accessors.WorldGenRegionAccessor;
import pokecube.world.gen.structures.GenericJigsawStructure;
import pokecube.world.gen.structures.pieces.ExpandedPoolElementStructurePiece;
import pokecube.world.gen.structures.pool_elements.ExpandedJigsawPiece;
import thut.core.common.ThutCore;

public class ExpandedJigsawPacement
{
    public static final Set<ResourceLocation> ALWAYS_ADD = Sets.newHashSet();

    static
    {
        ALWAYS_ADD.add(new ResourceLocation("pokecube:village/common/trainers"));
    }

    public static ServerLevel getForGen(final GenerationContext context)
    {
        // Try directly getting the level first
        if (context.heightAccessor() instanceof ServerLevel level) return level;
        // Then try from the chunk access
        if (context.heightAccessor() instanceof ChunkAccessAcessor access_a)
        {
            LevelHeightAccessor levelh = access_a.getLevelHeightAccessor();
            if (levelh instanceof ServerLevel level) return level;
            else if (levelh instanceof WorldGenRegionAccessor access) return access.getServerLevel();
        }
        // Next try if it was a world gen region
        if (context.heightAccessor() instanceof WorldGenRegionAccessor access) return access.getServerLevel();

        // Finally decide from chunkGenerator.
        ChunkGenerator chunkGen = context.chunkGenerator();
        final MinecraftServer server = ThutCore.proxy.getServer();
        for (final ServerLevel w : server.getAllLevels()) if (w.getChunkSource().getGenerator() == chunkGen) return w;
        Exception e = new IllegalStateException("Did not find a server level for this context!");
        PokecubeAPI.LOGGER.error(e);
        e.printStackTrace();
        return server.overworld();
    }

    public static Optional<GenerationStub> addPieces(GenericJigsawStructure config, GenerationContext context,
            BlockPos centre, boolean bound_checks, boolean on_surface)
    {
        WorldgenRandom worldgenrandom = new WorldgenRandom(new LegacyRandomSource(0L));
        worldgenrandom.setLargeFeatureSeed(context.seed(), context.chunkPos().x, context.chunkPos().z);
        RandomState rng = context.randomState();
        RegistryAccess registryaccess = context.registryAccess();
        ChunkGenerator chunkgenerator = context.chunkGenerator();
        StructureTemplateManager structuremanager = context.structureTemplateManager();
        LevelHeightAccessor levelheightaccessor = context.heightAccessor();
        Predicate<Holder<Biome>> predicate = context.validBiome();

        Registry<StructureTemplatePool> registry = registryaccess.registryOrThrow(Registry.TEMPLATE_POOL_REGISTRY);
        Rotation rotation = Rotation.getRandom(worldgenrandom);

        StructureTemplatePool root_pool = config.startPool.value();
        // This one can be completely random, as is just the start pool, this
        // shouldn't have any requirements...
        StructurePoolElement root_element = root_pool.getRandomTemplate(worldgenrandom);

        if (root_element == EmptyPoolElement.INSTANCE)
        {
            return Optional.empty();
        }
        else
        {
            PoolElementStructurePiece root_piece = new ExpandedPoolElementStructurePiece(structuremanager, root_element,
                    centre, root_element.getGroundLevelDelta(), rotation,
                    root_element.getBoundingBox(structuremanager, centre, rotation));
            BoundingBox boundingbox = root_piece.getBoundingBox();
            int i = (boundingbox.maxX() + boundingbox.minX()) / 2;
            int j = (boundingbox.maxZ() + boundingbox.minZ()) / 2;

            int root_dy = config.y_settings.vertical_offset;
            int ground_y = 0;

            if (config.air)
            {
                ground_y = chunkgenerator.getFirstFreeHeight(i, j, config.height_type, levelheightaccessor, rng);
                ground_y = Math.max(config.y_settings.min_y, ground_y);
                if (config.y_settings.vertical_offset > 0)
                    root_dy = config.y_settings.dy_offset + worldgenrandom.nextInt(config.y_settings.vertical_offset);
            }
            else if (config.underground)
            {
                ground_y = chunkgenerator.getFirstFreeHeight(i, j, config.height_type, levelheightaccessor, rng);
                ground_y = Math.min(config.y_settings.max_y, ground_y);
                if (config.y_settings.vertical_offset > 0)
                    root_dy = -config.y_settings.dy_offset - worldgenrandom.nextInt(config.y_settings.vertical_offset);
            }
            else if (on_surface)
            {
                ground_y = chunkgenerator.getFirstFreeHeight(i, j, config.height_type, levelheightaccessor, rng);
                root_dy = config.y_settings.vertical_offset;
            }

            if (config.y_settings.fixed_y > Integer.MIN_VALUE)
            {
                ground_y = config.y_settings.fixed_y;
            }

            int k = centre.getY() + root_dy + ground_y;

            if (!predicate.test(chunkgenerator.getBiomeSource().getNoiseBiome(QuartPos.fromBlock(i),
                    QuartPos.fromBlock(k), QuartPos.fromBlock(j), rng.sampler())))
            {
                return Optional.empty();
            }
            else
            {
                int l = boundingbox.minY() + root_piece.getGroundLevelDelta();
                root_piece.move(0, k - l, 0);

                BoundingBox pieceBoundingBox = root_piece.getBoundingBox();
                int pieceCenterX = (pieceBoundingBox.maxX() + pieceBoundingBox.minX()) / 2;
                int pieceCenterZ = (pieceBoundingBox.maxZ() + pieceBoundingBox.minZ()) / 2;
                int pieceCenterY = l;
                AABB maxBounds = new AABB(pieceBoundingBox.getCenter()).inflate(
                        config.clearances.max_distance_from_center, 1024, config.clearances.max_distance_from_center);

                return Optional.of(new Structure.GenerationStub(new BlockPos(pieceCenterX, pieceCenterY, pieceCenterZ),
                        (builder) ->
                        {
                            if (config.max_depth > 0)
                            {
                                List<Placer> attempts = Lists.newArrayList();

                                if (ThutCore.conf.debug) PokecubeAPI.LOGGER.debug("Building: {}", root_pool.getName());
                                tries:
                                for (int n = 0; n < 10; n++)
                                {
                                    if (ThutCore.conf.debug) PokecubeAPI.LOGGER.debug("Starting Try: {}", n);
                                    List<PoolElementStructurePiece> list = Lists.newArrayList();
                                    list.add(root_piece);
                                    Placer placer = new Placer(config, context, registry, config.max_depth, maxBounds,
                                            list, worldgenrandom);
                                    attempts.add(placer);

                                    MutableObject<VoxelShape> bounds = new MutableObject<>(
                                            Shapes.join(Shapes.create(maxBounds), Shapes.create(AABB.of(boundingbox)),
                                                    BooleanOp.ONLY_FIRST));

                                    PieceState next_state = new PieceState(root_piece, bounds, 0);
                                    placer.placing.addLast(next_state);

                                    while (!placer.placing.isEmpty())
                                    {
                                        next_state = placer.placing.removeFirst();
                                        placer.tryPlacingChildren(next_state, bound_checks, levelheightaccessor);
                                    }

                                    if (ThutCore.conf.debug) PokecubeAPI.LOGGER.debug("Ended Try: {}", n);
                                    if (placer.needed_once.isEmpty()) break;
                                    for (String s : placer.needed_once)
                                    {
                                        if (!placer.added_once.contains(s))
                                        {
                                            if (ThutCore.conf.debug)
                                                PokecubeAPI.LOGGER.debug("Try: {} has failed. Missing {}", n, s);
                                            continue tries;
                                        }
                                    }
                                    break;
                                }

                                Placer most_complete = attempts.get(attempts.size() - 1);
                                if (attempts.size() > 0)
                                {
                                    int missing = 0;
                                    for (String s : most_complete.needed_once)
                                    {
                                        if (!most_complete.added_once.contains(s)) missing++;
                                    }

                                    for (Placer attempt : attempts)
                                    {
                                        int number = 0;
                                        for (String s : attempt.needed_once)
                                        {
                                            if (!attempt.added_once.contains(s)) number++;
                                        }
                                        if (number < missing)
                                        {
                                            missing = number;
                                            most_complete = attempt;
                                        }
                                    }
                                }
                                @SuppressWarnings("unchecked")
                                List<PoolElementStructurePiece> list = (List<PoolElementStructurePiece>) most_complete.pieces;

                                if (ThutCore.conf.debug) PokecubeAPI.LOGGER.debug("Finshed: {}", root_pool.getName());

                                new PostProcessor(config).accept(context, list);
                                list.forEach(builder::addPiece);
                                config.markPlaced(context);
                            }
                        }));
            }
        }
    }

    static final class PieceState
    {
        final PoolElementStructurePiece piece;
        final Set<BlockPos> used_jigsaws = Sets.newHashSet();
        final MutableObject<VoxelShape> free;
        final int depth;

        PieceState(PoolElementStructurePiece piece, MutableObject<VoxelShape> free, int depth)
        {
            this.piece = piece;
            this.free = free;
            this.depth = depth;
        }
    }

    static final class PiecePlacement implements Comparable<PiecePlacement>
    {
        final Runnable run;
        final PoolElementStructurePiece piece;

        public PiecePlacement(PoolElementStructurePiece piece, Runnable run)
        {
            this.run = run;
            this.piece = piece;
        }

        @Override
        public int compareTo(PiecePlacement o)
        {
            boolean usRigid = piece.getElement().getProjection() == Projection.RIGID;
            boolean theyRigid = o.piece.getElement().getProjection() == Projection.RIGID;
            if (usRigid && !theyRigid)
            {
                return -1;
            }
            if (theyRigid && !usRigid)
            {
                return 1;
            }
            return 0;
        }
    }

    static enum Attachment
    {
        EDGE, INSIDE, INVALID;

        public boolean valid()
        {
            return this != INVALID;
        }
    }

    static final class Placer
    {
        final Registry<StructureTemplatePool> pools;
        final int maxDepth;
        final ChunkGenerator chunkGenerator;
        final StructureTemplateManager structureManager;
        final GenerationContext context;
        final List<? super PoolElementStructurePiece> pieces;
        final RandomSource random;
        final Set<String> needed_once;
        final Set<String> added_once = Sets.newHashSet();
        final GenericJigsawStructure config;
        final RandomState rng;
        final Deque<ExpandedJigsawPacement.PieceState> placing = Queues.newArrayDeque();
        final AABB maxBounds;
        MutableObject<VoxelShape> rigid_bounds = new MutableObject<>();
        MutableObject<VoxelShape> non_rigid_bounds = new MutableObject<>();

        boolean logs = false;

        Placer(GenericJigsawStructure config, GenerationContext context, Registry<StructureTemplatePool> pools,
                int max_depth, AABB maxBounds, List<? super PoolElementStructurePiece> pieces, RandomSource random)
        {
            this.pools = pools;
            this.maxDepth = max_depth;
            this.context = context;
            this.chunkGenerator = context.chunkGenerator();
            this.structureManager = context.structureTemplateManager();
            this.pieces = pieces;
            this.random = random;
            this.needed_once = Sets.newHashSet(config.required_parts);
            this.rng = context.randomState();
            this.config = config;
            this.maxBounds = maxBounds;
        }

        List<StructureBlockInfo> getShuffledJigsaws(StructurePoolElement structurepoolelement, BlockPos blockpos,
                Rotation rotation)
        {
            List<StructureBlockInfo> shuffled = structurepoolelement.getShuffledJigsawBlocks(this.structureManager,
                    blockpos, rotation, this.random);
            return shuffled;
        }

        List<StructurePoolElement> getShuffledParts(int depth, Optional<StructureTemplatePool> target_pool,
                Optional<StructureTemplatePool> fallback_pool)
        {
            List<StructurePoolElement> list = Lists.newArrayList();
            boolean addChildren = depth < this.maxDepth || ALWAYS_ADD.contains(target_pool.get().getName());
            if (addChildren)
            {
                list.addAll(target_pool.get().getShuffledTemplates(this.random));
                Set<ResourceLocation> extra_pools = Sets.newHashSet();
                for (StructurePoolElement e : list)
                {
                    if (e instanceof ExpandedJigsawPiece p)
                    {
                        extra_pools.addAll(p.extra_pools);
                    }
                }
                for (ResourceLocation l : extra_pools)
                {
                    Optional<StructureTemplatePool> target_pool_e = this.pools.getOptional(l);
                    if (target_pool_e.isPresent())
                    {
                        list.addAll(target_pool_e.get().getShuffledTemplates(this.random));
                    }
                }
                Collections.shuffle(list);
                if (!needed_once.isEmpty())
                {
                    List<StructurePoolElement> to_remove = Lists.newArrayList();
                    List<StructurePoolElement> needed = Lists.newArrayList();
                    for (StructurePoolElement e : list)
                    {
                        outer:
                        if (e instanceof ExpandedJigsawPiece p)
                        {
                            boolean need = false;
                            for (String flag : p._flags)
                            {
                                // If we have, mark to remove and skip
                                if (added_once.contains(flag))
                                {
                                    to_remove.add(e);
                                    break outer;
                                }
                                // Otherwise, if we need it, mark as needed
                                // We only skip this if not removed by another
                                // flag!
                                if (needed_once.contains(flag))
                                {
                                    need = p.int_config.priority >= 0;
                                }
                            }
                            if (need) needed.add(p);
                        }
                    }
                    // Remove the removes
                    list.removeAll(to_remove);
                    // also the needed
                    list.removeAll(needed);
                    // Re-add the needed at the front
                    list.addAll(0, needed);
                }
            }
            list.addAll(fallback_pool.get().getShuffledTemplates(this.random));
            return list;
        }

        public Attachment canAttach(StructureTemplate.StructureBlockInfo root,
                StructureTemplate.StructureBlockInfo next, AABB root_box)
        {
            Direction root_front = JigsawBlock.getFrontFacing(root.state);
            Direction next_front = JigsawBlock.getFrontFacing(next.state);

            boolean correct_direction = root_front == next_front.getOpposite();

            if (!correct_direction && logs) PokecubeAPI.LOGGER.debug("wrong direction");
            if (!correct_direction) return Attachment.INVALID;

            Direction root_top = JigsawBlock.getTopFacing(root.state);
            Direction next_top = JigsawBlock.getTopFacing(next.state);
            JointType jointtype = JointType.byName(root.nbt.getString("joint")).orElseGet(() -> {
                return root_front.getAxis().isHorizontal() ? JointType.ALIGNED : JointType.ROLLABLE;
            });

            boolean correct_orientation = jointtype == JointType.ROLLABLE || root_top == next_top;
            if (!correct_orientation && logs) PokecubeAPI.LOGGER.debug("wrong orientation");

            if (!correct_orientation) return Attachment.INVALID;

            boolean tag_match = root.nbt.getString("target").equals(next.nbt.getString("name"));

            if (!tag_match && logs)
                PokecubeAPI.LOGGER.debug(root.nbt.getString("target") + "!=" + next.nbt.getString("name"));
            if (!tag_match) return Attachment.INVALID;
            BlockPos next_pos = root.pos.relative(root_front);
            return root_box.contains(next_pos.getX(), next_pos.getY(), next_pos.getZ()) ? Attachment.INSIDE
                    : Attachment.EDGE;
        }

        @SuppressWarnings("deprecation")
        void tryPlacingChildren(PieceState root_state, boolean bound_check, LevelHeightAccessor heightmap)
        {
            PoolElementStructurePiece current_root = root_state.piece;
            MutableObject<VoxelShape> freeSpace = root_state.free;
            int depth = root_state.depth;

            Predicate<StructurePoolElement> log_data = e -> {
                return e.toString().contains("trainer") && false;
            };

            StructurePoolElement root_element = current_root.getElement();
            BlockPos blockpos = current_root.getPosition();
            Rotation rotation = current_root.getRotation();
            StructureTemplatePool.Projection root_projection = root_element.getProjection();
            BoundingBox root_bounding_box = current_root.getBoundingBox();
            int root_min_y = root_bounding_box.minY();
            AABB root_box = AABB.of(root_bounding_box);

            Heightmap.Types _default;
            Heightmap.Types heightmap$types;
            boolean water = config.height_type == Types.OCEAN_FLOOR_WG;
            int depth_offset = 0;
            boolean parent_junctions = true;

            if (root_element instanceof ExpandedJigsawPiece p)
            {
                water = water || p.bool_config.water_terrain_match;
                depth_offset = -p.int_config.extra_child_depth;
                parent_junctions = !p.bool_config.no_affect_noise;
                _default = water ? Heightmap.Types.OCEAN_FLOOR_WG : Heightmap.Types.WORLD_SURFACE_WG;
                root_projection = p._projection;
                if (p.bool_config.only_once) for (String s : p._flags) added_once.add(s);
                for (String s : p._needed_flags) if (!s.isBlank()) needed_once.add(s);
                // Mark it as added if we needed
                // this part.
                for (String s : p._flags) if (needed_once.contains(s)) added_once.add(s);
            }
            else
            {
                _default = config.height_type;
            }

            boolean root_rigid = root_projection == StructureTemplatePool.Projection.RIGID;

            if (heightmap instanceof ServerLevel)
            {
                heightmap$types = water ? Heightmap.Types.OCEAN_FLOOR : Heightmap.Types.WORLD_SURFACE;
            }
            else
            {
                heightmap$types = _default;
            }

            List<PiecePlacement> newParts = Lists.newArrayList();

            List<StructureBlockInfo> root_jigsaws = this.getShuffledJigsaws(root_element, blockpos, rotation);
            root_jigsaws:
            for (StructureBlockInfo root_block_info : root_jigsaws)
            {
                BlockPos raw_jigsaw_pos = root_block_info.pos;
                if (root_state.used_jigsaws.contains(raw_jigsaw_pos)) continue root_jigsaws;
                Direction direction = JigsawBlock.getFrontFacing(root_block_info.state);
                BlockPos connecting_jigsaw_pos = raw_jigsaw_pos.relative(direction);
                boolean next_pick_inside = root_bounding_box.isInside(connecting_jigsaw_pos);

                int dy = raw_jigsaw_pos.getY() - root_min_y;
                int k = -1;
                ResourceLocation next_pool_name = new ResourceLocation(root_block_info.nbt.getString("pool"));
                Optional<StructureTemplatePool> next_pool = this.pools.getOptional(next_pool_name);

                boolean valid_next_pool = next_pool.isPresent()
                        && (next_pool.get().size() != 0 || Objects.equals(next_pool_name, Pools.EMPTY.location()));

                if (valid_next_pool)
                {
                    ResourceLocation resourcelocation1 = next_pool.get().getFallback();
                    Optional<StructureTemplatePool> fallback_pool = this.pools.getOptional(resourcelocation1);

                    boolean valid_fallback = fallback_pool.isPresent() && (fallback_pool.get().size() != 0
                            || Objects.equals(resourcelocation1, Pools.EMPTY.location()));

                    if (valid_fallback)
                    {
                        var picked_parts = getShuffledParts(depth, next_pool, fallback_pool);
                        // remove any parts that are deemed invalid due to
                        // attachment rules.
                        if (root_element instanceof ExpandedJigsawPiece piece) picked_parts.removeIf(piece::noAttach);

                        for (StructurePoolElement next_picked_element : picked_parts)
                        {
                            if (next_picked_element == EmptyPoolElement.INSTANCE) continue;

                            this.logs = log_data.test(next_picked_element);

                            int bounds_fails = 0;
                            int duplicated_fails = 0;
                            int wrong_attach = 0;
                            int vanilla_fails = 0;

                            for (Rotation random_direction : Rotation.getShuffled(this.random))
                            {
                                List<StructureBlockInfo> next_jigsaws = this.getShuffledJigsaws(next_picked_element,
                                        BlockPos.ZERO, random_direction);
                                BoundingBox picked_box = next_picked_element.getBoundingBox(this.structureManager,
                                        BlockPos.ZERO, random_direction);
                                int l;
                                if (bound_check && picked_box.getYSpan() <= 16)
                                {
                                    l = next_jigsaws.stream().mapToInt((structure_info) -> {
                                        if (!picked_box.isInside(structure_info.pos
                                                .relative(JigsawBlock.getFrontFacing(structure_info.state))))
                                        {
                                            return 0;
                                        }
                                        else
                                        {
                                            ResourceLocation id = new ResourceLocation(
                                                    structure_info.nbt.getString("pool"));
                                            Optional<StructureTemplatePool> pool_entry = this.pools.getOptional(id);
                                            Optional<StructureTemplatePool> pool_fallback = pool_entry
                                                    .flatMap((pool) ->
                                                    {
                                                        return this.pools.getOptional(pool.getFallback());
                                                    });
                                            int y_1 = pool_entry.map((pool) -> {
                                                return pool.getMaxSize(this.structureManager);
                                            }).orElse(0);
                                            int y_2 = pool_fallback.map((pool) -> {
                                                return pool.getMaxSize(this.structureManager);
                                            }).orElse(0);
                                            return Math.max(y_1, y_2);
                                        }
                                    }).max().orElse(0);
                                }
                                else
                                {
                                    l = 0;
                                }

                                if (this.logs) PokecubeAPI.LOGGER.info(next_jigsaws);

                                pick_jigsaws:
                                for (StructureBlockInfo next_block_info : next_jigsaws)
                                {
                                    Attachment attachment = canAttach(root_block_info, next_block_info, root_box);
                                    if (attachment.valid())
                                    {
                                        BlockPos next_pos_raw = next_block_info.pos;
                                        BlockPos next_jigsaw_pos = connecting_jigsaw_pos.subtract(next_pos_raw);
                                        BoundingBox next_pick_box = next_picked_element.getBoundingBox(
                                                this.structureManager, next_jigsaw_pos, random_direction);
                                        int next_min_y = next_pick_box.minY();
                                        StructureTemplatePool.Projection next_projection = next_picked_element
                                                .getProjection();

                                        // Some space below to prevent roads
                                        // going too close under buildings.
                                        int room_below = 5;
                                        int h_clearance = config.clearances.h_clearance;
                                        int v_clearance = config.clearances.v_clearance;
                                        if (next_picked_element instanceof ExpandedJigsawPiece p)
                                        {
                                            room_below = p.int_config.space_below;
                                            if (p.int_config.v_clearance >= 0)
                                            {
                                                v_clearance = p.int_config.v_clearance;
                                            }
                                            if (p.int_config.h_clearance >= 0)
                                            {
                                                v_clearance = p.int_config.h_clearance;
                                            }
                                            next_projection = p._projection;
                                        }

                                        boolean next_pick_rigid = next_projection == StructureTemplatePool.Projection.RIGID;

                                        int raw_pos_y = next_pos_raw.getY();
                                        int jigsaw_block_dy = dy - raw_pos_y
                                                + JigsawBlock.getFrontFacing(root_block_info.state).getStepY();
                                        int l1;
                                        if (root_rigid && next_pick_rigid)
                                        {
                                            l1 = root_min_y + jigsaw_block_dy;
                                        }
                                        else
                                        {
                                            if (k == -1)
                                            {
                                                k = this.chunkGenerator.getFirstFreeHeight(raw_jigsaw_pos.getX(),
                                                        raw_jigsaw_pos.getZ(), heightmap$types, heightmap, rng);
                                            }
                                            l1 = k - raw_pos_y;
                                        }

                                        int i2 = l1 - next_min_y;
                                        BoundingBox next_pick_box_shifted_y = next_pick_box.moved(0, i2, 0);
                                        BlockPos blockpos5 = next_jigsaw_pos.offset(0, i2, 0);
                                        if (l > 0)
                                        {
                                            int j2 = Math.max(l + 1,
                                                    next_pick_box_shifted_y.maxY() - next_pick_box_shifted_y.minY());
                                            next_pick_box_shifted_y.encapsulate(new BlockPos(
                                                    next_pick_box_shifted_y.minX(), next_pick_box_shifted_y.minY() + j2,
                                                    next_pick_box_shifted_y.minZ()));
                                        }

                                        AABB test_box = AABB.of(next_pick_box_shifted_y).deflate(0.25D);

                                        // Unless we are actually inside, do
                                        // not allow intersections with the
                                        // root box.
                                        if (!next_pick_inside && attachment == Attachment.EDGE)
                                        {
                                            if (test_box.intersects(root_box))
                                            {
                                                bounds_fails++;
                                                continue pick_jigsaws;
                                            }
                                        }

                                        // The intersect of small box + large
                                        // box equals small box iff it is
                                        // entirely contained inside.
                                        boolean entirelyInside = root_rigid && next_pick_inside
                                                && attachment == Attachment.INSIDE
                                                && test_box.intersect(root_box).equals(test_box);

                                        VoxelShape test_shape = Shapes.create(test_box);

                                        if (!entirelyInside && Shapes.joinIsNotEmpty(freeSpace.getValue(), test_shape,
                                                BooleanOp.ONLY_SECOND))
                                        {
                                            vanilla_fails++;
                                            continue pick_jigsaws;
                                        }

                                        if (this.logs)
                                            PokecubeAPI.LOGGER.debug("Placing Sub Part! {}", next_picked_element);

                                        int root_y_offset = current_root.getGroundLevelDelta();
                                        int next_y_offset;

                                        if (next_pick_rigid)
                                        {
                                            next_y_offset = root_y_offset - jigsaw_block_dy;
                                        }
                                        else
                                        {
                                            next_y_offset = next_picked_element.getGroundLevelDelta();
                                        }

                                        PoolElementStructurePiece next_piece = new ExpandedPoolElementStructurePiece(
                                                this.structureManager, next_picked_element, blockpos5, next_y_offset,
                                                random_direction, next_pick_box_shifted_y);

                                        int finalK = k;
                                        int finalDepthOffset = depth_offset;
                                        int finalRoomBelow = room_below;
                                        int finalVClearance = v_clearance;
                                        boolean finalParentJunctions = parent_junctions;
                                        StructureTemplatePool.Projection finalNextProjection = next_projection;
                                        StructureTemplatePool.Projection finalRootProjection = root_projection;

                                        newParts.add(new PiecePlacement(next_piece, () -> {

                                            // We do this in here again, as
                                            // below is when we actually add the
                                            // part.
                                            if (!entirelyInside && Shapes.joinIsNotEmpty(freeSpace.getValue(),
                                                    test_shape, BooleanOp.ONLY_SECOND))
                                            {
                                                return;
                                            }

                                            int new_depth = depth + 1 + finalDepthOffset;
                                            PieceState next_piece_state = new PieceState(next_piece, freeSpace,
                                                    new_depth);

                                            AABB next_box = AABB.of(next_pick_box_shifted_y)
                                                    .expandTowards(0, -finalRoomBelow, 0)
                                                    .inflate(h_clearance, finalVClearance, h_clearance);
                                            VoxelShape new_shape = Shapes.create(next_box);
                                            // Consume the space we used up.
                                            if (next_pick_rigid)
                                                freeSpace.setValue(Shapes.joinUnoptimized(freeSpace.getValue(),
                                                        new_shape, BooleanOp.ONLY_FIRST));

                                            int root_junction_y_offset;
                                            if (root_rigid)
                                            {
                                                root_junction_y_offset = root_min_y + dy;
                                            }
                                            else if (next_pick_rigid)
                                            {
                                                root_junction_y_offset = l1 + raw_pos_y;
                                            }
                                            else
                                            {
                                                int k2 = finalK;
                                                if (k2 == -1)
                                                {
                                                    k2 = this.chunkGenerator.getFirstFreeHeight(raw_jigsaw_pos.getX(),
                                                            raw_jigsaw_pos.getZ(), heightmap$types, heightmap, rng);
                                                }
                                                root_junction_y_offset = k2 + jigsaw_block_dy / 2;
                                            }

                                            boolean addJunctions = finalParentJunctions;

                                            if (next_picked_element instanceof ExpandedJigsawPiece p)
                                            {
                                                if (p.bool_config.only_once) for (String s : p._flags)
                                                {
                                                    added_once.add(s);
                                                    needed_once.add(s);
                                                }
                                                for (String s : p._needed_flags) if (!s.isBlank()) needed_once.add(s);
                                                // Mark it as added if we needed
                                                // this part.
                                                for (String s : p._flags)
                                                    if (needed_once.contains(s)) added_once.add(s);
                                                addJunctions = finalParentJunctions || !p.bool_config.no_affect_noise;
                                            }
                                            // The junctions are used for little
                                            // islands under the jigsaws, this
                                            // should allow having entire
                                            // sections
                                            // without them.
                                            if (addJunctions)
                                            {
                                                JigsawJunction root_junction = new JigsawJunction(
                                                        connecting_jigsaw_pos.getX(),
                                                        root_junction_y_offset - dy + root_y_offset,
                                                        connecting_jigsaw_pos.getZ(), jigsaw_block_dy,
                                                        finalNextProjection);

                                                JigsawJunction next_junction = new JigsawJunction(raw_jigsaw_pos.getX(),
                                                        root_junction_y_offset - raw_pos_y + next_y_offset,
                                                        raw_jigsaw_pos.getZ(), -jigsaw_block_dy, finalRootProjection);

                                                current_root.addJunction(root_junction);
                                                next_piece.addJunction(next_junction);
                                            }

                                            this.pieces.add(next_piece);
                                            if (new_depth <= this.maxDepth)
                                            {
                                                next_piece_state.used_jigsaws.add(raw_jigsaw_pos);
                                                next_piece_state.used_jigsaws.add(next_pos_raw);

                                                root_state.used_jigsaws.add(raw_jigsaw_pos);
                                                root_state.used_jigsaws.add(next_pos_raw);

                                                this.placing.addLast(next_piece_state);
                                            }
                                        }));
                                        continue root_jigsaws;
                                    }
                                    else wrong_attach++;
                                }
                            }
                            if (this.logs) PokecubeAPI.LOGGER.debug(
                                    "Skipping {} as did not fit: root_conflict:{} duplicated:{} no_attachment:{} box_conflicts:{}",
                                    next_picked_element, bounds_fails, duplicated_fails, wrong_attach, vanilla_fails);
                        }
                    }
                    else
                    {
                        PokecubeAPI.LOGGER.warn("Empty or non-existent fallback pool: {} in {}", resourcelocation1,
                                config.name);
                    }
                }
                else
                {
                    PokecubeAPI.LOGGER.warn("Empty or non-existent pool: {} in {}", next_pool_name, config.name);
                }
            }
            // this should ensure that we run the rigids first, then do the
            // others after.
            newParts.sort((o1, o2) -> o1.compareTo(o2));
            // Now we run them in order.
            newParts.forEach(part -> part.run.run());
        }
    }
}
