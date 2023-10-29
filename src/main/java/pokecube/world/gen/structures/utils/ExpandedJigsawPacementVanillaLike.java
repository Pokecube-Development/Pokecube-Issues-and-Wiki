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
import org.apache.logging.log4j.Logger;

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
import pokecube.api.PokecubeAPI;
import pokecube.world.gen.structures.configs.ExpandedJigsawConfiguration;
import pokecube.world.gen.structures.pool_elements.ExpandedJigsawPiece;

public class ExpandedJigsawPacementVanillaLike
{
    static final Logger LOGGER = PokecubeAPI.LOGGER;

    public static Optional<PieceGenerator<ExpandedJigsawConfiguration>> addPieces(
            PieceGeneratorSupplier.Context<ExpandedJigsawConfiguration> context, PieceFactory factory, BlockPos centre,
            boolean bound_checks, boolean on_surface)
    {
        WorldgenRandom worldgenrandom = new WorldgenRandom(new LegacyRandomSource(0L));
        worldgenrandom.setLargeFeatureSeed(context.seed(), context.chunkPos().x, context.chunkPos().z);
        RegistryAccess registryaccess = context.registryAccess();
        ExpandedJigsawConfiguration config = context.config();
        ChunkGenerator chunkgenerator = context.chunkGenerator();
        StructureManager structuremanager = context.structureManager();
        LevelHeightAccessor levelheightaccessor = context.heightAccessor();
        Predicate<Holder<Biome>> predicate = context.validBiome();
        StructureFeature.bootstrap();
        Registry<StructureTemplatePool> registry = registryaccess.registryOrThrow(Registry.TEMPLATE_POOL_REGISTRY);
        Rotation rotation = Rotation.getRandom(worldgenrandom);
        StructureTemplatePool structuretemplatepool = config.startPool().value();
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
            int k;
            if (on_surface)
            {
                k = centre.getY() + chunkgenerator.getFirstFreeHeight(i, j, Heightmap.Types.WORLD_SURFACE_WG,
                        levelheightaccessor);
            }
            else
            {
                k = centre.getY();
            }

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

                    int max_box_size = 80;
                    int max_box_height = 80;
                    AABB aabb = new AABB(i - max_box_size, k - max_box_height,
                            j - max_box_size, i + max_box_size + 1,
                            k + max_box_height + 1, j + max_box_size + 1);

                    if (config.maxDepth() > 0)
                    {
                        tries:
                        for (int n = 0; n < 100; n++)
                        {
                            list.add(poolelementstructurepiece);
                            Placer placer = new Placer(config, registry, config.maxDepth(), factory, chunkgenerator,
                                    structuremanager, list, worldgenrandom);
                            placer.placing
                                    .addLast(
                                            new PieceState(poolelementstructurepiece,
                                                    new MutableObject<>(Shapes.join(Shapes.create(aabb),
                                                            Shapes.create(AABB.of(boundingbox)), BooleanOp.ONLY_FIRST)),
                                                    0));

                            while (!placer.placing.isEmpty())
                            {
                                PieceState jigsawplacement$piecestate = placer.placing.removeFirst();
                                placer.tryPlacingChildren(jigsawplacement$piecestate.piece,
                                        jigsawplacement$piecestate.free, jigsawplacement$piecestate.depth, bound_checks,
                                        levelheightaccessor);
                            }
                            if (placer.needed_once.isEmpty()) break;
                            for (String s : placer.needed_once)
                            {
                                if (!placer.added_once.contains(s)) continue tries;
                            }
                            break;
                        }
                    }
                    PostProcessor.POSTPROCESS.accept(context, list);
                    list.forEach(builder::addPiece);
                });
            }
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

        PieceState(PoolElementStructurePiece p_210311_, MutableObject<VoxelShape> p_210312_, int p_210313_)
        {
            this.piece = p_210311_;
            this.free = p_210312_;
            this.depth = p_210313_;
        }
    }

    static final class Placer
    {
        private final Registry<StructureTemplatePool> pools;
        private final int maxDepth;
        private final PieceFactory factory;
        private final ChunkGenerator chunkGenerator;
        private final StructureManager structureManager;
        private final List<? super PoolElementStructurePiece> pieces;
        final ExpandedJigsawConfiguration config;
        private final Random random;
        final Deque<PieceState> placing = Queues.newArrayDeque();
        final Set<String> needed_once;
        final Set<String> added_once = Sets.newHashSet();

        Placer(ExpandedJigsawConfiguration config, Registry<StructureTemplatePool> pools, int maxDepth,
                PieceFactory factory, ChunkGenerator chunkGenerator, StructureManager structureManager,
                List<? super PoolElementStructurePiece> pieces, Random random)
        {
            this.pools = pools;
            this.maxDepth = maxDepth;
            this.factory = factory;
            this.chunkGenerator = chunkGenerator;
            this.structureManager = structureManager;
            this.pieces = pieces;
            this.random = random;
            this.config = config;
            this.needed_once = Sets.newHashSet(config.required_parts);
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
        void tryPlacingChildren(PoolElementStructurePiece root_pool, MutableObject<VoxelShape> bounds_holder, int depth,
                boolean bound_check, LevelHeightAccessor level)
        {
            StructurePoolElement root_element = root_pool.getElement();
            BlockPos blockpos = root_pool.getPosition();
            Rotation rotation = root_pool.getRotation();
            StructureTemplatePool.Projection root_projection = root_element.getProjection();
            boolean root_rigid = root_projection == StructureTemplatePool.Projection.RIGID;
            MutableObject<VoxelShape> mutableobject = new MutableObject<>();
            BoundingBox boundingbox = root_pool.getBoundingBox();
            int i = boundingbox.minY();

            root_jigsaws:
            for (StructureTemplate.StructureBlockInfo root_block_info : this.getShuffledJigsaws(root_element, blockpos,
                    rotation))
            {
                Direction direction = JigsawBlock.getFrontFacing(root_block_info.state);
                BlockPos raw_jigsaw_pos = root_block_info.pos;
                BlockPos connecting_jigsaw_pos = raw_jigsaw_pos.relative(direction);
                int j = raw_jigsaw_pos.getY() - i;
                int k = -1;
                ResourceLocation next_loc = new ResourceLocation(root_block_info.nbt.getString("pool"));
                Optional<StructureTemplatePool> next_pool = this.pools.getOptional(next_loc);
                if (next_pool.isPresent()
                        && (next_pool.get().size() != 0 || Objects.equals(next_loc, Pools.EMPTY.location())))
                {
                    ResourceLocation fallback_loc = next_pool.get().getFallback();
                    Optional<StructureTemplatePool> fallback_pool = this.pools.getOptional(fallback_loc);
                    if (fallback_pool.isPresent() && (fallback_pool.get().size() != 0
                            || Objects.equals(fallback_loc, Pools.EMPTY.location())))
                    {
                        boolean flag1 = boundingbox.isInside(connecting_jigsaw_pos);
                        MutableObject<VoxelShape> mutableobject1;
                        if (flag1)
                        {
                            mutableobject1 = mutableobject;
                            if (mutableobject.getValue() == null)
                            {
                                mutableobject.setValue(Shapes.create(AABB.of(boundingbox)));
                            }
                        }
                        else
                        {
                            mutableobject1 = bounds_holder;
                        }

                        List<StructurePoolElement> list = Lists.newArrayList();

                        list = this.getShuffledParts(depth, next_pool, fallback_pool);

                        for (StructurePoolElement next_picked_element : list)
                        {
                            if (next_picked_element == EmptyPoolElement.INSTANCE)
                            {
                                break;
                            }

                            for (Rotation rotation1 : Rotation.getShuffled(this.random))
                            {
                                List<StructureTemplate.StructureBlockInfo> list1 = next_picked_element
                                        .getShuffledJigsawBlocks(this.structureManager, BlockPos.ZERO, rotation1,
                                                this.random);
                                BoundingBox boundingbox1 = next_picked_element.getBoundingBox(this.structureManager,
                                        BlockPos.ZERO, rotation1);
                                int l;
                                if (bound_check && boundingbox1.getYSpan() <= 16)
                                {
                                    l = list1.stream().mapToInt((p_210332_) -> {
                                        if (!boundingbox1.isInside(
                                                p_210332_.pos.relative(JigsawBlock.getFrontFacing(p_210332_.state))))
                                        {
                                            return 0;
                                        }
                                        else
                                        {
                                            ResourceLocation resourcelocation2 = new ResourceLocation(
                                                    p_210332_.nbt.getString("pool"));
                                            Optional<StructureTemplatePool> optional2 = this.pools
                                                    .getOptional(resourcelocation2);
                                            Optional<StructureTemplatePool> optional3 = optional2
                                                    .flatMap((p_210344_) ->
                                                    {
                                                        return this.pools.getOptional(p_210344_.getFallback());
                                                    });
                                            int j3 = optional2.map((p_210342_) -> {
                                                return p_210342_.getMaxSize(this.structureManager);
                                            }).orElse(0);
                                            int k3 = optional3.map((p_210340_) -> {
                                                return p_210340_.getMaxSize(this.structureManager);
                                            }).orElse(0);
                                            return Math.max(j3, k3);
                                        }
                                    }).max().orElse(0);
                                }
                                else
                                {
                                    l = 0;
                                }

                                for (StructureTemplate.StructureBlockInfo target_block_info : list1)
                                {
                                    if (JigsawBlock.canAttach(root_block_info, target_block_info))
                                    {
                                        BlockPos blockpos3 = target_block_info.pos;
                                        BlockPos blockpos4 = connecting_jigsaw_pos.subtract(blockpos3);
                                        BoundingBox boundingbox2 = next_picked_element
                                                .getBoundingBox(this.structureManager, blockpos4, rotation1);
                                        int i1 = boundingbox2.minY();
                                        StructureTemplatePool.Projection next_piece_projection = next_picked_element
                                                .getProjection();
                                        boolean flag2 = next_piece_projection == StructureTemplatePool.Projection.RIGID;
                                        int j1 = blockpos3.getY();
                                        int k1 = j - j1 + JigsawBlock.getFrontFacing(root_block_info.state).getStepY();
                                        int l1;
                                        if (root_rigid && flag2)
                                        {
                                            l1 = i + k1;
                                        }
                                        else
                                        {
                                            if (k == -1)
                                            {
                                                k = this.chunkGenerator.getFirstFreeHeight(raw_jigsaw_pos.getX(),
                                                        raw_jigsaw_pos.getZ(), Heightmap.Types.WORLD_SURFACE_WG, level);
                                            }

                                            l1 = k - j1;
                                        }

                                        int i2 = l1 - i1;
                                        BoundingBox boundingbox3 = boundingbox2.moved(0, i2, 0);
                                        BlockPos blockpos5 = blockpos4.offset(0, i2, 0);
                                        if (l > 0)
                                        {
                                            int j2 = Math.max(l + 1, boundingbox3.maxY() - boundingbox3.minY());
                                            boundingbox3.encapsulate(new BlockPos(boundingbox3.minX(),
                                                    boundingbox3.minY() + j2, boundingbox3.minZ()));
                                        }

                                        if (!Shapes.joinIsNotEmpty(mutableobject1.getValue(),
                                                Shapes.create(AABB.of(boundingbox3).deflate(0.25D)),
                                                BooleanOp.ONLY_SECOND))
                                        {
                                            mutableobject1.setValue(Shapes.joinUnoptimized(mutableobject1.getValue(),
                                                    Shapes.create(AABB.of(boundingbox3)), BooleanOp.ONLY_FIRST));
                                            int i3 = root_pool.getGroundLevelDelta();
                                            int k2;
                                            if (flag2)
                                            {
                                                k2 = i3 - k1;
                                            }
                                            else
                                            {
                                                k2 = next_picked_element.getGroundLevelDelta();
                                            }

                                            PoolElementStructurePiece next_piece = this.factory.create(
                                                    this.structureManager, next_picked_element, blockpos5, k2,
                                                    rotation1, boundingbox3);
                                            int l2;
                                            if (root_rigid)
                                            {
                                                l2 = i + j;
                                            }
                                            else if (flag2)
                                            {
                                                l2 = l1 + j1;
                                            }
                                            else
                                            {
                                                if (k == -1)
                                                {
                                                    k = this.chunkGenerator.getFirstFreeHeight(raw_jigsaw_pos.getX(),
                                                            raw_jigsaw_pos.getZ(), Heightmap.Types.WORLD_SURFACE_WG,
                                                            level);
                                                }

                                                l2 = k + k1 / 2;
                                            }

                                            root_pool.addJunction(
                                                    new JigsawJunction(connecting_jigsaw_pos.getX(), l2 - j + i3,
                                                            connecting_jigsaw_pos.getZ(), k1, next_piece_projection));
                                            next_piece.addJunction(new JigsawJunction(raw_jigsaw_pos.getX(),
                                                    l2 - j1 + k2, raw_jigsaw_pos.getZ(), -k1, root_projection));
                                            this.pieces.add(next_piece);

                                            // Now we mark as added if we have
                                            // flags
                                            if (next_picked_element instanceof ExpandedJigsawPiece p)
                                            {
                                                if (p.bool_config.only_once) for (String s : p._flags) added_once.add(s);
                                                // Mark it as added if we needed
                                                // this part.
                                                for (String s : p._flags)
                                                    if (needed_once.contains(s)) added_once.add(s);
                                            }

                                            if (depth + 1 <= this.maxDepth)
                                            {
                                                this.placing
                                                        .addLast(new PieceState(next_piece, mutableobject1, depth + 1));
                                            }
                                            continue root_jigsaws;
                                        }
                                    }
                                }
                            }
                        }
                    }
                    else
                    {
                        LOGGER.warn("Empty or non-existent fallback pool: {}", fallback_loc);
                    }
                }
                else
                {
                    LOGGER.warn("Empty or non-existent pool: {}", next_loc);
                }
            }

        }
    }
}