package pokecube.legends.blocks.normalblocks;

import java.util.List;
import java.util.Random;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
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
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.lighting.LayerLightEngine;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.PlantType;
import pokecube.legends.init.BlockInit;
import pokecube.legends.init.FeaturesInit;
import pokecube.legends.init.PlantsInit;

public class AgedGrassBlock extends GrassBlock implements BonemealableBlock
{
    public AgedGrassBlock(final Properties properties)
    {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(SnowyDirtBlock.SNOWY, false));
    }

    @Override
    public void randomTick(final BlockState state, final ServerLevel world, final BlockPos pos, final Random random)
    {

        if (!AgedGrassBlock.canBeGrass(state, world, pos))
        {
            if (!world.isAreaLoaded(pos, 3))
                return;

            world.setBlockAndUpdate(pos, BlockInit.AGED_DIRT.get().defaultBlockState());
        } else if (world.getMaxLocalRawBrightness(pos.above()) >= 9)
        {
            final BlockState blockstate = this.defaultBlockState();

            for (int i = 0; i < 4; ++i)
            {
                final BlockPos blockpos = pos.offset(random.nextInt(3) - 1, random.nextInt(5) - 3, random.nextInt(3)
                        - 1);
                if (world.getBlockState(blockpos).is(BlockInit.AGED_DIRT.get()) && AgedGrassBlock.canPropagate(
                        blockstate, world, blockpos))
                    world.setBlockAndUpdate(blockpos, blockstate.setValue(
                            SnowyDirtBlock.SNOWY, world.getBlockState(blockpos.above()).is(Blocks.SNOW)));
            }
        }
    }

    public static boolean canPropagate(final BlockState state, final LevelReader world, final BlockPos pos)
    {
        final BlockPos blockpos = pos.above();
        return AgedGrassBlock.canBeGrass(state, world, pos) && !world.getFluidState(blockpos).is(FluidTags.WATER);
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
            final int i = LayerLightEngine.getLightBlockInto(world, state, pos, blockstate, blockpos, Direction.UP,
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
            return block.getBlockState(pos).getMaterial() == Material.WATER && block.getBlockState(pos) == this.defaultBlockState();
        else if (plantType == PlantType.BEACH)
            return ((block.getBlockState(pos.east()).getBlock() == Blocks.WATER || block.getBlockState(pos.east()).hasProperty(BlockStateProperties.WATERLOGGED))
                    || (block.getBlockState(pos.west()).getBlock() == Blocks.WATER || block.getBlockState(pos.west()).hasProperty(BlockStateProperties.WATERLOGGED))
                    || (block.getBlockState(pos.north()).getBlock() == Blocks.WATER || block.getBlockState(pos.north()).hasProperty(BlockStateProperties.WATERLOGGED))
                    || (block.getBlockState(pos.south()).getBlock() == Blocks.WATER || block.getBlockState(pos.south()).hasProperty(BlockStateProperties.WATERLOGGED)));
        else
            return super.canSustainPlant(state, block, pos, direction, plantable);
    }

    @Override
    public void performBonemeal(ServerLevel world, Random random, BlockPos pos, BlockState state)
    {
        BlockPos posAbove = pos.above();
        BlockState grassState = PlantsInit.GOLDEN_GRASS.get().defaultBlockState();
        BlockState fernState = PlantsInit.GOLDEN_FERN.get().defaultBlockState();

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
           if (stateAbove.is(fernState.getBlock()) && random.nextInt(10) == 0) 
           {
              ((BonemealableBlock)fernState.getBlock()).performBonemeal(world, random, posAbove1, stateAbove);
           }

           if (stateAbove.isAir())
           {
              Holder<PlacedFeature> placedFeature;
              if (random.nextInt(8) == 0)
              {
                 List<ConfiguredFeature<?, ?>> list = world.getBiome(posAbove1).value().getGenerationSettings().getFlowerFeatures();
                 if (list.isEmpty())
                 {
                    continue;
                 }

                 placedFeature = ((RandomPatchConfiguration)list.get(0).config()).feature();
              } else {
                 placedFeature = FeaturesInit.PlantPlacements.PATCH_GOLDEN_GRASS;
              }

              placedFeature.value().place(world, world.getChunkSource().getGenerator(), random, posAbove1);
           }
        }
    }
}
