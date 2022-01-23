package pokecube.legends.blocks.normalblocks;

import java.util.List;
import java.util.Random;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.lighting.LayerLightEngine;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.PlantType;
import pokecube.legends.init.BlockInit;
import pokecube.legends.init.FeaturesInit;
import pokecube.legends.init.PlantsInit;

public class DistorticGrassBlock extends DirectionalBlock implements BonemealableBlock
{
    public static final BooleanProperty SNOWY = BlockStateProperties.SNOWY;

    public DistorticGrassBlock(final BlockBehaviour.Properties properties)
    {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(DirectionalBlock.FACING, Direction.UP).setValue(
                DistorticGrassBlock.SNOWY, false));
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(DirectionalBlock.FACING, DistorticGrassBlock.SNOWY);
    }

    @SuppressWarnings("deprecation")
    @Override
    public BlockState updateShape(final BlockState state, final Direction direction, final BlockState state1,
            final LevelAccessor world, final BlockPos pos, final BlockPos pos1)
    {
        return direction != Direction.UP ? super.updateShape(state, direction, state1, world, pos, pos1)
                : (BlockState) state.setValue(DistorticGrassBlock.SNOWY, state1.is(Blocks.SNOW_BLOCK) || state1.is(
                        Blocks.SNOW));
    }

    @Override
    public BlockState rotate(final BlockState state, final Rotation rot)
    {
        return state.setValue(DirectionalBlock.FACING, rot.rotate(state.getValue(DirectionalBlock.FACING)));
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState mirror(final BlockState state, final Mirror mirrorIn)
    {
        return state.rotate(mirrorIn.getRotation(state.getValue(DirectionalBlock.FACING)));
    }

    @Override
    public BlockState getStateForPlacement(final BlockPlaceContext context)
    {
        final BlockState state = context.getLevel().getBlockState(context.getClickedPos().above());
        return this.defaultBlockState().setValue(DirectionalBlock.FACING, context.getNearestLookingDirection()
                .getOpposite()).setValue(DistorticGrassBlock.SNOWY, state.is(Blocks.SNOW_BLOCK)
                        || state.is(
                                Blocks.SNOW));
    }

    @Override
    public boolean isValidBonemealTarget(final BlockGetter block, final BlockPos pos, final BlockState state,
            final boolean valid)
    {
        return block.getBlockState(pos.above()).isAir() && state.getValue(DirectionalBlock.FACING) == Direction.UP;
    }

    @Override
    public boolean isBonemealSuccess(final Level world, final Random random, final BlockPos pos, final BlockState state)
    {
        return true;
    }

    private static boolean canBeGrass(final BlockState state, final LevelReader world, final BlockPos pos)
    {
        final BlockPos blockpos = pos.above();
        final BlockPos blockpos1 = pos.below();
        final BlockPos blockpos2 = pos.north();
        final BlockPos blockpos3 = pos.south();
        final BlockPos blockpos4 = pos.east();
        final BlockPos blockpos5 = pos.west();
        final BlockState blockstate = world.getBlockState(blockpos);
        final BlockState blockstate1 = world.getBlockState(blockpos1);
        final BlockState blockstate2 = world.getBlockState(blockpos2);
        final BlockState blockstate3 = world.getBlockState(blockpos3);
        final BlockState blockstate4 = world.getBlockState(blockpos4);
        final BlockState blockstate5 = world.getBlockState(blockpos5);
        if (blockstate.is(Blocks.SNOW) && blockstate.getValue(SnowLayerBlock.LAYERS) >= 1)
            return true;
        else if (blockstate.getFluidState().getAmount() == 8)
            return false;
        else
        {
            final int up = LayerLightEngine.getLightBlockInto(world, state, pos, blockstate, blockpos, Direction.UP,
                    blockstate.getLightBlock(world, blockpos));
            final int down = LayerLightEngine.getLightBlockInto(world, state, pos, blockstate1, blockpos1,
                    Direction.DOWN, blockstate1.getLightBlock(world, blockpos1));
            final int north = LayerLightEngine.getLightBlockInto(world, state, pos, blockstate2, blockpos2,
                    Direction.NORTH, blockstate2.getLightBlock(world, blockpos2));
            final int south = LayerLightEngine.getLightBlockInto(world, state, pos, blockstate3, blockpos3,
                    Direction.SOUTH, blockstate3.getLightBlock(world, blockpos3));
            final int east = LayerLightEngine.getLightBlockInto(world, state, pos, blockstate4, blockpos4,
                    Direction.EAST, blockstate4.getLightBlock(world, blockpos4));
            final int west = LayerLightEngine.getLightBlockInto(world, state, pos, blockstate5, blockpos5,
                    Direction.WEST, blockstate5.getLightBlock(world, blockpos5));
            if (state.getValue(DirectionalBlock.FACING) == Direction.UP)
                return up < world.getMaxLightLevel();
            else if (state.getValue(DirectionalBlock.FACING) == Direction.DOWN)
                return down < world.getMaxLightLevel();
            else if (state.getValue(DirectionalBlock.FACING) == Direction.NORTH)
                return north < world
                        .getMaxLightLevel();
            else if (state.getValue(DirectionalBlock.FACING) == Direction.SOUTH)
                return south < world
                        .getMaxLightLevel();
            else if (state.getValue(DirectionalBlock.FACING) == Direction.EAST)
                return east < world.getMaxLightLevel();
            else
                return west < world.getMaxLightLevel();
        }
    }

    @Override
    public void randomTick(final BlockState state, final ServerLevel world, final BlockPos pos, final Random random)
    {
        if (!DistorticGrassBlock.canBeGrass(state, world, pos))
            world.setBlockAndUpdate(pos, BlockInit.DISTORTIC_STONE
                    .get().defaultBlockState());
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public void performBonemeal(final ServerLevel world, final Random random, final BlockPos pos,
            final BlockState state)
    {
        {
            BlockPos posAbove = pos.above();
            BlockState grassState = PlantsInit.DISTORTIC_GRASS.get().defaultBlockState();

            label46:
            for(int i = 0; i < 128; ++i)
            {
               BlockPos posAbove1 = posAbove;

               for(int j = 0; j < i / 16; ++j)
               {
                  posAbove1 = posAbove1.offset(random.nextInt(3) - 1, (random.nextInt(3) - 1) * random.nextInt(3) / 2, random.nextInt(3) - 1);
                  if (!world.getBlockState(posAbove1.below()).is(this) || world.getBlockState(posAbove1).isCollisionShapeFullBlock(world, posAbove1))
                  {
                     continue label46;
                  }
               }

               BlockState stateAbove = world.getBlockState(posAbove1);
               if (stateAbove.is(grassState.getBlock()) && random.nextInt(10) == 0) 
               {
                  ((BonemealableBlock)grassState.getBlock()).performBonemeal(world, random, posAbove1, stateAbove);
               }

               if (stateAbove.isAir())
               {
                  PlacedFeature placedFeature;
                  if (random.nextInt(8) == 0)
                  {
                     List<ConfiguredFeature<?, ?>> list = world.getBiome(posAbove1).getGenerationSettings().getFlowerFeatures();
                     if (list.isEmpty())
                     {
                        continue;
                     }

                     placedFeature = ((RandomPatchConfiguration)list.get(0).config()).feature().get();
                  } else {
                     placedFeature = FeaturesInit.Configs.DISTORTIC_GRASS_BONEMEAL;
                  }

                  placedFeature.place(world, world.getChunkSource().getGenerator(), random, posAbove1);
               }
            }
        }
    }

    @Override
    public boolean canSustainPlant(final BlockState state, final BlockGetter block, final BlockPos pos, final Direction direction, final IPlantable plantable)
    {
        final BlockPos plantPos = new BlockPos(pos.getX(), pos.getY() + 1, pos.getZ());
        final PlantType plantType = plantable.getPlantType(block, plantPos);

        if (plantType == PlantType.PLAINS)
            return true;
        else if (plantType == PlantType.WATER)
            return block.getBlockState(pos).getMaterial() == Material.WATER && block.getBlockState(pos) == this.defaultBlockState();
        else if (plantType == PlantType.BEACH)
            return ((block.getBlockState(pos.east()).getBlock() == Blocks.WATER || block.getBlockState(pos.east()).hasProperty(BlockStateProperties.WATERLOGGED))
                    || (block.getBlockState(pos.west()).getBlock() == Blocks.WATER || block.getBlockState(pos.west()).hasProperty(BlockStateProperties.WATERLOGGED))
                    || (block.getBlockState(pos.north()).getBlock() == Blocks.WATER || block.getBlockState(pos.north()).hasProperty(BlockStateProperties.WATERLOGGED))
                    || (block.getBlockState(pos.south()).getBlock() == Blocks.WATER || block.getBlockState(pos.south()).hasProperty(BlockStateProperties.WATERLOGGED)));
        else
            return super.canSustainPlant(state, block, pos, direction, plantable);
    }
}
