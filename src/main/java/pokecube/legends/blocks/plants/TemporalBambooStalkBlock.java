package pokecube.legends.blocks.plants;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BambooStalkBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BambooLeaves;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import pokecube.legends.Reference;
import pokecube.legends.init.PlantsInit;
import thut.lib.RegHelper;

public class TemporalBambooStalkBlock extends BambooStalkBlock implements BonemealableBlock
{
    protected static final VoxelShape SMALL_SHAPE = Block.box(7, 0, 7, 9, 16, 9);
    protected static final VoxelShape LARGE_SHAPE = Block.box(6.5, 0, 6.5, 9.5, 16, 9.5);
    // Tags
    public static TagKey<Block> TEMPORAL_BAMBOO_PLANTABLE_ON = TagKey.create(RegHelper.BLOCK_REGISTRY,
            new ResourceLocation(Reference.ID, "temporal_bamboo_plantable_on"));

    public TemporalBambooStalkBlock(final BlockBehaviour.Properties properties)
    {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(BambooStalkBlock.AGE, Integer.valueOf(0))
                .setValue(BambooStalkBlock.LEAVES, BambooLeaves.NONE).setValue(BambooStalkBlock.STAGE, Integer.valueOf(0)));
    }

    @Override
    public VoxelShape getShape(final BlockState state, final BlockGetter block, final BlockPos pos,
            final CollisionContext context)
    {
        final VoxelShape voxelshape = state.getValue(BambooStalkBlock.AGE) == 1 ? TemporalBambooStalkBlock.LARGE_SHAPE
                : TemporalBambooStalkBlock.SMALL_SHAPE;
        final Vec3 vec3 = state.getOffset(block, pos);
        return voxelshape.move(vec3.x, vec3.y, vec3.z);
    }

    @Override
    public VoxelShape getCollisionShape(final BlockState state, final BlockGetter block, final BlockPos pos,
            final CollisionContext context)
    {
        final VoxelShape voxelshape = state.getValue(BambooStalkBlock.AGE) == 1 ? TemporalBambooStalkBlock.LARGE_SHAPE
                : TemporalBambooStalkBlock.SMALL_SHAPE;
        final Vec3 vec3 = state.getOffset(block, pos);
        return voxelshape.move(vec3.x, vec3.y, vec3.z);
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(final BlockPlaceContext context)
    {
        final FluidState fluid = context.getLevel().getFluidState(context.getClickedPos());
        if (!fluid.isEmpty()) return null;
        else
        {
            final BlockState state = context.getLevel().getBlockState(context.getClickedPos().below());
            if (state.is(TemporalBambooStalkBlock.TEMPORAL_BAMBOO_PLANTABLE_ON) & !state.is(Blocks.BAMBOO_SAPLING))
            {
                if (state.is(PlantsInit.TEMPORAL_BAMBOO_SHOOT.get()))
                    return this.defaultBlockState().setValue(BambooStalkBlock.AGE, Integer.valueOf(0));
                else if (state.is(PlantsInit.TEMPORAL_BAMBOO.get()))
                {
                    final int i = state.getValue(BambooStalkBlock.AGE) > 0 ? 1 : 0;
                    return this.defaultBlockState().setValue(BambooStalkBlock.AGE, Integer.valueOf(i));
                }
                else
                {
                    final BlockState state1 = context.getLevel().getBlockState(context.getClickedPos().above());
                    return state1.is(PlantsInit.TEMPORAL_BAMBOO.get())
                            ? this.defaultBlockState().setValue(BambooStalkBlock.AGE, state1.getValue(BambooStalkBlock.AGE))
                            : PlantsInit.TEMPORAL_BAMBOO_SHOOT.get().defaultBlockState();
                }
            }
            else return null;
        }
    }

    @Override
    public boolean canSurvive(final BlockState state, final LevelReader world, final BlockPos pos)
    {
        return world.getBlockState(pos.below()).is(TemporalBambooStalkBlock.TEMPORAL_BAMBOO_PLANTABLE_ON);
    }

    @Override
    public BlockState updateShape(final BlockState state, final Direction direction, final BlockState state1,
            final LevelAccessor world, final BlockPos pos, final BlockPos pos1)
    {
        if (!state.canSurvive(world, pos)) world.scheduleTick(pos, this, 1);

        if (direction == Direction.UP && state1.is(PlantsInit.TEMPORAL_BAMBOO.get())
                && state1.getValue(BambooStalkBlock.AGE) > state.getValue(BambooStalkBlock.AGE))
            world.setBlock(pos, state.cycle(BambooStalkBlock.AGE), 2);

        return super.updateShape(state, direction, state1, world, pos, pos1);
    }

    @Override
    public void growBamboo(final BlockState state, final Level world, final BlockPos pos, final RandomSource random,
            final int x)
    {
        final BlockState state1 = world.getBlockState(pos.below());
        final BlockPos pos1 = pos.below(2);
        final BlockState state2 = world.getBlockState(pos1);
        BambooLeaves leaves = BambooLeaves.NONE;
        if (x >= 1)
            if (state1.is(PlantsInit.TEMPORAL_BAMBOO.get()) && state1.getValue(BambooStalkBlock.LEAVES) != BambooLeaves.NONE)
        {
            if (state1.is(PlantsInit.TEMPORAL_BAMBOO.get()) && state1.getValue(BambooStalkBlock.LEAVES) != BambooLeaves.NONE)
            {
                leaves = BambooLeaves.LARGE;
                if (state2.is(PlantsInit.TEMPORAL_BAMBOO.get()))
                {
                    world.setBlock(pos.below(), state1.setValue(BambooStalkBlock.LEAVES, BambooLeaves.SMALL), 3);
                    world.setBlock(pos1, state2.setValue(BambooStalkBlock.LEAVES, BambooLeaves.NONE), 3);
                }
            }
        }
            else leaves = BambooLeaves.SMALL;

        final int i = state.getValue(BambooStalkBlock.AGE) != 1 && !state2.is(PlantsInit.TEMPORAL_BAMBOO.get()) ? 0 : 1;
        final int j = (x < 11 || !(random.nextFloat() < 0.25F)) && x != 15 ? 0 : 1;
        world.setBlock(pos.above(), this.defaultBlockState().setValue(BambooStalkBlock.AGE, Integer.valueOf(i))
                .setValue(BambooStalkBlock.LEAVES, leaves).setValue(BambooStalkBlock.STAGE, Integer.valueOf(j)), 3);
    }

    @Override
    public int getHeightAboveUpToMax(final BlockGetter block, final BlockPos pos)
    {
        int i;
        for (i = 0; i < 16 && block.getBlockState(pos.above(i + 1)).is(PlantsInit.TEMPORAL_BAMBOO.get()); ++i)
        {}

        return i;
    }

    @Override
    public int getHeightBelowUpToMax(final BlockGetter block, final BlockPos pos)
    {
        int i;
        for (i = 0; i < 16 && block.getBlockState(pos.below(i + 1)).is(PlantsInit.TEMPORAL_BAMBOO.get()); ++i)
        {}
        return i;
    }
}
