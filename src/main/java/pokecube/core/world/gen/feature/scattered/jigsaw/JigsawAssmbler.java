package pokecube.core.world.gen.feature.scattered.jigsaw;

import java.util.Deque;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;

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
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.Heightmap;
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
    private TemplateManager                   templateManager;
    private List<StructurePiece>              structurePieces;
    private Random                            rand;
    private final Deque<JigsawAssmbler.Entry> availablePieces = Queues.newArrayDeque();

    public JigsawAssmbler()
    {
    }

    public boolean build(final ResourceLocation resourceLocationIn, final int depth,
            final JigsawManager.IPieceFactory pieceFactory, final ChunkGenerator<?> chunkGenerator,
            final TemplateManager templateManagerIn, final BlockPos pos, final List<StructurePiece> parts,
            final Random rand)
    {
        this.depth = depth;
        this.pieceFactory = pieceFactory;
        this.chunkGenerator = chunkGenerator;
        this.templateManager = templateManagerIn;
        this.structurePieces = parts;
        this.rand = rand;
        final Rotation rotation = Rotation.randomRotation(rand);
        final JigsawPattern jigsawpattern = JigsawManager.REGISTRY.get(resourceLocationIn);
        final JigsawPiece jigsawpiece = jigsawpattern.getRandomPiece(rand);
        final AbstractVillagePiece abstractvillagepiece = pieceFactory.create(templateManagerIn, jigsawpiece, pos,
                jigsawpiece.func_214850_d(), rotation, jigsawpiece.getBoundingBox(templateManagerIn, pos, rotation));
        final MutableBoundingBox mutableboundingbox = abstractvillagepiece.getBoundingBox();
        final int i = (mutableboundingbox.maxX + mutableboundingbox.minX) / 2;
        final int j = (mutableboundingbox.maxZ + mutableboundingbox.minZ) / 2;
        final int k = chunkGenerator.func_222532_b(i, j, Heightmap.Type.WORLD_SURFACE_WG);
        abstractvillagepiece.offset(0, k - (mutableboundingbox.minY + abstractvillagepiece.getGroundLevelDelta()), 0);
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
                        jigsawmanager$entry.index, jigsawmanager$entry.depth);
            }

        }
        if (jigsawpattern instanceof JigsawPatternCustom)
        {
            final List<JigsawPiece> guarenteed = ((JigsawPatternCustom) jigsawpattern).neededChildren;
            boolean hasAll = true;
            outer:
            for (final JigsawPiece p : guarenteed)
            {
                for (final StructurePiece part : parts)
                    if (part instanceof AbstractVillagePiece && ((AbstractVillagePiece) part).getJigsawPiece() == p)
                        continue outer;
                hasAll = false;
                break;
            }
            return hasAll;
        }
        return true;
    }

    private void addPiece(final AbstractVillagePiece villagePieceIn, final AtomicReference<VoxelShape> atomicVoxelShape,
            final int part_index, final int current_depth)
    {
        final JigsawPiece jigsawpiece = villagePieceIn.getJigsawPiece();
        final BlockPos blockpos = villagePieceIn.getPos();
        final Rotation rotation = villagePieceIn.getRotation();
        final JigsawPattern.PlacementBehaviour jigsawpattern$placementbehaviour = jigsawpiece.getPlacementBehaviour();
        final boolean flag = jigsawpattern$placementbehaviour == JigsawPattern.PlacementBehaviour.RIGID;
        final AtomicReference<VoxelShape> atomicreference = new AtomicReference<>();
        final MutableBoundingBox mutableboundingbox = villagePieceIn.getBoundingBox();
        final int i = mutableboundingbox.minY;

        label123:
        for (final Template.BlockInfo template$blockinfo : jigsawpiece.getJigsawBlocks(this.templateManager, blockpos,
                rotation, this.rand))
        {
            final Direction direction = template$blockinfo.state.get(DirectionalBlock.FACING);
            final BlockPos blockpos1 = template$blockinfo.pos;
            final BlockPos blockpos2 = blockpos1.offset(direction);
            final int j = blockpos1.getY() - i;
            int k = -1;
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

                for (final JigsawPiece jigsawpiece1 : list)
                {
                    if (jigsawpiece1 == EmptyJigsawPiece.INSTANCE) break;

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
                                final boolean flag2 = jigsawpattern$placementbehaviour1 == JigsawPattern.PlacementBehaviour.RIGID;
                                final int k1 = blockpos3.getY();
                                final int l1 = j - k1 + template$blockinfo.state.get(DirectionalBlock.FACING)
                                        .getYOffset();
                                int i2;
                                if (flag && flag2) i2 = i + l1;
                                else
                                {
                                    if (k == -1) k = this.chunkGenerator.func_222532_b(blockpos1.getX(), blockpos1
                                            .getZ(), Heightmap.Type.WORLD_SURFACE_WG);

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
                                    if (flag2) l2 = j3 - l1;
                                    else l2 = jigsawpiece1.func_214850_d();

                                    final AbstractVillagePiece abstractvillagepiece = this.pieceFactory.create(
                                            this.templateManager, jigsawpiece1, blockpos5, l2, rotation1,
                                            mutableboundingbox3);
                                    int i3;
                                    if (flag) i3 = i + j;
                                    else if (flag2) i3 = i2 + k1;
                                    else
                                    {
                                        if (k == -1) k = this.chunkGenerator.func_222532_b(blockpos1.getX(), blockpos1
                                                .getZ(), Heightmap.Type.WORLD_SURFACE_WG);

                                        i3 = k + l1 / 2;
                                    }

                                    villagePieceIn.addJunction(new JigsawJunction(blockpos2.getX(), i3 - j + j3,
                                            blockpos2.getZ(), l1, jigsawpattern$placementbehaviour1));
                                    abstractvillagepiece.addJunction(new JigsawJunction(blockpos1.getX(), i3 - k1 + l2,
                                            blockpos1.getZ(), -l1, jigsawpattern$placementbehaviour));
                                    this.structurePieces.add(abstractvillagepiece);
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
