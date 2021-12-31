package pokecube.core.world.gen.jigsaw;

import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import org.apache.commons.lang3.mutable.MutableObject;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.block.JigsawBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.StructureSettings;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.StructureFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.structures.EmptyPoolElement;
import net.minecraft.world.level.levelgen.feature.structures.JigsawJunction;
import net.minecraft.world.level.levelgen.feature.structures.JigsawPlacement;
import net.minecraft.world.level.levelgen.feature.structures.StructurePoolElement;
import net.minecraft.world.level.levelgen.feature.structures.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGeneratorSupplier.Context;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import pokecube.core.PokecubeCore;
import pokecube.core.database.worldgen.WorldgenHandler;
import pokecube.core.database.worldgen.WorldgenHandler.JigSawConfig;
import pokecube.core.interfaces.PokecubeMod;
import thut.core.common.ThutCore;

public class JigsawAssmbler
{
    static final class Entry
    {
        private final PoolElementStructurePiece villagePiece;
        private final MutableObject<VoxelShape> shapeReference;
        private final int index;
        private final int depth;

        private Entry(final PoolElementStructurePiece villagePieceIn, final MutableObject<VoxelShape> shapeReference,
                final int index, final int depth)
        {
            this.villagePiece = villagePieceIn;
            this.shapeReference = shapeReference;
            this.index = index;
            this.depth = depth;
        }
    }

    public static ServerLevel getForGen(final ChunkGenerator chunkGen)
    {
        final MinecraftServer server = ThutCore.proxy.getServer();
        for (final ServerLevel w : server.getAllLevels()) if (w.getChunkSource().getGenerator() == chunkGen) return w;
        throw new IllegalStateException("Did not find a world for this chunk generator!");
    }

    private JigsawPlacement.PieceFactory pieceFactory;

    private int depth;
    private ChunkGenerator chunkGenerator;
    private StructureManager templateManager;
    private List<StructurePiece> parts = Lists.newArrayList();
    private Random rand;

    private Predicate<StructurePoolElement> validator = p -> true;

    private final Deque<JigsawAssmbler.Entry> availablePieces = Queues.newArrayDeque();

    private Heightmap.Types SURFACE_TYPE = Heightmap.Types.WORLD_SURFACE_WG;

    private final Set<String> once_added = Sets.newHashSet();
    private final Set<String> needed_once = Sets.newHashSet();

    private final Set<BoundingBox> conflict_check = Sets.newHashSet();
    private final Set<ChunkPos> checked_chunks = Sets.newHashSet();

    private final JigSawConfig config;

    private LevelHeightAccessor heightAccess;

    private boolean checkConflicts = false;

    final ResourceLocation structName;
    StructureFeature<?> thisFeature;

    public JigsawAssmbler(final JigSawConfig config)
    {
        this.config = config;
        structName = new ResourceLocation(config.type.isEmpty() ? config.name : config.type);
        thisFeature = WorldgenHandler.getFeature(structName);
    }

    public JigsawAssmbler checkConflicts()
    {
        this.checkConflicts = true;
        return this;
    }

    private void init(final int depth, final JigsawPlacement.PieceFactory pieceFactory,
            final ChunkGenerator chunkGenerator, final StructureManager templateManagerIn, final Random rand)
    {
        this.depth = depth;
        this.pieceFactory = pieceFactory;
        this.chunkGenerator = chunkGenerator;
        this.templateManager = templateManagerIn;
        this.rand = rand;
        this.once_added.clear();
        this.needed_once.clear();
        this.needed_once.addAll(this.config.needed_once);
    }

    private StructureTemplatePool init(final RegistryAccess regAccess, final ResourceLocation pool)
    {
        final Registry<StructureTemplatePool> registry = regAccess.registryOrThrow(Registry.TEMPLATE_POOL_REGISTRY);
        return registry.get(pool);
    }

    public Optional<PieceGenerator<JigsawConfig>> build(Context<JigsawConfig> context,
            BiConsumer<PieceGenerator.Context<JigsawConfig>, List<StructurePiece>> postProcessor)
    {
        int y = context.config().struct_config.minY;
        BlockPos pos = new BlockPos(context.chunkPos().getMinBlockX() + 7, y, context.chunkPos().getMinBlockZ() + 7);
        return this.build(context, postProcessor, pos, -1, c -> true);
    }

