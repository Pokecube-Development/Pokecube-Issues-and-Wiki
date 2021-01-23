package pokecube.core.world.gen.jigsaw;

import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;

import net.minecraft.block.JigsawBlock;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.MutableRegistry;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.Heightmap.Type;
import net.minecraft.world.gen.feature.jigsaw.EmptyJigsawPiece;
import net.minecraft.world.gen.feature.jigsaw.JigsawJunction;
import net.minecraft.world.gen.feature.jigsaw.JigsawManager;
import net.minecraft.world.gen.feature.jigsaw.JigsawPattern;
import net.minecraft.world.gen.feature.jigsaw.JigsawPatternRegistry;
import net.minecraft.world.gen.feature.jigsaw.JigsawPiece;
import net.minecraft.world.gen.feature.structure.AbstractVillagePiece;
import net.minecraft.world.gen.feature.structure.StructurePiece;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.gen.feature.template.TemplateManager;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.LogicalSidedProvider;
import pokecube.core.PokecubeCore;
import pokecube.core.database.worldgen.WorldgenHandler.JigSawConfig;

public class JigsawAssmbler
{
    static final class Entry
    {
        private final AbstractVillagePiece        villagePiece;
        private final AtomicReference<VoxelShape> shapeReference;
        private final int                         index;
        private final int                         depth;

        private Entry(final AbstractVillagePiece villagePieceIn, final AtomicReference<VoxelShape> shapeReference,
                final int index, final int depth)
        {
            this.villagePiece = villagePieceIn;
            this.shapeReference = shapeReference;
            this.index = index;
            this.depth = depth;
        }
    }

    public static ServerWorld getForGen(final ChunkGenerator chunkGen)
    {
        final MinecraftServer server = LogicalSidedProvider.INSTANCE.get(LogicalSide.SERVER);
        for (final ServerWorld w : server.getWorlds())
            if (w.getChunkProvider().generator == chunkGen) return w;
        throw new IllegalStateException("Did not find a world for this chunk generator!");
    }

    private JigsawManager.IPieceFactory pieceFactory;

    private int                  depth;
    private ChunkGenerator       chunkGenerator;
    private TemplateManager      templateManager;
    private List<StructurePiece> structurePieces;
    private Random               rand;

    private Predicate<JigsawPiece> validator = p -> true;

    private final Deque<JigsawAssmbler.Entry> availablePieces = Queues.newArrayDeque();

    private Heightmap.Type SURFACE_TYPE = Heightmap.Type.WORLD_SURFACE_WG;

    private final Set<String> once_added  = Sets.newHashSet();
    private final Set<String> needed_once = Sets.newHashSet();

    private final JigSawConfig config;

    public JigsawAssmbler(final JigSawConfig config)
    {
        this.config = config;
    }

    private void init(final int depth, final JigsawManager.IPieceFactory pieceFactory,
            final ChunkGenerator chunkGenerator, final TemplateManager templateManagerIn, final BlockPos pos,
            final List<StructurePiece> parts, final Random rand, final Biome biome)
    {
        this.depth = depth;
        this.pieceFactory = pieceFactory;
        this.chunkGenerator = chunkGenerator;
        this.templateManager = templateManagerIn;
        this.structurePieces = parts;
        this.rand = rand;
        this.once_added.clear();
        this.needed_once.clear();
        this.needed_once.addAll(this.config.needed_once);
    }

    private JigsawPattern init(final DynamicRegistries dynamicRegistryManager,
            final ResourceLocation resourceLocationIn)
    {
        final MutableRegistry<JigsawPattern> mutableregistry = dynamicRegistryManager.getRegistry(
                Registry.JIGSAW_POOL_KEY);
        return mutableregistry.getOrDefault(resourceLocationIn);
    }

    public boolean build(final DynamicRegistries dynamicRegistryManager, final ResourceLocation resourceLocationIn,
            final int depth, final JigsawManager.IPieceFactory pieceFactory, final ChunkGenerator chunkGenerator,
            final TemplateManager templateManagerIn, final BlockPos pos, final List<StructurePiece> parts,
            final Random rand, final Biome biome, final Predicate<JigsawPiece> isValid, final int default_k)
    {
        return this.build(this.init(dynamicRegistryManager, resourceLocationIn), Rotation.randomRotation(rand), depth,
                pieceFactory, chunkGenerator, templateManagerIn, pos, parts, rand, biome, default_k, isValid);
    }

    public boolean build(final DynamicRegistries dynamicRegistryManager, final ResourceLocation resourceLocationIn,
            final int depth, final JigsawManager.IPieceFactory pieceFactory, final ChunkGenerator chunkGenerator,
            final TemplateManager templateManagerIn, final BlockPos pos, final List<StructurePiece> parts,
            final Random rand, final Biome biome, final Predicate<JigsawPiece> isValid)
    {
        return this.build(dynamicRegistryManager, resourceLocationIn, depth, pieceFactory, chunkGenerator,
                templateManagerIn, pos, parts, rand, biome, isValid, -1);
    }

