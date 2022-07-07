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
import org.slf4j.Logger;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;

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
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import pokecube.core.PokecubeCore;
import pokecube.world.gen.structures.configs.ExpandedJigsawConfiguration;
import pokecube.world.gen.structures.pool_elements.ExpandedJigsawPiece;
import thut.core.common.ThutCore;

public class ExpandedJigsawPacement
{
    static final Logger LOGGER = LogUtils.getLogger();

    public static ServerLevel getForGen(final ChunkGenerator chunkGen)
    {
        final MinecraftServer server = ThutCore.proxy.getServer();
        for (final ServerLevel w : server.getAllLevels()) if (w.getChunkSource().getGenerator() == chunkGen) return w;
        throw new IllegalStateException("Did not find a world for this chunk generator!");
    }

    public static Optional<PieceGenerator<ExpandedJigsawConfiguration>> addPieces(
            PieceGeneratorSupplier.Context<ExpandedJigsawConfiguration> context,
            ExpandedJigsawPacement.PieceFactory factory, BlockPos centre, boolean bound_checks, boolean on_surface)
    {
        WorldgenRandom worldgenrandom = new WorldgenRandom(new LegacyRandomSource(0L));
        worldgenrandom.setLargeFeatureSeed(context.seed(), context.chunkPos().x, context.chunkPos().z);
        RegistryAccess registryaccess = context.registryAccess();
        ExpandedJigsawConfiguration jigsawconfiguration = context.config();
        ChunkGenerator chunkgenerator = context.chunkGenerator();
        StructureManager structuremanager = context.structureManager();
        LevelHeightAccessor levelheightaccessor = context.heightAccessor();
        Predicate<Holder<Biome>> predicate = context.validBiome();
        Set<String> needed_once = Sets.newHashSet(jigsawconfiguration.required_parts);
        StructureFeature.bootstrap();
        Registry<StructureTemplatePool> registry = registryaccess.registryOrThrow(Registry.TEMPLATE_POOL_REGISTRY);
        Rotation rotation = Rotation.getRandom(worldgenrandom);

        StructureTemplatePool structuretemplatepool = jigsawconfiguration.startPool().value();
        // This one can be completely random, as is just the start pool, this
        // shouldn't have any requirements...
        StructurePoolElement structurepoolelement = structuretemplatepool.getRandomTemplate(worldgenrandom);

        if (structurepoolelement == EmptyPoolElement.INSTANCE)
        {
            return Optional.empty();
        }
        else
        {
            PoolElementStructurePiece poolelementstructurepiece = factory.create(structuremanager, structurepoolelement,
                    centre, structurepoolelement.getGroundLevelDelta(), rotation,
                    structurepoolelement.getBoundingBox(structuremanager, centre, rotation));
            BoundingBox boundingbox = poolelementstructurepiece.getBoundingBox();
            int i = (boundingbox.maxX() + boundingbox.minX()) / 2;
            int j = (boundingbox.maxZ() + boundingbox.minZ()) / 2;
            int dk = jigsawconfiguration.vertical_offset;
            if (on_surface)
            {
                dk = chunkgenerator.getFirstFreeHeight(i, j, jigsawconfiguration.height_type, levelheightaccessor)
                        + jigsawconfiguration.vertical_offset;
            }
            int k = centre.getY() + dk;

            if (!predicate.test(
                    chunkgenerator.getNoiseBiome(QuartPos.fromBlock(i), QuartPos.fromBlock(k), QuartPos.fromBlock(j))))
            {
                return Optional.empty();
            }
            else
            {
                int l = boundingbox.minY() + poolelementstructurepiece.getGroundLevelDelta();
                poolelementstructurepiece.move(0, k - l, 0);
                return Optional.of((builder, config_context) -> {
                    List<PoolElementStructurePiece> list = Lists.newArrayList();
                    list.add(poolelementstructurepiece);
                    if (jigsawconfiguration.maxDepth() > 0)
                    {
                        int max_box_size = 80;
                        AABB aabb = new AABB((double) (i - max_box_size), (double) (k - max_box_size),
                                (double) (j - max_box_size), (double) (i + max_box_size + 1),
                                (double) (k + max_box_size + 1), (double) (j + max_box_size + 1));
                        ExpandedJigsawPacement.Placer ModifiedJigsawPacement$placer = new ExpandedJigsawPacement.Placer(
                                registry, jigsawconfiguration.maxDepth(), factory, chunkgenerator, structuremanager,
                                list, worldgenrandom, needed_once);
                        ModifiedJigsawPacement$placer.placing
                                .addLast(
                                        new ExpandedJigsawPacement.PieceState(poolelementstructurepiece,
                                                new MutableObject<>(Shapes.join(Shapes.create(aabb),
                                                        Shapes.create(AABB.of(boundingbox)), BooleanOp.ONLY_FIRST)),
                                                0));

                        while (!ModifiedJigsawPacement$placer.placing.isEmpty())
                        {
                            ExpandedJigsawPacement.PieceState ModifiedJigsawPacement$piecestate = ModifiedJigsawPacement$placer.placing
                                    .removeFirst();
                            ModifiedJigsawPacement$placer.tryPlacingChildren(ModifiedJigsawPacement$piecestate.piece,
                                    ModifiedJigsawPacement$piecestate.free, ModifiedJigsawPacement$piecestate.depth,
                                    bound_checks, levelheightaccessor);
                        }

                        PostProcessor.POSTPROCESS.accept(config_context, list);
                        list.forEach(builder::addPiece);

                        List<AABB> aabbs = ModifiedJigsawPacement$placer.rigid_bounds.getValue().toAabbs();
                        System.out.println("rigids: " + aabbs.size());
                        System.out.println(aabbs);
                    }
                });
            }
        }
    }

