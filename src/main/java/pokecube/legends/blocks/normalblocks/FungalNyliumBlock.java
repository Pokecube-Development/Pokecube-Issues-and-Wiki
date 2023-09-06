package pokecube.legends.blocks.normalblocks;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.GrassBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.SnowyDirtBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.lighting.LightEngine;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.PlantType;
import pokecube.legends.init.BlockInit;

public class FungalNyliumBlock extends GrassBlock implements BonemealableBlock
{
    public FungalNyliumBlock(final Properties properties)
    {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(SnowyDirtBlock.SNOWY, false));
    }

    @SuppressWarnings("deprecation")
    @Override
    public void randomTick(final BlockState state, final ServerLevel world, final BlockPos pos, final RandomSource random)
    {

        if (!FungalNyliumBlock.canBeGrass(state, world, pos))
        {
            if (!world.isAreaLoaded(pos, 3))
                return;

            world.setBlockAndUpdate(pos, BlockInit.MUSHROOM_DIRT.get().defaultBlockState());
        } else if (world.getMaxLocalRawBrightness(pos.above()) >= 9)
        {
            final BlockState blockstate = this.defaultBlockState();

            for (int i = 0; i < 4; ++i)
            {
                final BlockPos blockpos = pos.offset(random.nextInt(3) - 1, random.nextInt(5) - 3, random.nextInt(3)
                        - 1);
                if (world.getBlockState(blockpos).is(BlockInit.MUSHROOM_DIRT.get()) && FungalNyliumBlock.canPropagate(
                        blockstate, world, blockpos))
                    world.setBlockAndUpdate(blockpos, blockstate.setValue(
                            SnowyDirtBlock.SNOWY, world.getBlockState(blockpos.above()).is(Blocks.SNOW)));
            }
        }
    }

    public static boolean canPropagate(final BlockState state, final LevelReader world, final BlockPos pos)
    {
        final BlockPos blockpos = pos.above();
        return FungalNyliumBlock.canBeGrass(state, world, pos) && !world.getFluidState(blockpos).is(FluidTags.WATER);
    }

    public static boolean canBeGrass(final BlockState state, final LevelReader world, final BlockPos pos)
    {
        final BlockPos blockpos = pos.above();
        final BlockState blockstate = world.getBlockState(blockpos);
        if (blockstate.is(Blocks.SNOW) && blockstate.getValue(SnowLayerBlock.LAYERS) >= 1)
            return true;
        else if (blockstate.getFluidState().getAmount() == 8)
            return false;
        else
        {
            final int i = LightEngine.getLightBlockInto(world, state, pos, blockstate, blockpos, Direction.UP,
                    blockstate.getLightBlock(world, blockpos));
            return i < world.getMaxLightLevel();
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
            return block.getFluidState(pos).is(FluidTags.WATER) && block.getBlockState(pos) == this.defaultBlockState();
        else if (plantType == PlantType.BEACH)
            return ((block.getBlockState(pos.east()).getBlock() == Blocks.WATER || block.getBlockState(pos.east()).hasProperty(BlockStateProperties.WATERLOGGED))
                    || (block.getBlockState(pos.west()).getBlock() == Blocks.WATER || block.getBlockState(pos.west()).hasProperty(BlockStateProperties.WATERLOGGED))
                    || (block.getBlockState(pos.north()).getBlock() == Blocks.WATER || block.getBlockState(pos.north()).hasProperty(BlockStateProperties.WATERLOGGED))
                    || (block.getBlockState(pos.south()).getBlock() == Blocks.WATER || block.getBlockState(pos.south()).hasProperty(BlockStateProperties.WATERLOGGED)));
        else
            return super.canSustainPlant(state, block, pos, direction, plantable);
    }

    @Override
    public void performBonemeal(ServerLevel world, RandomSource random, BlockPos pos, BlockState state)
    {
        BlockPos posAbove = pos.above();

        for(int i = 0; i < 128; ++i)
        {
           BlockState stateAbove = world.getBlockState(posAbove);

           if (stateAbove.isAir())
           {
              if (random.nextInt(8) == 0)
              {
                 List<ConfiguredFeature<?, ?>> list = world.getBiome(posAbove).value().getGenerationSettings().getFlowerFeatures();
                 if (list.isEmpty())
                 {
                    continue;
                 }
              }
           }
        }
    }
}