    public boolean build(final JigsawPattern jigsawpattern, final Rotation rotation, final int depth,
            final JigsawManager.IPieceFactory pieceFactory, final ChunkGenerator chunkGenerator,
            final TemplateManager templateManagerIn, final BlockPos pos, final List<StructurePiece> parts,
            final Random rand, final Biome biome, final int default_k, final Predicate<JigsawPiece> isValid)
    {
        this.validator = isValid;
        if (PokecubeCore.getConfig().debug) PokecubeCore.LOGGER.debug("Jigsaw starting build");
        this.init(depth, pieceFactory, chunkGenerator, templateManagerIn, pos, parts, rand, biome);

        if (this.config.water || this.config.air) this.SURFACE_TYPE = this.config.water ? Type.OCEAN_FLOOR_WG
                : Type.WORLD_SURFACE_WG;
        else if (!this.config.surface) this.SURFACE_TYPE = null;

        final JigsawPiece jigsawpiece = jigsawpattern.getRandomPiece(rand);
        final AbstractVillagePiece abstractvillagepiece = pieceFactory.create(templateManagerIn, jigsawpiece, pos,
                jigsawpiece.getGroundLevelDelta(), rotation, jigsawpiece.getBoundingBox(templateManagerIn, pos,
                        rotation));

        final MutableBoundingBox mutableboundingbox = abstractvillagepiece.getBoundingBox();
        final int i = (mutableboundingbox.maxX + mutableboundingbox.minX) / 2;
        final int j = (mutableboundingbox.maxZ + mutableboundingbox.minZ) / 2;
        int k = default_k;
        // If we have not been provided with a default value, determine where to
        // place the ground for the structure
        if (k == -1)
        {
            final int variance = this.config.variance <= 0 ? 0 : rand.nextInt(this.config.variance);
            // Air spawns are a somewhat random distance above the surface
            // reference.
            if (this.config.air) k = chunkGenerator.getNoiseHeight(i, j, this.SURFACE_TYPE) + variance;
            // If we have a surface reference, then lets use that
            else if (this.SURFACE_TYPE != null) k = chunkGenerator.getNoiseHeight(i, j, this.SURFACE_TYPE);
            else
            {
                // Otherwise, pick a random value below ground
                k = this.chunkGenerator.getNoiseHeight(i, j, Heightmap.Type.OCEAN_FLOOR_WG);
                if (k > 0) k = this.rand.nextInt(k + 1);
                else k = chunkGenerator.getSeaLevel();
            }
        }
        // Ensure it is placed in range
        if (k <= 0 || k >= chunkGenerator.getMaxBuildHeight())
        {
            k = chunkGenerator.getMaxBuildHeight();
            k = this.rand.nextInt(k + 1);
        }
        final int dy = -this.config.height;
        abstractvillagepiece.offset(0, k - (mutableboundingbox.minY + abstractvillagepiece.getGroundLevelDelta() + dy),
                0);
        parts.add(abstractvillagepiece);
        if (depth > 0)
        {
            final int l = 80;
            final AxisAlignedBB axisalignedbb = new AxisAlignedBB(i - l, k - l, j - l, i + l + 1, k + l + 1, j + l + 1);
            this.availablePieces.addLast(new Entry(abstractvillagepiece, new AtomicReference<>(VoxelShapes
                    .combineAndSimplify(VoxelShapes.create(axisalignedbb), VoxelShapes.create(AxisAlignedBB.toImmutable(
                            mutableboundingbox)), IBooleanFunction.ONLY_FIRST)), k + l, 0));

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
            if (!(part instanceof AbstractVillagePiece)) continue;
            final AbstractVillagePiece jig = (AbstractVillagePiece) part;
            if (!(jig.getJigsawPiece() instanceof CustomJigsawPiece)) continue;
            final CustomJigsawPiece piece = (CustomJigsawPiece) jig.getJigsawPiece();
            if (piece.opts != null) guarenteed.remove(piece.opts.flag);
        }
        return guarenteed.isEmpty();
    }

