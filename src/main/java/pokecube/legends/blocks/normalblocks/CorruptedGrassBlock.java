package pokecube.legends.blocks.normalblocks;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.NyliumBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.lighting.LightEngine;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.PlantType;
import pokecube.legends.init.BlockInit;
import pokecube.legends.init.PlantsInit;
import thut.lib.RegHelper;

public class CorruptedGrassBlock extends NyliumBlock implements BonemealableBlock
{
    public static final BooleanProperty SNOWY = BlockStateProperties.SNOWY;

    public CorruptedGrassBlock(final Properties properties)
    {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(CorruptedGrassBlock.SNOWY, false));
    }

    @SuppressWarnings("deprecation")
    @Override
    public BlockState updateShape(final BlockState state, final Direction direction, final BlockState state1,
            final LevelAccessor world, final BlockPos pos, final BlockPos pos1)
    {
        return direction != Direction.UP ? super.updateShape(state, direction, state1, world, pos, pos1)
                : (BlockState) state.setValue(CorruptedGrassBlock.SNOWY, state1.is(Blocks.SNOW_BLOCK) || state1.is(
                        Blocks.SNOW));
    }

    @Override
    public BlockState getStateForPlacement(final BlockPlaceContext context)
    {
        final BlockState state = context.getLevel().getBlockState(context.getClickedPos().above());
        return this.defaultBlockState().setValue(CorruptedGrassBlock.SNOWY, state.is(Blocks.SNOW_BLOCK) || state.is(
                Blocks.SNOW));
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(CorruptedGrassBlock.SNOWY);
    }

    private static boolean canBeGrass(final BlockState state, final LevelReader world, final BlockPos pos)
    {
        final BlockPos blockpos = pos.above();
        final BlockState blockstate = world.getBlockState(blockpos);
        if (blockstate.is(Blocks.SNOW) && blockstate.getValue(SnowLayerBlock.LAYERS) >= 1)
            return true;
        else if (blockstate.getFluidState().getAmount() == 8)
            return false;
        else
        {
            final int light = LightEngine.getLightBlockInto(world, state, pos, blockstate, blockpos, Direction.UP,
                    blockstate.getLightBlock(world, blockpos));
            return light < world.getMaxLightLevel();
        }
    }

    @Override
    public void randomTick(final BlockState state, final ServerLevel world, final BlockPos pos, final RandomSource random)
    {
        if (!CorruptedGrassBlock.canBeGrass(state, world, pos))
            world.setBlockAndUpdate(pos, BlockInit.CORRUPTED_DIRT
                    .get().defaultBlockState());
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
        BlockState grassState = PlantsInit.CORRUPTED_GRASS.get().defaultBlockState();

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
                  placedFeature = world.registryAccess().registryOrThrow(RegHelper.PLACED_FEATURE_REGISTRY)
                          .getHolderOrThrow(ResourceKey.create(RegHelper.PLACED_FEATURE_REGISTRY, new ResourceLocation("pokecube_legends:corrupted_grass_bonemeal")));
              }

              placedFeature.value().place(world, world.getChunkSource().getGenerator(), random, posAbove1);
           }
        }
    }
}
