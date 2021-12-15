package pokecube.legends.blocks.normalblocks;

import java.util.Random;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
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
import net.minecraft.world.level.lighting.LayerLightEngine;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.PlantType;
import pokecube.legends.init.BlockInit;
import pokecube.legends.init.ItemInit;
import pokecube.legends.init.PlantsInit;

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
            final int light = LayerLightEngine.getLightBlockInto(world, state, pos, blockstate, blockpos, Direction.UP,
                    blockstate.getLightBlock(world, blockpos));
            return light < world.getMaxLightLevel();
        }
    }

    @Override
    public void randomTick(final BlockState state, final ServerLevel world, final BlockPos pos, final Random random)
    {
        if (!CorruptedGrassBlock.canBeGrass(state, world, pos))
            world.setBlockAndUpdate(pos, BlockInit.CORRUPTED_DIRT
                    .get().defaultBlockState());
    }

//    @Override
//    public void performBonemeal(final ServerLevel world, final Random random, final BlockPos pos, final BlockState state)
//    {
//        final BlockState blockstate = world.getBlockState(pos);
//        final BlockPos blockpos = pos.above();
//        if (blockstate.is(BlockInit.CORRUPTED_GRASS.get()))
//        {
//            ForestVegetationFeature.place(world, random, blockpos, FeaturesInit.Configs.TAINTED_BARRENS_CONFIG, 3, 1);
//        }
//    }

    @Override
    public void performBonemeal(final ServerLevel world, final Random random, final BlockPos pos, final BlockState state)
    {
        final BlockPos blockpos = pos.above();
        final BlockState blockstate = PlantsInit.CORRUPTED_GRASS.get().defaultBlockState();

        label48: for (int i = 0; i < 128; ++i)
        {
            BlockPos blockpos1 = blockpos;

            for (int j = 0; j < i / 16; ++j)
            {
                blockpos1 = blockpos1.offset(random.nextInt(3) - 1, (random.nextInt(3) - 1) * random.nextInt(3) / 2, random.nextInt(3) - 1);
                if (!world.getBlockState(blockpos1.below()).is(this) || world.getBlockState(blockpos1).isCollisionShapeFullBlock(world, blockpos1))
                    continue label48;
            }

            final BlockState blockstate2 = world.getBlockState(blockpos1);
            if (blockstate2.is(blockstate.getBlock()) && random.nextInt(10) == 0)
                ((BonemealableBlock) blockstate.getBlock()).performBonemeal(world, random, blockpos1, blockstate2);

            if (blockstate2.isAir())
            {
//             BlockState blockstate1;
//             if (random.nextInt(8) == 0) {
//                final List<ConfiguredFeature<?, ?>> list = world.getBiome(blockpos1).getGenerationSettings().getFlowerFeatures();
//                if (list.isEmpty()) continue;
//                blockstate1 = GrassCorruptedBlock.getBlockState(random, blockpos1, list.get(0));
//             }
//            else blockstate1 = blockstate;
//
//             if (blockstate1.canSurvive(world, blockpos1)) {
//                world.setBlock(blockpos1, blockstate1, 3);
//                ForestVegetationFeature.place(world, random, blockpos, FeaturesInit.Configs.TAINTED_BARRENS_CONFIG, 3, 1);
//             }
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

//    @SuppressWarnings("unchecked")
//    public static <U extends FeatureConfiguration> BlockState getBlockState(final Random random, final BlockPos pos, final ConfiguredFeature<U, ?> config)
//    {
    // FIXME grass bonemeal
//       final AbstractFlowerFeature<U> feature = (AbstractFlowerFeature<U>)config.feature;
//       return feature.getRandomFlower(random, pos, config.config());
//    }
}