    public Optional<PieceGenerator<JigsawConfig>> build(Context<JigsawConfig> context,
            BiConsumer<PieceGenerator.Context<JigsawConfig>, List<StructurePiece>> postProcessor, BlockPos pos,
            int default_k, final Predicate<StructurePoolElement> isValid)
    {
        Pools.bootstrap();
        StructureFeature.bootstrap();

        RegistryAccess dynamicRegistryManager = context.registryAccess();
        ResourceLocation resourceLocationIn = new ResourceLocation(context.config().struct_config.root);
        JigsawPlacement.PieceFactory pieceFactory = PoolElementStructurePiece::new;
        ChunkGenerator chunkGenerator = context.chunkGenerator();
        StructureManager templateManagerIn = context.structureManager();
        WorldgenRandom worldgenrandom = new WorldgenRandom(new LegacyRandomSource(0L));
        LevelHeightAccessor heightAccessor = context.heightAccessor();

        worldgenrandom.setLargeFeatureSeed(context.seed(), context.chunkPos().x, context.chunkPos().z);

        boolean built = build(dynamicRegistryManager, resourceLocationIn, context.config().struct_config.size,
                pieceFactory, chunkGenerator, templateManagerIn, pos, worldgenrandom, isValid, default_k,
                heightAccessor);

        LegacyRandomSource rand = new LegacyRandomSource(0);

        int n = 1;

        int maxN = 5;
        while (!built && n++ < maxN)
        {
            worldgenrandom.setLargeFeatureSeed(rand.nextLong(), context.chunkPos().x, context.chunkPos().z);
            built = build(dynamicRegistryManager, resourceLocationIn, default_k, pieceFactory, chunkGenerator,
                    templateManagerIn, pos, worldgenrandom, isValid, default_k, heightAccessor);
        }
        if (n > 1) PokecubeMod.LOGGER.warn(n + " iterations of build for: " + context.config().struct_config.name);

        if (parts.isEmpty() || n > maxN) return Optional.empty();

        int max_h = JigsawAssmbler.getForGen(chunkGenerator).dimensionType().logicalHeight() - 5;

        int y_shift = 0;
        for (StructurePiece p : parts)
        {
            if (p.getBoundingBox().maxY > max_h) y_shift = Math.min(y_shift, max_h - p.getBoundingBox().maxY);
        }

        if (y_shift != 0)
        {
            PokecubeMod.LOGGER.debug("Shifting {} down by {} ", context.config().struct_config.name, y_shift);
            for (StructurePiece p : parts) p.move(0, y_shift, 0);
        }

        return Optional.of((builder, context_) -> {
            postProcessor.accept(context_, parts);
            parts.forEach(builder::addPiece);
        });
    }

    private boolean build(final RegistryAccess dynamicRegistryManager, final ResourceLocation resourceLocationIn,
            final int depth, final JigsawPlacement.PieceFactory pieceFactory, final ChunkGenerator chunkGenerator,
            final StructureManager templateManagerIn, final BlockPos pos, final Random rand,
            final Predicate<StructurePoolElement> isValid, final int default_k,
            final LevelHeightAccessor heightAccessor)
    {
        return this.build(this.init(dynamicRegistryManager, resourceLocationIn), Rotation.getRandom(rand), depth,
                pieceFactory, chunkGenerator, templateManagerIn, pos, rand, default_k, isValid, heightAccessor);
    }

    private StructurePoolElement getRandomTemplate(StructureTemplatePool pool, Random rand)
    {
        StructurePoolElement element = pool.getRandomTemplate(rand);
        return element;
    }

