package pokecube.core.world.gen.jigsaw;

import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;

import net.minecraft.block.DirectionalBlock;
import net.minecraft.block.JigsawBlock;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.Heightmap.Type;
import net.minecraft.world.gen.feature.jigsaw.EmptyJigsawPiece;
import net.minecraft.world.gen.feature.jigsaw.JigsawJunction;
import net.minecraft.world.gen.feature.jigsaw.JigsawManager;
import net.minecraft.world.gen.feature.jigsaw.JigsawPattern;
import net.minecraft.world.gen.feature.jigsaw.JigsawPiece;
import net.minecraft.world.gen.feature.structure.AbstractVillagePiece;
import net.minecraft.world.gen.feature.structure.StructurePiece;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.gen.feature.template.TemplateManager;
import pokecube.core.PokecubeCore;
import pokecube.core.database.SpawnBiomeMatcher.SpawnCheck;
import pokecube.core.world.gen.jigsaw.JigsawPieces.SingleOffsetPiece;
import thut.api.maths.Vector3;

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

    private int                               depth;
    private JigsawManager.IPieceFactory       pieceFactory;
    private ChunkGenerator<?>                 chunkGenerator;
    private BlockPos                          base_pos;
    private TemplateManager                   templateManager;
    private List<StructurePiece>              structurePieces;
    private Random                            rand;
    private Biome                             biome;
    private final Deque<JigsawAssmbler.Entry> availablePieces = Queues.newArrayDeque();
    private final Set<String>                 once_added      = Sets.newHashSet();
    private Heightmap.Type                    SURFACE_TYPE    = Heightmap.Type.WORLD_SURFACE_WG;
    private JigsawPatternCustom               root            = null;

    public JigsawAssmbler()
    {
    }

    private void init(final int depth, final JigsawManager.IPieceFactory pieceFactory,
            final ChunkGenerator<?> chunkGenerator, final TemplateManager templateManagerIn, final BlockPos pos,
            final List<StructurePiece> parts, final Random rand, final Biome biome)
    {
        this.biome = biome;
        this.base_pos = pos;
        this.depth = depth;
        this.pieceFactory = pieceFactory;
        this.chunkGenerator = chunkGenerator;
        this.templateManager = templateManagerIn;
        this.structurePieces = parts;
        this.rand = rand;
        this.root = null;
        this.once_added.clear();
    }

    private JigsawPattern init(final ResourceLocation resourceLocationIn)
    {
        return JigsawManager.REGISTRY.get(resourceLocationIn);
    }

    public boolean build(final ResourceLocation resourceLocationIn, final int depth,
            final JigsawManager.IPieceFactory pieceFactory, final ChunkGenerator<?> chunkGenerator,
            final TemplateManager templateManagerIn, final BlockPos pos, final List<StructurePiece> parts,
            final Random rand, final Biome biome)
    {
        return this.build(this.init(resourceLocationIn), Rotation.randomRotation(rand), depth, pieceFactory,
                chunkGenerator, templateManagerIn, pos, parts, rand, biome, c ->
                { // Do nothing
                }, c ->
                { // Do nothing
                }, -1);
    }

    public boolean build(final JigsawPattern jigsawpattern, final Rotation rotation, final int depth,
            final JigsawManager.IPieceFactory pieceFactory, final ChunkGenerator<?> chunkGenerator,
            final TemplateManager templateManagerIn, final BlockPos pos, final List<StructurePiece> parts,
            final Random rand, final Biome biome, final Consumer<JigsawPatternCustom> pre,
            final Consumer<JigsawPatternCustom> post, final int default_k)
    {
        PokecubeCore.LOGGER.debug("Jigsaw starting build");
        this.init(depth, pieceFactory, chunkGenerator, templateManagerIn, pos, parts, rand, biome);
        if (jigsawpattern instanceof JigsawPatternCustom)
        {
            this.root = (JigsawPatternCustom) jigsawpattern;
            pre.accept(this.root);
        }

        if (this.root != null) if (this.root.jigsaw.water || this.root.jigsaw.air)
            this.SURFACE_TYPE = Type.OCEAN_FLOOR_WG;
        else if (!this.root.jigsaw.surface) this.SURFACE_TYPE = null;

        final JigsawPiece jigsawpiece = jigsawpattern.getRandomPiece(rand);
        final AbstractVillagePiece abstractvillagepiece = pieceFactory.create(templateManagerIn, jigsawpiece, pos,
                jigsawpiece.func_214850_d(), rotation, jigsawpiece.getBoundingBox(templateManagerIn, pos, rotation));
        final MutableBoundingBox mutableboundingbox = abstractvillagepiece.getBoundingBox();
        final int i = (mutableboundingbox.maxX + mutableboundingbox.minX) / 2;
        final int j = (mutableboundingbox.maxZ + mutableboundingbox.minZ) / 2;
        int k = default_k;

        if (k == -1 && this.root != null && this.root.jigsaw.air) k = chunkGenerator.func_222532_b(i, j,
                this.SURFACE_TYPE) + rand.nextInt(this.root.jigsaw.variance);

        if (k == -1) if (this.SURFACE_TYPE != null) k = chunkGenerator.func_222532_b(i, j, this.SURFACE_TYPE);
        else
        {
            k = this.chunkGenerator.func_222532_b(i, j, Heightmap.Type.OCEAN_FLOOR_WG);
            if (k > 0) k = this.rand.nextInt(k + 1);
            else k = chunkGenerator.world.getSeaLevel();
        }
        int dy = 0;
        if (this.root != null) dy = -this.root.jigsaw.height;
        abstractvillagepiece.offset(0, k - (mutableboundingbox.minY + abstractvillagepiece.getGroundLevelDelta() + dy),
                0);
        parts.add(abstractvillagepiece);
        if (depth > 0)
        {
            final int l = 80;
            final AxisAlignedBB axisalignedbb = new AxisAlignedBB(i - l, k - l, j - l, i + l + 1, k + l + 1, j + l + 1);
            this.availablePieces.addLast(new Entry(abstractvillagepiece, new AtomicReference<>(VoxelShapes
                    .combineAndSimplify(VoxelShapes.create(axisalignedbb), VoxelShapes.create(AxisAlignedBB
                            .func_216363_a(mutableboundingbox)), IBooleanFunction.ONLY_FIRST)), k + l, 0));

            while (!this.availablePieces.isEmpty())
            {
                final Entry jigsawmanager$entry = this.availablePieces.removeFirst();
                this.addPiece(jigsawmanager$entry.villagePiece, jigsawmanager$entry.shapeReference,
                        jigsawmanager$entry.index, jigsawmanager$entry.depth, default_k);
            }
        }

        if (this.root != null) post.accept(this.root);

        if (jigsawpattern instanceof JigsawPatternCustom)
        {
            final List<String> guarenteed = Lists.newArrayList(((JigsawPatternCustom) jigsawpattern).neededChildren);
            for (final StructurePiece part : parts)
                if (part instanceof AbstractVillagePiece && ((AbstractVillagePiece) part)
                        .getJigsawPiece() instanceof SingleOffsetPiece)
                {
                    final SingleOffsetPiece p = (SingleOffsetPiece) ((AbstractVillagePiece) part).getJigsawPiece();
                    guarenteed.remove(p.flag);
                }
            return guarenteed.isEmpty();
        }
        return true;
    }

    private void sort(final List<JigsawPiece> list)
    {
        final List<JigsawPiece> needed = Lists.newArrayList();
        final IWorld world = this.chunkGenerator.world;
        final BlockPos pos = this.base_pos;
        final SpawnCheck check = new SpawnCheck(Vector3.getNewVector().set(pos), world, this.biome);
        if (this.root != null) list.removeIf(p -> !this.root.isValidPos(p, check));
        list.removeIf(p -> p instanceof SingleOffsetPiece && this.once_added.contains(((SingleOffsetPiece) p).flag));
        if (this.root != null) for (final JigsawPiece p : list)
            if (p instanceof SingleOffsetPiece && !((SingleOffsetPiece) p).flag.isEmpty() && this.root.neededChildren
                    .contains(((SingleOffsetPiece) p).flag)) needed.add(p);
        list.removeIf(p -> needed.contains(p));
        Collections.shuffle(needed, this.rand);
        for (final JigsawPiece p : needed)
            list.add(0, p);
        if (this.root != null) list.forEach(p ->
        {
            if (p instanceof SingleOffsetPiece)
            {
                ((SingleOffsetPiece) p).offset = this.root.jigsaw.offset;
                ((SingleOffsetPiece) p).subbiome = this.root.jigsaw.biomeType;
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
            k0 = this.chunkGenerator.func_222532_b(blockpos.getX(), blockpos.getZ(), Heightmap.Type.OCEAN_FLOOR_WG);
            if (k0 > 0) k0 = this.rand.nextInt(k0 + 1);
            else k0 = -1;
        }

        label123:
        for (final Template.BlockInfo template$blockinfo : jigsawpiece.getJigsawBlocks(this.templateManager, blockpos,
                rotation, this.rand))
        {
            final Direction direction = template$blockinfo.state.get(DirectionalBlock.FACING);
            final BlockPos blockpos1 = template$blockinfo.pos;
            final BlockPos blockpos2 = blockpos1.offset(direction);
            final int j = blockpos1.getY() - i;
            int k = k0;
            final JigsawPattern jigsawpattern = JigsawManager.REGISTRY.get(new ResourceLocation(template$blockinfo.nbt
                    .getString("target_pool")));
            final JigsawPattern jigsawpattern1 = JigsawManager.REGISTRY.get(jigsawpattern.func_214948_a());
            if (jigsawpattern != JigsawPattern.INVALID && (jigsawpattern.getNumberOfPieces() != 0
                    || jigsawpattern == JigsawPattern.EMPTY))
            {
                final boolean flag1 = mutableboundingbox.isVecInside(blockpos2);
                AtomicReference<VoxelShape> atomicreference1;
                int l;
                if (flag1)
                {
                    atomicreference1 = atomicreference;
                    l = i;
                    if (atomicreference.get() == null) atomicreference.set(VoxelShapes.create(AxisAlignedBB
                            .func_216363_a(mutableboundingbox)));
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
                    if (jigsawpiece1 instanceof SingleOffsetPiece && !(once = ((SingleOffsetPiece) jigsawpiece1).flag)
                            .isEmpty() && this.once_added.contains(once)) continue;

                    for (final Rotation rotation1 : Rotation.shuffledRotations(this.rand))
                    {
                        final List<Template.BlockInfo> list1 = jigsawpiece1.getJigsawBlocks(this.templateManager,
                                BlockPos.ZERO, rotation1, this.rand);
                        final MutableBoundingBox mutableboundingbox1 = jigsawpiece1.getBoundingBox(this.templateManager,
                                BlockPos.ZERO, rotation1);
                        int i1;
                        if (mutableboundingbox1.getYSize() > 16) i1 = 0;
                        else i1 = list1.stream().mapToInt((p_214880_2_) ->
                        {
                            if (!mutableboundingbox1.isVecInside(p_214880_2_.pos.offset(p_214880_2_.state.get(
                                    DirectionalBlock.FACING)))) return 0;
                            else
                            {
                                final ResourceLocation resourcelocation = new ResourceLocation(p_214880_2_.nbt
                                        .getString("target_pool"));
                                final JigsawPattern jigsawpattern2 = JigsawManager.REGISTRY.get(resourcelocation);
                                final JigsawPattern jigsawpattern3 = JigsawManager.REGISTRY.get(jigsawpattern2
                                        .func_214948_a());
                                return Math.max(jigsawpattern2.func_214945_a(this.templateManager), jigsawpattern3
                                        .func_214945_a(this.templateManager));
                            }
                        }).max().orElse(0);

                        for (final Template.BlockInfo template$blockinfo1 : list1)
                            if (JigsawBlock.func_220171_a(template$blockinfo, template$blockinfo1))
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
                                final int l1 = j - k1 + template$blockinfo.state.get(DirectionalBlock.FACING)
                                        .getYOffset();
                                int i2;
                                if (root_rigid && rigid) i2 = i + l1;
                                else
                                {
                                    if (k == -1) k = this.chunkGenerator.func_222532_b(blockpos1.getX(), blockpos1
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
                                        .func_216363_a(mutableboundingbox3).shrink(0.25D)),
                                        IBooleanFunction.ONLY_SECOND))
                                {
                                    atomicreference1.set(VoxelShapes.combine(atomicreference1.get(), VoxelShapes.create(
                                            AxisAlignedBB.func_216363_a(mutableboundingbox3)),
                                            IBooleanFunction.ONLY_FIRST));
                                    final int j3 = villagePieceIn.getGroundLevelDelta();
                                    int l2;
                                    if (rigid) l2 = j3 - l1;
                                    else l2 = jigsawpiece1.func_214850_d();

                                    final AbstractVillagePiece abstractvillagepiece = this.pieceFactory.create(
                                            this.templateManager, jigsawpiece1, blockpos5, l2, rotation1,
                                            mutableboundingbox3);
                                    int i3;
                                    if (root_rigid) i3 = i + j;
                                    else if (rigid) i3 = i2 + k1;
                                    else
                                    {
                                        if (k == -1) k = this.chunkGenerator.func_222532_b(blockpos1.getX(), blockpos1
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
                                        PokecubeCore.LOGGER.debug("added core part");
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
