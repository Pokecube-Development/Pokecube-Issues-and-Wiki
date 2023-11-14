package pokecube.world.gen.structures.utils;

import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
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
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.JigsawBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.JigsawBlockEntity.JointType;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGeneratorSupplier;
import net.minecraft.world.level.levelgen.structure.pools.EmptyPoolElement;
import net.minecraft.world.level.levelgen.structure.pools.JigsawJunction;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import pokecube.api.PokecubeAPI;
import pokecube.mixin.accessors.ChunkAccessAcessor;
import pokecube.mixin.accessors.WorldGenRegionAccessor;
import pokecube.world.gen.structures.GenericJigsawStructure;
import pokecube.world.gen.structures.configs.ExpandedJigsawConfiguration;
import pokecube.world.gen.structures.pool_elements.ExpandedJigsawPiece;
import thut.core.common.ThutCore;

public class ExpandedJigsawPacement
{
    public static final Set<ResourceLocation> ALWAYS_ADD = Sets.newHashSet();

    static
    {
        ALWAYS_ADD.add(new ResourceLocation("pokecube:village/common/trainers"));
    }

    public static ServerLevel getForGen(final PieceGeneratorSupplier.Context<?> context)
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

    public static Optional<PieceGenerator<ExpandedJigsawConfiguration>> addPieces(
            PieceGeneratorSupplier.Context<ExpandedJigsawConfiguration> context, PieceFactory factory, BlockPos centre,
            boolean bound_checks, boolean on_surface)
    {
        WorldgenRandom worldgenrandom = new WorldgenRandom(new LegacyRandomSource(0L));
        worldgenrandom.setLargeFeatureSeed(context.seed(), context.chunkPos().x, context.chunkPos().z);
        Rotation rotation = Rotation.getRandom(worldgenrandom);
        return addPieces(context, factory, centre, bound_checks, on_surface, worldgenrandom, rotation);
    }