    private boolean build(final StructureTemplatePool pool, final Rotation rotation, final int depth,
            final JigsawPlacement.PieceFactory pieceFactory, final ChunkGenerator chunkGenerator,
            final StructureManager templateManagerIn, final BlockPos pos, final Random rand, final int default_k,
            final Predicate<StructurePoolElement> isValid, final LevelHeightAccessor heightAccessor)
    {
        this.validator = isValid;
        this.heightAccess = heightAccessor;
        if (PokecubeCore.getConfig().debug) PokecubeCore.LOGGER.debug("Jigsaw starting build");
        this.init(depth, pieceFactory, chunkGenerator, templateManagerIn, rand);

        if (this.config.water || this.config.air)
            this.SURFACE_TYPE = this.config.water ? Types.OCEAN_FLOOR_WG : Types.WORLD_SURFACE_WG;
        else if (!this.config.surface) this.SURFACE_TYPE = null;

        final StructurePoolElement jigsawpiece = getRandomTemplate(pool, rand);
        final PoolElementStructurePiece poolElement = pieceFactory.create(templateManagerIn, jigsawpiece, pos,
                jigsawpiece.getGroundLevelDelta(), rotation,
                jigsawpiece.getBoundingBox(templateManagerIn, pos, rotation));

        final BoundingBox boundingBox = poolElement.getBoundingBox();
        final int i = (boundingBox.maxX + boundingBox.minX()) / 2;
        final int j = (boundingBox.maxZ + boundingBox.minZ()) / 2;
        int k = default_k;
        // If we have not been provided with a default value, determine where to
        // place the ground for the structure
        if (k == -1)
        {
            final int variance = this.config.variance <= 0 ? 0 : rand.nextInt(this.config.variance);
            // Air spawns are a somewhat random distance above the surface
            // reference.
            if (this.config.air)
                k = chunkGenerator.getFirstFreeHeight(i, j, this.SURFACE_TYPE, this.heightAccess) + variance;
            // If we have a surface reference, then lets use that
            else if (this.SURFACE_TYPE != null)
                k = chunkGenerator.getFirstFreeHeight(i, j, this.SURFACE_TYPE, this.heightAccess);
            else
            {
                // Otherwise, pick a random value below ground
                k = this.chunkGenerator.getFirstFreeHeight(i, j, Heightmap.Types.OCEAN_FLOOR_WG, this.heightAccess);
                if (k > 0) k = this.rand.nextInt(k + 1);
                else k = chunkGenerator.getSeaLevel();
            }
        }
        // Ensure it is placed in range
        if (k <= 0 || k >= chunkGenerator.getGenDepth())
        {
            k = chunkGenerator.getGenDepth();
            k = this.rand.nextInt(k + 1);
        }
        final int dy = -this.config.height + boundingBox.minY() + poolElement.getGroundLevelDelta();
        poolElement.move(0, k - dy, 0);

        if (!this.add(poolElement)) return false;

        if (depth > 0)
        {
            final int dr = 80;
            final int dh = 255;
            final AABB axisalignedbb = new AABB(i - dr, k - dr, j - dh, i + dr + 1, k + dh + 1, j + dr + 1);
            this.availablePieces
                    .addLast(new Entry(poolElement, new MutableObject<>(Shapes.join(Shapes.create(axisalignedbb),
                            Shapes.create(AABB.of(boundingBox)), BooleanOp.ONLY_FIRST)), k + dh, 0));
            while (!this.availablePieces.isEmpty())
            {
                final Entry jigsawmanager$entry = this.availablePieces.removeFirst();
                this.addPiece(jigsawmanager$entry.villagePiece, jigsawmanager$entry.shapeReference,
                        jigsawmanager$entry.index, jigsawmanager$entry.depth, default_k);
            }
        }
        final List<String> guarenteed = Lists.newArrayList(this.needed_once);
        for (final StructurePiece part : parts)
        {
            if (!(part instanceof PoolElementStructurePiece)) continue;
            final PoolElementStructurePiece jig = (PoolElementStructurePiece) part;
            if (!(jig.getElement() instanceof CustomJigsawPiece)) continue;
            final CustomJigsawPiece piece = (CustomJigsawPiece) jig.getElement();
            if (piece.opts != null) guarenteed.remove(piece.opts.flag);
        }
        return guarenteed.isEmpty() && !parts.isEmpty();
    }