    public static void addPieces(RegistryAccess reg_access, PoolElementStructurePiece root_piece, int max_depth,
            ExpandedJigsawPacement.PieceFactory factory, ChunkGenerator chunk_gen, StructureManager structure_manager,
            List<? super PoolElementStructurePiece> pieces, Random random, LevelHeightAccessor height_accessor)
    {
        Registry<StructureTemplatePool> registry = reg_access.registryOrThrow(Registry.TEMPLATE_POOL_REGISTRY);
        ExpandedJigsawPacement.Placer ModifiedJigsawPacement$placer = new ExpandedJigsawPacement.Placer(registry,
                max_depth, factory, chunk_gen, structure_manager, pieces, random, Sets.newHashSet());
        ModifiedJigsawPacement$placer.placing
                .addLast(new ExpandedJigsawPacement.PieceState(root_piece, new MutableObject<>(Shapes.INFINITY), 0));

        while (!ModifiedJigsawPacement$placer.placing.isEmpty())
        {
            ExpandedJigsawPacement.PieceState ModifiedJigsawPacement$piecestate = ModifiedJigsawPacement$placer.placing
                    .removeFirst();
            ModifiedJigsawPacement$placer.tryPlacingChildren(ModifiedJigsawPacement$piecestate.piece,
                    ModifiedJigsawPacement$piecestate.free, ModifiedJigsawPacement$piecestate.depth, false,
                    height_accessor);
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
        private final Registry<StructureTemplatePool> pools;
        private final int maxDepth;
        private final ExpandedJigsawPacement.PieceFactory factory;
        private final ChunkGenerator chunkGenerator;
        private final StructureManager structureManager;
        private final List<? super PoolElementStructurePiece> pieces;
        private final Random random;
        private final Set<String> needed_once;
        private final Set<String> added_once = Sets.newHashSet();
        final Deque<ExpandedJigsawPacement.PieceState> placing = Queues.newArrayDeque();
        MutableObject<VoxelShape> rigid_bounds = new MutableObject<>();
        MutableObject<VoxelShape> non_rigid_bounds = new MutableObject<>();

        Placer(Registry<StructureTemplatePool> pools, int max_depth, ExpandedJigsawPacement.PieceFactory factory,
                ChunkGenerator chunkGenerator, StructureManager structureManager,
                List<? super PoolElementStructurePiece> pieces, Random random, Set<String> needed_once)
        {
            this.pools = pools;
            this.maxDepth = max_depth;
            this.factory = factory;
            this.chunkGenerator = chunkGenerator;
            this.structureManager = structureManager;
            this.pieces = pieces;
            this.random = random;
            this.needed_once = needed_once;
        }

        List<StructureTemplate.StructureBlockInfo> getShuffledJigsaws(StructurePoolElement structurepoolelement,
                BlockPos blockpos, Rotation rotation)
        {
            List<StructureTemplate.StructureBlockInfo> shuffled = structurepoolelement
                    .getShuffledJigsawBlocks(this.structureManager, blockpos, rotation, this.random);
            return shuffled;
        }

        List<StructurePoolElement> getShuffledParts(int depth, Optional<StructureTemplatePool> target_pool,
                Optional<StructureTemplatePool> fallback_pool)
        {
            List<StructurePoolElement> list = Lists.newArrayList();
            if (depth != this.maxDepth)
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
                                    need = true;
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

        @SuppressWarnings("deprecation")
        void tryPlacingChildren(PoolElementStructurePiece piece, MutableObject<VoxelShape> free, int depth,
                boolean bound_check, LevelHeightAccessor heightmap)
        {
            StructurePoolElement root_element = piece.getElement();
            BlockPos blockpos = piece.getPosition();
            Rotation rotation = piece.getRotation();
            StructureTemplatePool.Projection structuretemplatepool$projection = root_element.getProjection();
            boolean root_rigid = structuretemplatepool$projection == StructureTemplatePool.Projection.RIGID;
            MutableObject<VoxelShape> mutableobject = new MutableObject<>();
            BoundingBox root_bounding_box = piece.getBoundingBox();
            int root_min_y = root_bounding_box.minY();
            boolean should_check_single_bounds;

            if (root_element instanceof ExpandedJigsawPiece p)
            {
                should_check_single_bounds = p.bound_check;
            }
            else should_check_single_bounds = true;

            int non_rigid_clearance = 10;

            if (!root_rigid && non_rigid_bounds.getValue() == null)
            {
                non_rigid_bounds.setValue(Shapes.create(AABB.of(root_bounding_box).inflate(0, non_rigid_clearance, 0)));
            }

            root_jigsaws:
            for (StructureTemplate.StructureBlockInfo structuretemplate$structureblockinfo : this
                    .getShuffledJigsaws(root_element, blockpos, rotation))
            {
                Direction direction = JigsawBlock.getFrontFacing(structuretemplate$structureblockinfo.state);
                BlockPos raw_jigsaw_pos = structuretemplate$structureblockinfo.pos;
                BlockPos connecting_jigsaw_pos = raw_jigsaw_pos.relative(direction);
                int j = raw_jigsaw_pos.getY() - root_min_y;
                int k = -1;
                ResourceLocation resourcelocation = new ResourceLocation(
                        structuretemplate$structureblockinfo.nbt.getString("pool"));
                Optional<StructureTemplatePool> target_pool = this.pools.getOptional(resourcelocation);
                if (target_pool.isPresent()
                        && (target_pool.get().size() != 0 || Objects.equals(resourcelocation, Pools.EMPTY.location())))
                {
                    ResourceLocation resourcelocation1 = target_pool.get().getFallback();
                    Optional<StructureTemplatePool> fallback_pool = this.pools.getOptional(resourcelocation1);
                    if (fallback_pool.isPresent() && (fallback_pool.get().size() != 0
                            || Objects.equals(resourcelocation1, Pools.EMPTY.location())))
                    {
                        boolean jigsaw_inside_root = root_bounding_box.isInside(connecting_jigsaw_pos);
                        MutableObject<VoxelShape> previous_bounding_boxes;
                        MutableObject<VoxelShape> coll_box = new MutableObject<>();
                        if (jigsaw_inside_root && should_check_single_bounds)
                        {
                            previous_bounding_boxes = mutableobject;
                            if (mutableobject.getValue() == null)
                            {
                                mutableobject.setValue(Shapes.create(AABB.of(root_bounding_box)));
                            }
                        }
                        else if (!should_check_single_bounds)
                        {
                            coll_box.setValue(Shapes.create(AABB.of(root_bounding_box).deflate(0.25d)));
                            previous_bounding_boxes = free;
                        }
                        else
                        {
                            previous_bounding_boxes = free;
                        }

                        for (StructurePoolElement next_picked_element : getShuffledParts(depth, target_pool,
                                fallback_pool))
                        {
                            if (next_picked_element == EmptyPoolElement.INSTANCE) break;
                            for (Rotation rotation1 : Rotation.getShuffled(this.random))
                            {
                                List<StructureTemplate.StructureBlockInfo> list1 = next_picked_element
                                        .getShuffledJigsawBlocks(this.structureManager, BlockPos.ZERO, rotation1,
                                                this.random);
                                BoundingBox picked_box = next_picked_element.getBoundingBox(this.structureManager,
                                        BlockPos.ZERO, rotation1);
                                int l;
                                if (bound_check && picked_box.getYSpan() <= 16)
                                {
                                    l = list1.stream().mapToInt((structure_info) -> {
                                        if (!picked_box.isInside(structure_info.pos
                                                .relative(JigsawBlock.getFrontFacing(structure_info.state))))
                                        {
                                            return 0;
                                        }
                                        else
                                        {
                                            ResourceLocation resourcelocation2 = new ResourceLocation(
                                                    structure_info.nbt.getString("pool"));
                                            Optional<StructureTemplatePool> optional2 = this.pools
                                                    .getOptional(resourcelocation2);
                                            Optional<StructureTemplatePool> optional3 = optional2.flatMap((pool) -> {
                                                return this.pools.getOptional(pool.getFallback());
                                            });
                                            int j3 = optional2.map((pool) -> {
                                                return pool.getMaxSize(this.structureManager);
                                            }).orElse(0);
                                            int k3 = optional3.map((pool) -> {
                                                return pool.getMaxSize(this.structureManager);
                                            }).orElse(0);
                                            return Math.max(j3, k3);
                                        }
                                    }).max().orElse(0);
                                }
                                else
                                {
                                    l = 0;
                                }
                                next_pick:
                                for (StructureTemplate.StructureBlockInfo structuretemplate$structureblockinfo1 : list1)
                                {
                                    if (JigsawBlock.canAttach(structuretemplate$structureblockinfo,
                                            structuretemplate$structureblockinfo1))
                                    {
                                        BlockPos blockpos3 = structuretemplate$structureblockinfo1.pos;
                                        BlockPos next_jigsaw_pos = connecting_jigsaw_pos.subtract(blockpos3);
                                        BoundingBox next_pick_box = next_picked_element
                                                .getBoundingBox(this.structureManager, next_jigsaw_pos, rotation1);
                                        int i1 = next_pick_box.minY();
                                        StructureTemplatePool.Projection structuretemplatepool$projection1 = next_picked_element
                                                .getProjection();
                                        boolean next_pick_rigid = structuretemplatepool$projection1 == StructureTemplatePool.Projection.RIGID;
                                        int j1 = blockpos3.getY();
                                        int k1 = j - j1 + JigsawBlock
                                                .getFrontFacing(structuretemplate$structureblockinfo.state).getStepY();
                                        int l1;
                                        if (root_rigid && next_pick_rigid)
                                        {
                                            l1 = root_min_y + k1;
                                        }
                                        else
                                        {
                                            if (k == -1)
                                            {
                                                k = this.chunkGenerator.getFirstFreeHeight(raw_jigsaw_pos.getX(),
                                                        raw_jigsaw_pos.getZ(), Heightmap.Types.WORLD_SURFACE_WG,
                                                        heightmap);
                                            }
                                            l1 = k - j1;
                                        }

                                        int i2 = l1 - i1;
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

                                        // Some space below to prevent roads
                                        // going too close under buildings.
                                        int room_below = 5;
                                        if (next_picked_element instanceof ExpandedJigsawPiece p)
                                        {
                                            room_below = p.space_below;
                                        }

                                        // Never allow intersection of rigid
                                        // objects.
                                        if (rigid_bounds.getValue() != null)
                                        {
                                            AABB test_box = AABB.of(next_pick_box_shifted_y).deflate(0.25D);
                                            VoxelShape new_shape = Shapes.create(test_box);
                                            if (Shapes.joinIsNotEmpty(rigid_bounds.getValue(), new_shape,
                                                    BooleanOp.AND))
                                                continue next_pick;
                                        }
                                        // And then don't let non-rigids overlap
                                        // each other
                                        if (non_rigid_bounds.getValue() != null && !next_pick_rigid)
                                        {
                                            AABB test_box = AABB.of(next_pick_box_shifted_y).deflate(0.25D);
                                            VoxelShape new_shape = Shapes.create(test_box);
                                            if (Shapes.joinIsNotEmpty(non_rigid_bounds.getValue(), new_shape,
                                                    BooleanOp.AND))
                                                continue next_pick;
                                        }

                                        if (next_pick_rigid)
                                        {
                                            // If it was rigid, add it to the
                                            // rigid bounds
                                            AABB next_box = AABB.of(next_pick_box_shifted_y).expandTowards(0,
                                                    room_below, 0);
                                            VoxelShape new_shape = Shapes.create(next_box);
                                            previous_bounding_boxes
                                                    .setValue(Shapes.joinUnoptimized(previous_bounding_boxes.getValue(),
                                                            new_shape, BooleanOp.ONLY_FIRST));
                                            if (rigid_bounds.getValue() != null)
                                            {
                                                rigid_bounds.setValue(Shapes.or(rigid_bounds.getValue(), new_shape));
                                            }
                                            else
                                            {
                                                rigid_bounds.setValue(new_shape);
                                            }
                                        }
                                        else
                                        {
                                            // Otherwise add to the non-rigids
                                            AABB next_box = AABB.of(next_pick_box_shifted_y).inflate(0,
                                                    non_rigid_clearance, 0);
                                            VoxelShape new_shape = Shapes.create(next_box);
                                            if (non_rigid_bounds.getValue() != null)
                                            {
                                                non_rigid_bounds
                                                        .setValue(Shapes.or(non_rigid_bounds.getValue(), new_shape));
                                            }
                                            else
                                            {
                                                non_rigid_bounds.setValue(new_shape);
                                            }
                                        }

                                        int i3 = piece.getGroundLevelDelta();
                                        int k2;
                                        if (next_pick_rigid)
                                        {
                                            k2 = i3 - k1;
                                        }
                                        else
                                        {
                                            k2 = next_picked_element.getGroundLevelDelta();
                                        }

                                        PoolElementStructurePiece poolelementstructurepiece = this.factory.create(
                                                this.structureManager, next_picked_element, blockpos5, k2, rotation1,
                                                next_pick_box_shifted_y);

                                        int l2;
                                        if (root_rigid)
                                        {
                                            l2 = root_min_y + j;
                                        }
                                        else if (next_pick_rigid)
                                        {
                                            l2 = l1 + j1;
                                        }
                                        else
                                        {
                                            if (k == -1)
                                            {
                                                k = this.chunkGenerator.getFirstFreeHeight(raw_jigsaw_pos.getX(),
                                                        raw_jigsaw_pos.getZ(), Heightmap.Types.WORLD_SURFACE_WG,
                                                        heightmap);
                                            }
                                            l2 = k + k1 / 2;
                                        }

                                        piece.addJunction(new JigsawJunction(connecting_jigsaw_pos.getX(), l2 - j + i3,
                                                connecting_jigsaw_pos.getZ(), k1, structuretemplatepool$projection1));
                                        poolelementstructurepiece
                                                .addJunction(new JigsawJunction(raw_jigsaw_pos.getX(), l2 - j1 + k2,
                                                        raw_jigsaw_pos.getZ(), -k1, structuretemplatepool$projection));

                                        this.pieces.add(poolelementstructurepiece);

                                        if (next_picked_element instanceof ExpandedJigsawPiece p)
                                        {
                                            if (p.only_once) for (String s : p._flags) added_once.add(s);
                                            // Mark it as added if we needed
                                            // this part.
                                            for (String s : p._flags) if (needed_once.contains(s)) added_once.add(s);
                                        }
                                        if (depth + 1 <= this.maxDepth)
                                        {
                                            this.placing.addLast(new ExpandedJigsawPacement.PieceState(
                                                    poolelementstructurepiece, previous_bounding_boxes, depth + 1));
                                        }
                                        continue root_jigsaws;
                                    }
                                }
                            }
                        }
                    }
                    else
                    {
                        PokecubeCore.LOGGER.warn("Empty or non-existent fallback pool: {}", (Object) resourcelocation1);
                    }
                }
                else
                {
                    PokecubeCore.LOGGER.warn("Empty or non-existent pool: {}", (Object) resourcelocation);
                }
            }

        }
    }
}