    public static Optional<PieceGenerator<ExpandedJigsawConfiguration>> addPieces(
            PieceGeneratorSupplier.Context<ExpandedJigsawConfiguration> context, PieceFactory factory, BlockPos centre,
            boolean bound_checks, boolean on_surface, WorldgenRandom worldgenrandom, Rotation rotation)
    {
        RegistryAccess registryaccess = context.registryAccess();
        ExpandedJigsawConfiguration config = context.config();
        ChunkGenerator chunkgenerator = context.chunkGenerator();
        StructureManager structuremanager = context.structureManager();
        LevelHeightAccessor levelheightaccessor = context.heightAccessor();
        Predicate<Holder<Biome>> predicate = context.validBiome();
        StructureFeature.bootstrap();
        Registry<StructureTemplatePool> registry = registryaccess.registryOrThrow(Registry.TEMPLATE_POOL_REGISTRY);

        StructureTemplatePool root_pool = config.startPool().value();
        // This one can be completely random, as is just the start pool, this
        // shouldn't have any requirements...
        StructurePoolElement root_element = root_pool.getRandomTemplate(worldgenrandom);

        if (root_element == EmptyPoolElement.INSTANCE)
        {
            return Optional.empty();
        }
        else
        {
            PoolElementStructurePiece root_piece = factory.create(structuremanager, root_element, centre,
                    root_element.getGroundLevelDelta(), rotation,
                    root_element.getBoundingBox(structuremanager, centre, rotation));
            BoundingBox boundingbox = root_piece.getBoundingBox();
            int i = (boundingbox.maxX() + boundingbox.minX()) / 2;
            int j = (boundingbox.maxZ() + boundingbox.minZ()) / 2;

            int root_dy = config.y_settings.vertical_offset;
            int ground_y = 0;

            if (config.air)
            {
                ground_y = chunkgenerator.getFirstFreeHeight(i, j, config.height_type, levelheightaccessor);
                ground_y = Math.max(config.y_settings.min_y, ground_y);
                if (config.y_settings.vertical_offset > 0)
                    root_dy = config.y_settings.dy_offset + worldgenrandom.nextInt(config.y_settings.vertical_offset);
            }
            else if (config.underground)
            {
                ground_y = chunkgenerator.getFirstFreeHeight(i, j, config.height_type, levelheightaccessor);
                ground_y = Math.min(config.y_settings.max_y, ground_y);
                if (config.y_settings.vertical_offset > 0)
                    root_dy = -config.y_settings.dy_offset - worldgenrandom.nextInt(config.y_settings.vertical_offset);
            }
            else if (on_surface)
            {
                ground_y = chunkgenerator.getFirstFreeHeight(i, j, config.height_type, levelheightaccessor);
                root_dy = config.y_settings.vertical_offset;
            }
            int k = centre.getY() + root_dy + ground_y;

            if (!predicate.test(
                    chunkgenerator.getNoiseBiome(QuartPos.fromBlock(i), QuartPos.fromBlock(k), QuartPos.fromBlock(j))))
            {
                return Optional.empty();
            }
            else
            {
                int l = boundingbox.minY() + root_piece.getGroundLevelDelta();
                root_piece.move(0, k - l, 0);
                return Optional.of((builder, config_context) -> {
                    if (config.maxDepth() > 0)
                    {
                        int max_box_size = 80;
                        int max_box_height = 512;
                        AABB aabb = new AABB(i - max_box_size, k - max_box_height, j - max_box_size,
                                i + max_box_size + 1, k + max_box_height + 1, j + max_box_size + 1);

                        List<Placer> attempts = Lists.newArrayList();

                        if (ThutCore.conf.debug) PokecubeAPI.logDebug("Building: {}", root_pool.getName());
                        tries:
                        for (int n = 0; n < 10; n++)
                        {
                            if (ThutCore.conf.debug) PokecubeAPI.logDebug("Starting Try: {}", n);
                            List<PoolElementStructurePiece> list = Lists.newArrayList();
                            list.add(root_piece);
                            Placer placer = new Placer(config, registry, config.maxDepth(), factory, chunkgenerator,
                                    structuremanager, list, worldgenrandom);
                            attempts.add(placer);

                            MutableObject<VoxelShape> bounds = new MutableObject<>(Shapes.join(Shapes.create(aabb),
                                    Shapes.create(AABB.of(boundingbox)), BooleanOp.ONLY_FIRST));

                            PieceState next_state = new PieceState(root_piece, bounds, 0);
                            placer.placing.addLast(next_state);

                            while (!placer.placing.isEmpty())
                            {
                                next_state = placer.placing.removeFirst();
                                placer.tryPlacingChildren(next_state, bound_checks, levelheightaccessor);
                            }

                            if (ThutCore.conf.debug) PokecubeAPI.logDebug("Ended Try: {}", n);
                            if (placer.needed_once.isEmpty()) break;
                            for (String s : placer.needed_once)
                            {
                                if (!placer.added_once.contains(s))
                                {
                                    if (ThutCore.conf.debug)
                                        PokecubeAPI.logDebug("Try: {} has failed. Missing {}", n, s);
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

                        if (ThutCore.conf.debug) PokecubeAPI.logDebug("Finshed: {}", root_pool.getName());
                        PostProcessor.POSTPROCESS.accept(context, list);
                        list.forEach(builder::addPiece);

                        GenericJigsawStructure.markPlaced(context);
                    }
                });
            }
        }
    }

    public static void addPieces(RegistryAccess reg_access, PoolElementStructurePiece root_piece, int max_depth,
            PieceFactory factory, ChunkGenerator chunk_gen, StructureManager structure_manager,
            List<? super PoolElementStructurePiece> pieces, Random random, LevelHeightAccessor height_accessor)
    {
        Registry<StructureTemplatePool> registry = reg_access.registryOrThrow(Registry.TEMPLATE_POOL_REGISTRY);
        Placer placer = new Placer(null, registry, max_depth, factory, chunk_gen, structure_manager, pieces, random);
        placer.placing.addLast(new PieceState(root_piece, new MutableObject<>(Shapes.INFINITY), 0));

        while (!placer.placing.isEmpty())
        {
            ExpandedJigsawPacement.PieceState piece = placer.placing.removeFirst();
            placer.tryPlacingChildren(piece, false, height_accessor);
        }
    }

    public interface PieceFactory
    {
        PoolElementStructurePiece create(StructureManager p_210301_, StructurePoolElement p_210302_, BlockPos p_210303_,
                int p_210304_, Rotation p_210305_, BoundingBox p_210306_);
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

    static final class Placer
    {
        final Registry<StructureTemplatePool> pools;
        final int maxDepth;
        final ExpandedJigsawPacement.PieceFactory factory;
        final ChunkGenerator chunkGenerator;
        final StructureManager structureManager;
        final List<? super PoolElementStructurePiece> pieces;
        final Random random;
        final Set<String> needed_once;
        final Set<String> added_once = Sets.newHashSet();
        final ExpandedJigsawConfiguration config;
        final Deque<ExpandedJigsawPacement.PieceState> placing = Queues.newArrayDeque();
        MutableObject<VoxelShape> rigid_bounds = new MutableObject<>();
        MutableObject<VoxelShape> non_rigid_bounds = new MutableObject<>();

        boolean logs = false;

        Placer(ExpandedJigsawConfiguration config, Registry<StructureTemplatePool> pools, int max_depth,
                ExpandedJigsawPacement.PieceFactory factory, ChunkGenerator chunkGenerator,
                StructureManager structureManager, List<? super PoolElementStructurePiece> pieces, Random random)
        {
            this.pools = pools;
            this.maxDepth = max_depth;
            this.factory = factory;
            this.chunkGenerator = chunkGenerator;
            this.structureManager = structureManager;
            this.pieces = pieces;
            this.random = random;
            this.needed_once = Sets.newHashSet(config.required_parts);
            this.config = config;
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

        public boolean canAttach(StructureTemplate.StructureBlockInfo root, StructureTemplate.StructureBlockInfo next)
        {
            Direction root_front = JigsawBlock.getFrontFacing(root.state);
            Direction next_front = JigsawBlock.getFrontFacing(next.state);

            boolean correct_direction = root_front == next_front.getOpposite();

            if (!correct_direction && logs) PokecubeAPI.logDebug("wrong direction");
            if (!correct_direction) return false;

            Direction root_top = JigsawBlock.getTopFacing(root.state);
            Direction next_top = JigsawBlock.getTopFacing(next.state);
            JointType jointtype = JointType.byName(root.nbt.getString("joint")).orElseGet(() -> {
                return root_front.getAxis().isHorizontal() ? JointType.ALIGNED : JointType.ROLLABLE;
            });

            boolean correct_orientation = jointtype == JointType.ROLLABLE || root_top == next_top;
            if (!correct_orientation && logs) PokecubeAPI.logDebug("wrong orientation");

            if (!correct_orientation) return false;

            boolean tag_match = root.nbt.getString("target").equals(next.nbt.getString("name"));

            if (!tag_match && logs)
                PokecubeAPI.logDebug(root.nbt.getString("target") + "!=" + next.nbt.getString("name"));

            return tag_match;
        }

        @SuppressWarnings("deprecation")
        void tryPlacingChildren(PieceState root_state, boolean bound_check, LevelHeightAccessor heightmap)
        {
            PoolElementStructurePiece current_root = root_state.piece;
            MutableObject<VoxelShape> free = root_state.free;
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
                boolean alwaysAdd = ALWAYS_ADD.contains(next_pool_name);
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
                        for (StructurePoolElement next_picked_element : getShuffledParts(depth, next_pool,
                                fallback_pool))
                        {
                            if (next_picked_element == EmptyPoolElement.INSTANCE) break;

                            this.logs = log_data.test(next_picked_element);

                            int rigid_fails = 0;
                            int bounds_fails = 0;
                            int non_rigid_fails = 0;
                            int duplicated_fails = 0;
                            int wrong_attach = 0;

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

                                if (this.logs) PokecubeAPI.logInfo(next_jigsaws);

                                pick_jigsaws:
                                for (StructureBlockInfo next_block_info : next_jigsaws)
                                {
                                    if (alwaysAdd || canAttach(root_block_info, next_block_info))
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
                                                        raw_jigsaw_pos.getZ(), heightmap$types, heightmap);
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

                                        valid_checks:
                                        {
                                            if (alwaysAdd) break valid_checks;

                                            // Unless we are actually inside, do
                                            // not
                                            // allow intersections with the root
                                            // box.
                                            if (!next_pick_inside)
                                            {
                                                if (test_box.intersects(root_box))
                                                {
                                                    bounds_fails++;
                                                    continue pick_jigsaws;
                                                }
                                            }

                                            // Never allow intersection of rigid
                                            // objects.
                                            if (rigid_bounds.getValue() != null)
                                            {
                                                VoxelShape new_shape = Shapes.create(test_box);
                                                if (Shapes.joinIsNotEmpty(rigid_bounds.getValue(), new_shape,
                                                        BooleanOp.AND))
                                                {
                                                    rigid_fails++;
                                                    continue pick_jigsaws;
                                                }
                                            }

                                            // And then don't let non-rigids
                                            // overlap
                                            // each other
                                            if (non_rigid_bounds.getValue() != null)
                                            {
                                                VoxelShape new_shape = Shapes.create(test_box);
                                                if (Shapes.joinIsNotEmpty(non_rigid_bounds.getValue(), new_shape,
                                                        BooleanOp.AND))
                                                {
                                                    non_rigid_fails++;
                                                    continue pick_jigsaws;
                                                }
                                            }

                                            // If we are rigid, add the boundary
                                            // now, so we don't conflict with
                                            // future possible additions
                                            if (next_pick_rigid)
                                            {
                                                // If it was rigid, add it to
                                                // the rigid bounds
                                                AABB next_box = AABB.of(next_pick_box_shifted_y)
                                                        .expandTowards(0, -room_below, 0)
                                                        .inflate(h_clearance, v_clearance, h_clearance);
                                                VoxelShape new_shape = Shapes.create(next_box);

                                                if (rigid_bounds.getValue() != null)
                                                {
                                                    rigid_bounds.setValue(Shapes.joinUnoptimized(
                                                            rigid_bounds.getValue(), new_shape, BooleanOp.OR));
                                                }
                                                else
                                                {
                                                    rigid_bounds.setValue(new_shape);
                                                }
                                            }
                                        }

                                        if (this.logs)
                                            PokecubeAPI.logDebug("Placing Sub Part! {}", next_picked_element);

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

                                        PoolElementStructurePiece next_piece = this.factory.create(
                                                this.structureManager, next_picked_element, blockpos5, next_y_offset,
                                                random_direction, next_pick_box_shifted_y);

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
                                            if (k == -1)
                                            {
                                                k = this.chunkGenerator.getFirstFreeHeight(raw_jigsaw_pos.getX(),
                                                        raw_jigsaw_pos.getZ(), heightmap$types, heightmap);
                                            }
                                            root_junction_y_offset = k + jigsaw_block_dy / 2;
                                        }

                                        boolean addJunctions = parent_junctions;

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
                                            for (String s : p._flags) if (needed_once.contains(s)) added_once.add(s);
                                            addJunctions = parent_junctions || !p.bool_config.no_affect_noise;
                                        }
                                        // The junctions are used for little
                                        // islands under the jigsaws, this
                                        // should allow having entire sections
                                        // without them.
                                        if (addJunctions)
                                        {
                                            JigsawJunction root_junction = new JigsawJunction(
                                                    connecting_jigsaw_pos.getX(),
                                                    root_junction_y_offset - dy + root_y_offset,
                                                    connecting_jigsaw_pos.getZ(), jigsaw_block_dy, next_projection);

                                            JigsawJunction next_junction = new JigsawJunction(raw_jigsaw_pos.getX(),
                                                    root_junction_y_offset - raw_pos_y + next_y_offset,
                                                    raw_jigsaw_pos.getZ(), -jigsaw_block_dy, root_projection);

                                            current_root.addJunction(root_junction);
                                            next_piece.addJunction(next_junction);
                                        }

                                        int new_depth = depth + 1 + depth_offset;

                                        this.pieces.add(next_piece);
                                        if (new_depth <= this.maxDepth)
                                        {
                                            PieceState next_piece_state = new PieceState(next_piece, free, new_depth);
                                            next_piece_state.used_jigsaws.add(raw_jigsaw_pos);
                                            next_piece_state.used_jigsaws.add(next_pos_raw);

                                            root_state.used_jigsaws.add(raw_jigsaw_pos);
                                            root_state.used_jigsaws.add(next_pos_raw);

                                            this.placing.addLast(next_piece_state);
                                        }
                                        continue root_jigsaws;
                                    }
                                    else wrong_attach++;
                                }
                            }
                            if (this.logs) PokecubeAPI.logDebug(
                                    "Skipping {} as did not fit: rigid_conflict:{} non_rigid_conflict:{} root_conflict:{} duplicated:{} no_attachment:{}",
                                    next_picked_element, rigid_fails, non_rigid_fails, bounds_fails, duplicated_fails,
                                    wrong_attach);
                        }
                    }
                    else
                    {
                        PokecubeAPI.LOGGER.warn("Empty or non-existent fallback pool: {}", resourcelocation1);
                    }
                }
                else
                {
                    PokecubeAPI.LOGGER.warn("Empty or non-existent pool: {}", next_pool_name);
                }
            }

            // After adding in all the sub parts, then we actually update the
            // bounding boxes if it wasn't rigid.
            int non_rigid_clearance = 0;
            if (!root_rigid)
            {
                AABB next_box = AABB.of(root_bounding_box).inflate(0, non_rigid_clearance, 0);
                VoxelShape new_shape = Shapes.create(next_box);
                if (non_rigid_bounds.getValue() != null)
                {
                    non_rigid_bounds
                            .setValue(Shapes.joinUnoptimized(non_rigid_bounds.getValue(), new_shape, BooleanOp.OR));
                }
                else
                {
                    non_rigid_bounds.setValue(new_shape);
                }
            }
            if (root_rigid)
            {
                // If it was rigid, add it to the
                // rigid bounds, but ignoring clearances, as we need sub-parts
                // to attach
                AABB next_box = AABB.of(root_bounding_box);
                VoxelShape new_shape = Shapes.create(next_box);
                if (rigid_bounds.getValue() != null)
                {
                    rigid_bounds.setValue(Shapes.joinUnoptimized(rigid_bounds.getValue(), new_shape, BooleanOp.OR));
                }
                else
                {
                    rigid_bounds.setValue(new_shape);
                }
            }
        }
    }
}