    private boolean add(StructurePiece part)
    {
        if (checkConflicts && thisFeature != null)
        {
            BoundingBox box = part.getBoundingBox();
            int dx = Math.min(16, box.getXSpan() / 4);
            int dz = Math.min(16, box.getZSpan() / 4);

            dx = Math.max(dx, 1);
            dz = Math.max(dz, 1);

            for (int i = box.minX; i < box.maxX; i += dx) for (int j = box.minX; j < box.maxX; j += dz)
            {
                int x = SectionPos.blockToSectionCoord(i);
                int z = SectionPos.blockToSectionCoord(j);

                ChunkPos pos = new ChunkPos(x, z);
                if (checked_chunks.contains(pos)) continue;
                checked_chunks.add(pos);

                // Here we check if there are any conflicting structures around.

                final ServerLevel world = JigsawAssmbler.getForGen(chunkGenerator);
                world.getChunkSource();
                final StructureFeatureManager sfmanager = world.structureFeatureManager();
                final StructureSettings settings = chunkGenerator.getSettings();

                // We ask for EMPTY chunk, and allow it to be null, so
                // that
                // we don't cause issues if the chunk doesn't exist yet.
                final ChunkAccess ichunk = world.getChunk(x, z, ChunkStatus.EMPTY, false);
                // We then only care about chunks which have already
                // reached
                // at least this stage of loading.
                if (ichunk == null || !ichunk.getStatus().isOrAfter(ChunkStatus.STRUCTURE_STARTS)) continue;
                if (!ichunk.hasAnyStructureReferences()) continue;

                for (final StructureFeature<?> s : WorldgenHandler.getSortedList())
                {
                    // We shouldn't be conflicting with ourself
                    if (s.getRegistryName().equals(structName)) continue;

                    final StructureFeatureConfiguration structureseparationsettings = settings.getConfig(s);
                    // This means it doesn't spawn in this world, so we skip.
                    if (structureseparationsettings == null) continue;
                    // This is the way to tell if an actual real structure
                    // would be at this location.
                    final StructureStart<?> structurestart = sfmanager.getStartForFeature(SectionPos.bottomOf(ichunk),
                            s, ichunk);
                    // This means we do conflict, so no spawn here.
                    if (structurestart != null && structurestart.isValid())
                    {
                        conflict_check.add(structurestart.getBoundingBox());
                    }
                }
            }
            for (BoundingBox b : conflict_check)
            {
                if (b.intersects(box)) return false;
            }
        }
        return parts.add(part);
    }

    private void sort(final List<StructurePoolElement> list)
    {
        final List<StructurePoolElement> needed = Lists.newArrayList();
        list.removeIf(p -> !this.validator.test(p));
        list.removeIf(p -> p instanceof CustomJigsawPiece p2 && this.once_added.contains(p2.opts.flag));
        for (final StructurePoolElement p : list)
            if (p instanceof CustomJigsawPiece p2 && !p2.opts.flag.isEmpty() && this.needed_once.contains(p2.opts.flag))
                needed.add(p);
        list.removeIf(p -> needed.contains(p));
        Collections.shuffle(needed, this.rand);
        for (final StructurePoolElement p : needed) list.add(0, p);
        final LevelAccessor world = JigsawAssmbler.getForGen(this.chunkGenerator);
        list.forEach(p -> {
            if (p instanceof CustomJigsawPiece p2)
            {
                p2.config = this.config;
                p2.world = (Level) world;
            }
        });
    }