    private void sort(final List<JigsawPiece> list)
    {
        final List<JigsawPiece> needed = Lists.newArrayList();
        list.removeIf(p -> !this.validator.test(p));
        list.removeIf(p -> p instanceof CustomJigsawPiece && this.once_added.contains(
                ((CustomJigsawPiece) p).opts.flag));
        for (final JigsawPiece p : list)
            if (p instanceof CustomJigsawPiece && !((CustomJigsawPiece) p).opts.flag.isEmpty() && this.needed_once
                    .contains(((CustomJigsawPiece) p).opts.flag)) needed.add(p);
        list.removeIf(p -> needed.contains(p));
        Collections.shuffle(needed, this.rand);
        for (final JigsawPiece p : needed)
            list.add(0, p);
        final IWorld world = JigsawAssmbler.getForGen(this.chunkGenerator);
        list.forEach(p ->
        {
            if (p instanceof CustomJigsawPiece)
            {
                ((CustomJigsawPiece) p).config = this.config;
                ((CustomJigsawPiece) p).world = (World) world;
            }
        });
    }

    private void addPiece(final AbstractVillagePiece villagePieceIn, final AtomicReference<VoxelShape> atomicVoxelShape,
            final int part_index, final int current_depth, final int default_k)
    {
        final JigsawPiece part = villagePieceIn.getJigsawPiece();
        final BlockPos blockpos = villagePieceIn.getPos();
        final Rotation rotation = villagePieceIn.getRotation();
        final JigsawPattern.PlacementBehaviour placmenet = part.getPlacementBehaviour();
        final boolean root_rigid = placmenet == JigsawPattern.PlacementBehaviour.RIGID;
        final AtomicReference<VoxelShape> atomicreference = new AtomicReference<>();
        final MutableBoundingBox part_box = villagePieceIn.getBoundingBox();
        final int part_min_y = part_box.minY;

        int k0 = default_k;

        if (k0 == -1 && this.SURFACE_TYPE == null)
        {
            k0 = this.chunkGenerator.getNoiseHeight(blockpos.getX(), blockpos.getZ(), Heightmap.Type.OCEAN_FLOOR_WG);
            if (k0 > 0) k0 = this.rand.nextInt(k0 + 1);
            else k0 = -1;
        }

        label123:
        for (final Template.BlockInfo jigsaw_block : part.getJigsawBlocks(this.templateManager, blockpos, rotation,
                this.rand))
        {
            final Direction jig_dir = JigsawBlock.getConnectingDirection(jigsaw_block.state);
            final BlockPos jig_pos = jigsaw_block.pos;
            final BlockPos jig_target = jig_pos.offset(jig_dir);
            final int jig_min_y = jig_pos.getY() - part_min_y;
            int k = k0;
            final JigsawPattern pool = WorldGenRegistries.JIGSAW_POOL.getOrDefault(new ResourceLocation(jigsaw_block.nbt
                    .getString("pool")));
            if (pool == null)
            {
                PokecubeCore.LOGGER.error(jigsaw_block.nbt.getString("pool") + " " + jigsaw_block.nbt);
                continue;
            }
            final JigsawPattern pool_pattern = WorldGenRegistries.JIGSAW_POOL.getOrDefault(pool.getFallback());
            if (pool.getNumberOfPieces() != 0 || pool.getName().equals(JigsawPatternRegistry.field_244091_a
                    .getLocation()))
            {
                final boolean target_in_box = part_box.isVecInside(jig_target);
                AtomicReference<VoxelShape> atomicreference1;
                int l;
                if (target_in_box)
                {
                    atomicreference1 = atomicreference;
                    l = part_min_y;
                    if (atomicreference.get() == null) atomicreference.set(VoxelShapes.create(AxisAlignedBB.toImmutable(
                            part_box)));
                }
                else
                {
                    atomicreference1 = atomicVoxelShape;
                    l = part_index;
                }

                final List<JigsawPiece> list = Lists.newArrayList();
                if (current_depth != this.depth) list.addAll(pool.getShuffledPieces(this.rand));
                list.addAll(pool_pattern.getShuffledPieces(this.rand));
                this.sort(list);

                for (final JigsawPiece next_part : list)
                {
                    if (next_part == EmptyJigsawPiece.INSTANCE) break;
                    String once = "";

                    if (next_part instanceof CustomJigsawPiece && !(once = ((CustomJigsawPiece) next_part).opts.flag)
                            .isEmpty() && this.once_added.contains(once)) continue;

                    for (final Rotation dir : Rotation.shuffledRotations(this.rand))
                    {
                        final List<Template.BlockInfo> next_jigsaws = next_part.getJigsawBlocks(this.templateManager,
                                BlockPos.ZERO, dir, this.rand);
                        final MutableBoundingBox next_box = next_part.getBoundingBox(this.templateManager,
                                BlockPos.ZERO, dir);
                        int i1;
                        if (next_box.getYSize() > 16) i1 = 0;
                        else i1 = next_jigsaws.stream().mapToInt((blockInfo) ->
                        {
                            if (!next_box.isVecInside(blockInfo.pos.offset(blockInfo.state.get(JigsawBlock.ORIENTATION)
                                    .func_239642_b_()))) return 0;
                            else
                            {
                                final ResourceLocation pool_loc = new ResourceLocation(blockInfo.nbt.getString("pool"));
                                final JigsawPattern next_pool = WorldGenRegistries.JIGSAW_POOL.getOrDefault(pool_loc);
                                if (next_pool == null)
                                {
                                    PokecubeCore.LOGGER.error(jigsaw_block.nbt.getString("pool") + " "
                                            + jigsaw_block.nbt);
                                    return 0;
                                }
                                final JigsawPattern fallback_pool = WorldGenRegistries.JIGSAW_POOL.getOrDefault(
                                        next_pool.getFallback());
                                return Math.max(next_pool.getMaxSize(this.templateManager), fallback_pool.getMaxSize(
                                        this.templateManager));
                            }
                        }).max().orElse(0);

                        for (final Template.BlockInfo next_jigsaw : next_jigsaws)
                            if (JigsawBlock.hasJigsawMatch(jigsaw_block, next_jigsaw))
                            {
                                final BlockPos next_pos = next_jigsaw.pos;
                                final BlockPos dr_0 = new BlockPos(jig_target.getX() - next_pos.getX(), jig_target
                                        .getY() - next_pos.getY(), jig_target.getZ() - next_pos.getZ());
                                final MutableBoundingBox box_1 = next_part.getBoundingBox(this.templateManager, dr_0,
                                        dir);
                                final int next_min_y = box_1.minY;
                                final JigsawPattern.PlacementBehaviour placementRule = next_part
                                        .getPlacementBehaviour();
                                final boolean rigid = placementRule == JigsawPattern.PlacementBehaviour.RIGID;
                                final int target_y = next_pos.getY();
                                final int l1 = jig_min_y - target_y + JigsawBlock.getConnectingDirection(
                                        jigsaw_block.state).getYOffset();
                                int i2;
                                if (root_rigid && rigid) i2 = part_min_y + l1;
                                else
                                {
                                    if (k == -1) k = this.chunkGenerator.getNoiseHeight(jig_pos.getX(), jig_pos.getZ(),
                                            this.SURFACE_TYPE);
                                    i2 = k - target_y;
                                }

                                final int next_rel_y = i2 - next_min_y;
                                final MutableBoundingBox box_2 = box_1.func_215127_b(0, next_rel_y, 0);
                                final BlockPos dr_1 = dr_0.add(0, next_rel_y, 0);
                                if (i1 > 0)
                                {
                                    final int k2 = Math.max(i1 + 1, box_2.maxY - box_2.minY);
                                    box_2.maxY = box_2.minY + k2;
                                }

                                if (!VoxelShapes.compare(atomicreference1.get(), VoxelShapes.create(AxisAlignedBB
                                        .toImmutable(box_2).shrink(0.25D)), IBooleanFunction.ONLY_SECOND))
                                {
                                    atomicreference1.set(VoxelShapes.combine(atomicreference1.get(), VoxelShapes.create(
                                            AxisAlignedBB.toImmutable(box_2)), IBooleanFunction.ONLY_FIRST));
                                    final int j3 = villagePieceIn.getGroundLevelDelta();
                                    int l2;
                                    if (rigid) l2 = j3 - l1;
                                    else l2 = next_part.getGroundLevelDelta();

                                    final AbstractVillagePiece abstractvillagepiece = this.pieceFactory.create(
                                            this.templateManager, next_part, dr_1, l2, dir, box_2);
                                    int i3;
                                    if (root_rigid) i3 = part_min_y + jig_min_y;
                                    else if (rigid) i3 = i2 + target_y;
                                    else
                                    {
                                        if (k == -1) k = this.chunkGenerator.getNoiseHeight(jig_pos.getX(), jig_pos
                                                .getZ(), this.SURFACE_TYPE);
                                        i3 = k + l1 / 2;
                                    }

                                    villagePieceIn.addJunction(new JigsawJunction(jig_target.getX(), i3 - jig_min_y
                                            + j3, jig_target.getZ(), l1, placementRule));
                                    abstractvillagepiece.addJunction(new JigsawJunction(jig_pos.getX(), i3 - target_y
                                            + l2, jig_pos.getZ(), -l1, placmenet));
                                    this.structurePieces.add(abstractvillagepiece);
                                    if (!once.isEmpty())
                                    {
                                        this.once_added.add(once);
                                        if (PokecubeCore.getConfig().debug) PokecubeCore.LOGGER.debug(
                                                "added core part: {}", once);
                                    }
                                    if (current_depth + 1 <= this.depth) this.availablePieces.addLast(new Entry(
                                            abstractvillagepiece, atomicreference1, l, current_depth + 1));
                                    continue label123;
                                }
                            }
                    }
                }
            }
            else PokecubeCore.LOGGER.warn("Empty or none existent pool: {}", jigsaw_block.nbt.getString("target_pool"));
        }
    }
}
