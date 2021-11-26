package pokecube.legends.blocks.normalblocks;

import java.util.List;
import java.util.Random;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.GrassBlock;
import net.minecraft.world.level.block.MushroomBlock;
import net.minecraft.world.level.block.SeagrassBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.SnowyDirtBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.feature.AbstractFlowerFeature;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.lighting.LayerLightEngine;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.PlantType;
import pokecube.legends.init.BlockInit;
import pokecube.legends.init.FeaturesInit;
import pokecube.legends.init.ItemInit;
import pokecube.legends.init.PlantsInit;
import pokecube.legends.worldgen.features.ForestVegetationFeature;

public class GrassAgedBlock extends GrassBlock implements BonemealableBlock
{
    public GrassAgedBlock(final Properties properties)
    {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(SnowyDirtBlock.SNOWY, false));
    }

    @Override
    public void randomTick(final BlockState state, final ServerLevel world, final BlockPos pos, final Random random)
    {

        if (!GrassAgedBlock.canBeGrass(state, world, pos))
        {
            if (!world.isAreaLoaded(pos, 3)) return;

            world.setBlockAndUpdate(pos, BlockInit.AGED_DIRT.get().defaultBlockState());
        }
        else if (world.getMaxLocalRawBrightness(pos.above()) >= 9)
        {
            final BlockState blockstate = this.defaultBlockState();

            for (int i = 0; i < 4; ++i)
            {
                final BlockPos blockpos = pos.offset(random.nextInt(3) - 1, random.nextInt(5) - 3, random.nextInt(3)
                        - 1);
                if (world.getBlockState(blockpos).is(BlockInit.AGED_DIRT.get()) && GrassAgedBlock.canPropagate(
                        blockstate, world, blockpos)) world.setBlockAndUpdate(blockpos, blockstate.setValue(
                                SnowyDirtBlock.SNOWY, world.getBlockState(blockpos.above()).is(Blocks.SNOW)));
            }
        }
    }

    public static boolean canPropagate(final BlockState state, final LevelReader world, final BlockPos pos)
    {
        final BlockPos blockpos = pos.above();
        return GrassAgedBlock.canBeGrass(state, world, pos) && !world.getFluidState(blockpos).is(FluidTags.WATER);
    }

    public static boolean canBeGrass(final BlockState state, final LevelReader world, final BlockPos pos)
    {
        final BlockPos blockpos = pos.above();
        final BlockState blockstate = world.getBlockState(blockpos);
        if (blockstate.is(Blocks.SNOW) && blockstate.getValue(SnowLayerBlock.LAYERS) >= 1) return true;
        else if (blockstate.getFluidState().getAmount() == 8) return false;
        else
        {
            final int i = LayerLightEngine.getLightBlockInto(world, state, pos, blockstate, blockpos, Direction.UP,
                    blockstate.getLightBlock(world, blockpos));
            return i < world.getMaxLightLevel();
        }
    }

    @Override
    public boolean canSustainPlant(BlockState state, BlockGetter block, BlockPos pos, Direction direction, IPlantable plantable)
    {
        final BlockPos plantPos = new BlockPos(pos.getX(), pos.getY() + 1, pos.getZ());
        final PlantType plantType = plantable.getPlantType(block, plantPos);

        if (plantType == PlantType.PLAINS)
        {
            return true;
        } else if (plantType == PlantType.WATER)
        {
            return block.getBlockState(pos).getMaterial() == Material.WATER && block.getBlockState(pos) == defaultBlockState();
        } else if (plantType == PlantType.BEACH)
        {
            return ((block.getBlockState(pos.east()).getBlock() == Blocks.WATER || block.getBlockState(pos.east()).hasProperty(BlockStateProperties.WATERLOGGED))
                    || (block.getBlockState(pos.west()).getBlock() == Blocks.WATER || block.getBlockState(pos.west()).hasProperty(BlockStateProperties.WATERLOGGED))
                    || (block.getBlockState(pos.north()).getBlock() == Blocks.WATER || block.getBlockState(pos.north()).hasProperty(BlockStateProperties.WATERLOGGED))
                    || (block.getBlockState(pos.south()).getBlock() == Blocks.WATER || block.getBlockState(pos.south()).hasProperty(BlockStateProperties.WATERLOGGED)));
        } else
        {
            return super.canSustainPlant(state, block, pos, direction, plantable);
        }
    }

    @Override
    public void stepOn(final Level world, final BlockPos pos, final BlockState state, final Entity entity)
    {
        super.stepOn(world, pos, state, entity);
        GrassAgedBlock.executeProcedure(entity);
    }

    public static void executeProcedure(final Entity entity)
    {
        if (entity instanceof ServerPlayer) if (((Player) entity).getInventory().armor.get(3)
                .getItem() != new ItemStack(ItemInit.ULTRA_HELMET.get(), 1).getItem() || ((Player) entity)
                        .getInventory().armor.get(2).getItem() != new ItemStack(ItemInit.ULTRA_CHESTPLATE.get(), 1)
                                .getItem() || ((Player) entity).getInventory().armor.get(1).getItem() != new ItemStack(
                                        ItemInit.ULTRA_LEGGINGS.get(), 1).getItem() || ((Player) entity)
                                                .getInventory().armor.get(0).getItem() != new ItemStack(
                                                        ItemInit.ULTRA_BOOTS.get(), 1).getItem())
            ((LivingEntity) entity).addEffect(new MobEffectInstance(MobEffects.WITHER, 120, 1));
    }

    @Override
    public void performBonemeal(ServerLevel world, Random random, BlockPos pos, BlockState state) {
       BlockPos pos1 = pos.above();
       BlockState block = PlantsInit.GOLDEN_GRASS.get().defaultBlockState();

       bonemealing:
       for(int i = 0; i < 128; ++i)
       {
          BlockPos pos2 = pos1;
          for(int j = 0; j < i / 16; ++j)
          {
             pos2 = pos2.offset(random.nextInt(3) - 1, (random.nextInt(3) - 1) * random.nextInt(3) / 2, random.nextInt(3) - 1);
             if (!world.getBlockState(pos2.below()).is(this) || world.getBlockState(pos2).isCollisionShapeFullBlock(world, pos2))
             {
                continue bonemealing;
             }
          }

          BlockState state1 = world.getBlockState(pos2);
          if (state1.is(block.getBlock()) && random.nextInt(10) == 0)
          {
             ((BonemealableBlock)block.getBlock()).performBonemeal(world, random, pos2, state1);
          }

          if (state1.isAir())
          {
             BlockState state2;
             if (random.nextInt(8) == 0)
             {
                List<ConfiguredFeature<?, ?>> list = world.getBiome(pos2).getGenerationSettings().getFlowerFeatures();
                if (list.isEmpty())
                {
                   continue;
                }
                state2 = getBlockState(random, pos2, list.get(0));
             } else
             {
                state2 = block;
             }

             if (state2.canSurvive(world, pos2))
             {
                 world.setBlock(pos2, state2, 3);
                 ForestVegetationFeature.place(world, random, pos1, FeaturesInit.Configs.FORSAKEN_TAIGA_CONFIG, 3, 1);
             }
          }
       }
    }

    @SuppressWarnings("unchecked")
    public static <U extends FeatureConfiguration> BlockState getBlockState(Random random, BlockPos pos, ConfiguredFeature<U, ?> config)
    {
       AbstractFlowerFeature<U> feature = (AbstractFlowerFeature<U>)config.feature;
       return feature.getRandomFlower(random, pos, config.config());
    }
}