    @SuppressWarnings("deprecation")
    private void addPiece(final PoolElementStructurePiece villagePieceIn, final MutableObject<VoxelShape> outer_box_ref,
            final int part_index, int current_depth, final int default_k)
    {
        final StructurePoolElement part = villagePieceIn.getElement();

        final BlockPos blockpos = villagePieceIn.getPosition();
        final Rotation rotation = villagePieceIn.getRotation();
        final StructureTemplatePool.Projection projection = part.getProjection();
        final boolean root_rigid = projection == StructureTemplatePool.Projection.RIGID;
        final MutableObject<VoxelShape> new_box_ref = new MutableObject<>();
        final BoundingBox part_box = villagePieceIn.getBoundingBox();
        final int part_min_y = part_box.minY();

        int k0 = default_k;

        if (k0 == -1 && this.SURFACE_TYPE == null)
        {
            k0 = this.chunkGenerator.getFirstFreeHeight(blockpos.getX(), blockpos.getZ(),
                    Heightmap.Types.OCEAN_FLOOR_WG, this.heightAccess);
            if (k0 > 0) k0 = this.rand.nextInt(k0 + 1);
            else k0 = -1;
        }

        jigsaws:
        for (final StructureTemplate.StructureBlockInfo jigsaw_block : part
                .getShuffledJigsawBlocks(this.templateManager, blockpos, rotation, this.rand))
        {
            final Direction jig_dir = JigsawBlock.getFrontFacing(jigsaw_block.state);
            final BlockPos jig_pos = jigsaw_block.pos;
            final BlockPos jig_target = jig_pos.relative(jig_dir);
            final int jig_min_y = jig_pos.getY() - part_min_y;
            int k = k0;
            final StructureTemplatePool pool = BuiltinRegistries.TEMPLATE_POOL
                    .get(new ResourceLocation(jigsaw_block.nbt.getString("pool")));
            if (pool == null)
            {
                PokecubeCore.LOGGER.error(jigsaw_block.nbt.getString("pool") + " is a null pool! " + jigsaw_block.nbt);
                continue;
            }
            final StructureTemplatePool pool_pattern = BuiltinRegistries.TEMPLATE_POOL.get(pool.getFallback());
            if (pool.size() != 0 || pool.getName().equals(Pools.EMPTY.location()))
            {
                final boolean target_in_box = part_box.isInside(jig_target);
                MutableObject<VoxelShape> box_ref;
                int l;
                if (target_in_box)
                {
                    box_ref = new_box_ref;
                    l = part_min_y;
                    if (new_box_ref.getValue() == null) new_box_ref.setValue(Shapes.create(AABB.of(part_box)));
                }
                else
                {
                    box_ref = outer_box_ref;
                    l = part_index;
                }

                final List<StructurePoolElement> list = Lists.newArrayList();
                if (current_depth != this.depth) list.addAll(pool.getShuffledTemplates(this.rand));
                list.addAll(pool_pattern.getShuffledTemplates(this.rand));
                this.sort(list);

                for (final StructurePoolElement next_part : list)
                {

                    boolean allowEmpty = rand.nextDouble() > 0.99;
                    boolean isEmpty = next_part == EmptyPoolElement.INSTANCE;

                    if (isEmpty && allowEmpty) break;
                    else if (isEmpty) continue;

                    String once = "";

                    if (next_part instanceof CustomJigsawPiece
                            && !(once = ((CustomJigsawPiece) next_part).opts.flag).isEmpty()
                            && this.once_added.contains(once))
                        continue;

                    for (final Rotation dir : Rotation.getShuffled(this.rand))
                    {
                        final List<StructureTemplate.StructureBlockInfo> next_jigsaws = next_part
                                .getShuffledJigsawBlocks(this.templateManager, BlockPos.ZERO, dir, this.rand);
                        final BoundingBox next_box = next_part.getBoundingBox(this.templateManager, BlockPos.ZERO, dir);
                        int i1;
                        if (next_box.getYSpan() > 16) i1 = 0;
                        else i1 = next_jigsaws.stream().mapToInt((blockInfo) -> {
                            if (!next_box.isInside(
                                    blockInfo.pos.relative(blockInfo.state.getValue(JigsawBlock.ORIENTATION).front())))
                                return 0;
                            else
                            {
                                final ResourceLocation pool_loc = new ResourceLocation(blockInfo.nbt.getString("pool"));
                                final StructureTemplatePool next_pool = BuiltinRegistries.TEMPLATE_POOL.get(pool_loc);
                                if (next_pool == null)
                                {
                                    PokecubeCore.LOGGER.error(pool_loc + " is null! "
                                            + jigsaw_block.nbt.getString("pool") + " " + jigsaw_block.nbt);
                                    return 0;
                                }
                                final StructureTemplatePool fallback_pool = BuiltinRegistries.TEMPLATE_POOL
                                        .get(next_pool.getFallback());
                                return Math.max(next_pool.getMaxSize(this.templateManager),
                                        fallback_pool.getMaxSize(this.templateManager));
                            }
                        }).max().orElse(0);

                        for (final StructureTemplate.StructureBlockInfo next_jigsaw : next_jigsaws)
                            if (JigsawBlock.canAttach(jigsaw_block, next_jigsaw))
                        {
                            final BlockPos next_pos = next_jigsaw.pos;
                            final BlockPos dr_0 = new BlockPos(jig_target.getX() - next_pos.getX(),
                                    jig_target.getY() - next_pos.getY(), jig_target.getZ() - next_pos.getZ());
                            final BoundingBox box_1 = next_part.getBoundingBox(this.templateManager, dr_0, dir);
                            final int next_min_y = box_1.minY();
                            final StructureTemplatePool.Projection placementRule = next_part.getProjection();
                            final boolean rigid = placementRule == StructureTemplatePool.Projection.RIGID;
                            final int target_y = next_pos.getY();
                            final int l1 = jig_min_y - target_y
                                    + JigsawBlock.getFrontFacing(jigsaw_block.state).getStepY();
                            int i2;
                            if (root_rigid && rigid) i2 = part_min_y + l1;
                            else
                            {
                                if (k == -1) k = this.chunkGenerator.getFirstFreeHeight(jig_pos.getX(), jig_pos.getZ(),
                                        this.SURFACE_TYPE, this.heightAccess);
                                i2 = k - target_y;
                            }

                            final int next_rel_y = i2 - next_min_y;
                            final BoundingBox box_2 = box_1.moved(0, next_rel_y, 0);
                            final BlockPos dr_1 = dr_0.offset(0, next_rel_y, 0);
                            if (i1 > 0)
                            {
                                final int k2 = Math.max(i1 + 1, box_2.maxY() - box_2.minY());
                                box_2.encapsulate(new BlockPos(box_2.minX(), box_2.minY() + k2, box_2.minZ()));
                            }

                            if (!Shapes.joinIsNotEmpty(box_ref.getValue(), Shapes.create(AABB.of(box_2).deflate(0.25D)),
                                    BooleanOp.ONLY_SECOND))
                            {
                                box_ref.setValue(Shapes.joinUnoptimized(box_ref.getValue(),
                                        Shapes.create(AABB.of(box_2)), BooleanOp.ONLY_FIRST));
                                final int j3 = villagePieceIn.getGroundLevelDelta();
                                int l2;
                                if (rigid) l2 = j3 - l1;
                                else l2 = next_part.getGroundLevelDelta();

                                final PoolElementStructurePiece nextPart = this.pieceFactory
                                        .create(this.templateManager, next_part, dr_1, l2, dir, box_2);
                                int i3;
                                if (root_rigid) i3 = part_min_y + jig_min_y;
                                else if (rigid) i3 = i2 + target_y;
                                else
                                {
                                    if (k == -1) k = this.chunkGenerator.getFirstFreeHeight(jig_pos.getX(),
                                            jig_pos.getZ(), this.SURFACE_TYPE, this.heightAccess);
                                    i3 = k + l1 / 2;
                                }
                                if (this.add(nextPart))
                                {
                                    villagePieceIn.addJunction(new JigsawJunction(jig_target.getX(),
                                            i3 - jig_min_y + j3, jig_target.getZ(), l1, placementRule));
                                    nextPart.addJunction(new JigsawJunction(jig_pos.getX(), i3 - target_y + l2,
                                            jig_pos.getZ(), -l1, projection));
                                    if (!once.isEmpty())
                                    {
                                        this.once_added.add(once);
                                        if (PokecubeCore.getConfig().debug)
                                            PokecubeCore.LOGGER.debug("added core part: {}", once);
                                    }
                                    int depth = current_depth + 1;
                                    if (nextPart.getElement() instanceof CustomJigsawPiece p2 && p2.opts.needs_children)
                                        depth = 0;
                                    if (depth <= this.depth)
                                        this.availablePieces.addLast(new Entry(nextPart, box_ref, l, depth));
                                    continue jigsaws;
                                }
                            }
                        }
                    }
                }
            }
            else PokecubeCore.LOGGER.warn("Empty or none existent pool: {}", jigsaw_block.nbt.getString("target_pool"));
        }
    }
}
