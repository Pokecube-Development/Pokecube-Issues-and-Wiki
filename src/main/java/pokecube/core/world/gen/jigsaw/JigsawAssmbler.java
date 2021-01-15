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
        final JigsawPiece jigsawpiece = villagePieceIn.getJigsawPiece();
        final BlockPos blockpos = villagePieceIn.getPos();
        final Rotation rotation = villagePieceIn.getRotation();
        final JigsawPattern.PlacementBehaviour jigsawpattern$placementbehaviour = jigsawpiece.getPlacementBehaviour();
        final boolean root_rigid = jigsawpattern$placementbehaviour == JigsawPattern.PlacementBehaviour.RIGID;
        final AtomicReference<VoxelShape> atomicreference = new AtomicReference<>();
        final MutableBoundingBox mutableboundingbox = villagePieceIn.getBoundingBox();
        final int i = mutableboundingbox.minY;

        int k0 = default_k;

        if (k0 == -1) if (this.SURFACE_TYPE == null)
        {
            k0 = this.chunkGenerator.getNoiseHeight(blockpos.getX(), blockpos.getZ(), Heightmap.Type.OCEAN_FLOOR_WG);
            if (k0 > 0) k0 = this.rand.nextInt(k0 + 1);
            else k0 = -1;
        }

        label123:
        for (final Template.BlockInfo template$blockinfo : jigsawpiece.getJigsawBlocks(this.templateManager, blockpos,
                rotation, this.rand))
        {
            final Direction direction = JigsawBlock.getConnectingDirection(template$blockinfo.state);
            final BlockPos blockpos1 = template$blockinfo.pos;
            final BlockPos blockpos2 = blockpos1.offset(direction);
            final int j = blockpos1.getY() - i;
            int k = k0;
            final JigsawPattern jigsawpattern = WorldGenRegistries.JIGSAW_POOL.getOrDefault(new ResourceLocation(
                    template$blockinfo.nbt.getString("pool")));
            if (jigsawpattern == null)
            {
                PokecubeCore.LOGGER.error(template$blockinfo.nbt.getString("pool") + " " + template$blockinfo.nbt);
                continue;
            }

            final JigsawPattern jigsawpattern1 = WorldGenRegistries.JIGSAW_POOL.getOrDefault(jigsawpattern
                    .getFallback());
            if (jigsawpattern.getNumberOfPieces() != 0 || jigsawpattern.getName().equals(
                    JigsawPatternRegistry.field_244091_a.getLocation()))
            {
                final boolean flag1 = mutableboundingbox.isVecInside(blockpos2);
                AtomicReference<VoxelShape> atomicreference1;
                int l;
                if (flag1)
                {
                    atomicreference1 = atomicreference;
                    l = i;
                    if (atomicreference.get() == null) atomicreference.set(VoxelShapes.create(AxisAlignedBB.toImmutable(
                            mutableboundingbox)));
                }
                else
                {
                    atomicreference1 = atomicVoxelShape;
                    l = part_index;
                }

                final List<JigsawPiece> list = Lists.newArrayList();
                if (current_depth != this.depth) list.addAll(jigsawpattern.getShuffledPieces(this.rand));

                list.addAll(jigsawpattern1.getShuffledPieces(this.rand));

                this.sort(list);

                for (final JigsawPiece jigsawpiece1 : list)
                {
                    if (jigsawpiece1 == EmptyJigsawPiece.INSTANCE) break;
                    String once = "";

                    if (jigsawpiece1 instanceof CustomJigsawPiece
                            && !(once = ((CustomJigsawPiece) jigsawpiece1).opts.flag).isEmpty() && this.once_added
                                    .contains(once)) continue;

                    for (final Rotation rotation1 : Rotation.shuffledRotations(this.rand))
                    {
                        final List<Template.BlockInfo> list1 = jigsawpiece1.getJigsawBlocks(this.templateManager,
                                BlockPos.ZERO, rotation1, this.rand);
                        final MutableBoundingBox mutableboundingbox1 = jigsawpiece1.getBoundingBox(this.templateManager,
                                BlockPos.ZERO, rotation1);
                        int i1;
                        if (mutableboundingbox1.getYSize() > 16) i1 = 0;
                        else i1 = list1.stream().mapToInt((blockInfo) ->
                        {
                            if (!mutableboundingbox1.isVecInside(blockInfo.pos.offset(blockInfo.state.get(
                                    JigsawBlock.ORIENTATION).func_239642_b_()))) return 0;
                            else
                            {
                                final ResourceLocation resourcelocation = new ResourceLocation(blockInfo.nbt.getString(
                                        "pool"));
                                final JigsawPattern jigsawpattern2 = WorldGenRegistries.JIGSAW_POOL.getOrDefault(
                                        resourcelocation);
                                if (jigsawpattern2 == null)
                                {
                                    PokecubeCore.LOGGER.error(template$blockinfo.nbt.getString("pool") + " "
                                            + template$blockinfo.nbt);
                                    return 0;
                                }
                                final JigsawPattern jigsawpattern3 = WorldGenRegistries.JIGSAW_POOL.getOrDefault(
                                        jigsawpattern2.getFallback());
                                return Math.max(jigsawpattern2.getMaxSize(this.templateManager), jigsawpattern3
                                        .getMaxSize(this.templateManager));
                            }
                        }).max().orElse(0);

                        for (final Template.BlockInfo template$blockinfo1 : list1)
                            if (JigsawBlock.hasJigsawMatch(template$blockinfo, template$blockinfo1))
                            {
                                final BlockPos blockpos3 = template$blockinfo1.pos;
                                final BlockPos blockpos4 = new BlockPos(blockpos2.getX() - blockpos3.getX(), blockpos2
                                        .getY() - blockpos3.getY(), blockpos2.getZ() - blockpos3.getZ());
                                final MutableBoundingBox mutableboundingbox2 = jigsawpiece1.getBoundingBox(
                                        this.templateManager, blockpos4, rotation1);
                                final int j1 = mutableboundingbox2.minY;
                                final JigsawPattern.PlacementBehaviour jigsawpattern$placementbehaviour1 = jigsawpiece1
                                        .getPlacementBehaviour();
                                final boolean rigid = jigsawpattern$placementbehaviour1 == JigsawPattern.PlacementBehaviour.RIGID;
                                final int k1 = blockpos3.getY();
                                final int l1 = j - k1 + JigsawBlock.getConnectingDirection(template$blockinfo.state)
                                        .getYOffset();
                                int i2;
                                if (root_rigid && rigid) i2 = i + l1;
                                else
                                {
                                    if (k == -1) k = this.chunkGenerator.getNoiseHeight(blockpos1.getX(), blockpos1
                                            .getZ(), this.SURFACE_TYPE);

                                    i2 = k - k1;
                                }

                                final int j2 = i2 - j1;
                                final MutableBoundingBox mutableboundingbox3 = mutableboundingbox2.func_215127_b(0, j2,
                                        0);
                                final BlockPos blockpos5 = blockpos4.add(0, j2, 0);
                                if (i1 > 0)
                                {
                                    final int k2 = Math.max(i1 + 1, mutableboundingbox3.maxY
                                            - mutableboundingbox3.minY);
                                    mutableboundingbox3.maxY = mutableboundingbox3.minY + k2;
                                }

                                if (!VoxelShapes.compare(atomicreference1.get(), VoxelShapes.create(AxisAlignedBB
                                        .toImmutable(mutableboundingbox3).shrink(0.25D)), IBooleanFunction.ONLY_SECOND))
                                {
                                    atomicreference1.set(VoxelShapes.combine(atomicreference1.get(), VoxelShapes.create(
                                            AxisAlignedBB.toImmutable(mutableboundingbox3)),
                                            IBooleanFunction.ONLY_FIRST));
                                    final int j3 = villagePieceIn.getGroundLevelDelta();
                                    int l2;
                                    if (rigid) l2 = j3 - l1;
                                    else l2 = jigsawpiece1.getGroundLevelDelta();

                                    final AbstractVillagePiece abstractvillagepiece = this.pieceFactory.create(
                                            this.templateManager, jigsawpiece1, blockpos5, l2, rotation1,
                                            mutableboundingbox3);
                                    int i3;
                                    if (root_rigid) i3 = i + j;
                                    else if (rigid) i3 = i2 + k1;
                                    else
                                    {
                                        if (k == -1) k = this.chunkGenerator.getNoiseHeight(blockpos1.getX(), blockpos1
                                                .getZ(), this.SURFACE_TYPE);

                                        i3 = k + l1 / 2;
                                    }

                                    villagePieceIn.addJunction(new JigsawJunction(blockpos2.getX(), i3 - j + j3,
                                            blockpos2.getZ(), l1, jigsawpattern$placementbehaviour1));
                                    abstractvillagepiece.addJunction(new JigsawJunction(blockpos1.getX(), i3 - k1 + l2,
                                            blockpos1.getZ(), -l1, jigsawpattern$placementbehaviour));
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
            else PokecubeCore.LOGGER.warn("Empty or none existent pool: {}", template$blockinfo.nbt.getString(
                    "target_pool"));
        }

    }
}
