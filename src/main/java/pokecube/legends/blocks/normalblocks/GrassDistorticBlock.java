package pokecube.legends.blocks.normalblocks;

import java.util.List;
import java.util.Random;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.data.worldgen.Features;
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
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.levelgen.feature.AbstractFlowerFeature;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.NetherForestVegetationFeature;
import net.minecraft.world.level.levelgen.feature.TwistingVinesFeature;
import net.minecraft.world.level.lighting.LayerLightEngine;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.PlantType;
import pokecube.legends.init.BlockInit;
import pokecube.legends.init.ItemInit;

public class GrassDistorticBlock extends DirectionalBlock implements BonemealableBlock
{
    public static final BooleanProperty SNOWY = BlockStateProperties.SNOWY;

    public GrassDistorticBlock(final BlockBehaviour.Properties properties)
    {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(DirectionalBlock.FACING, Direction.UP).setValue(
                GrassDistorticBlock.SNOWY, false));
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(DirectionalBlock.FACING, GrassDistorticBlock.SNOWY);
    }

    @SuppressWarnings("deprecation")
    @Override
    public BlockState updateShape(final BlockState state, final Direction direction, final BlockState state1,
            final LevelAccessor world, final BlockPos pos, final BlockPos pos1)
    {
        return direction != Direction.UP ? super.updateShape(state, direction, state1, world, pos, pos1)
                : (BlockState) state.setValue(GrassDistorticBlock.SNOWY, state1.is(Blocks.SNOW_BLOCK) || state1.is(
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
                .getOpposite()).setValue(GrassDistorticBlock.SNOWY, state.is(Blocks.SNOW_BLOCK) || state.is(
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
        if (blockstate.is(Blocks.SNOW) && blockstate.getValue(SnowLayerBlock.LAYERS) >= 1) return true;
        else if (blockstate.getFluidState().getAmount() == 8) return false;
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
            if (state.getValue(DirectionalBlock.FACING) == Direction.UP) return up < world.getMaxLightLevel();
            else if (state.getValue(DirectionalBlock.FACING) == Direction.DOWN) return down < world.getMaxLightLevel();
            else if (state.getValue(DirectionalBlock.FACING) == Direction.NORTH) return north < world
                    .getMaxLightLevel();
            else if (state.getValue(DirectionalBlock.FACING) == Direction.SOUTH) return south < world
                    .getMaxLightLevel();
            else if (state.getValue(DirectionalBlock.FACING) == Direction.EAST) return east < world.getMaxLightLevel();
            else return west < world.getMaxLightLevel();
        }
    }

    @Override
    public void randomTick(final BlockState state, final ServerLevel world, final BlockPos pos, final Random random)
    {
        if (!GrassDistorticBlock.canBeGrass(state, world, pos)) world.setBlockAndUpdate(pos, BlockInit.DISTORTIC_STONE
                .get().defaultBlockState());
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public void performBonemeal(final ServerLevel world, final Random random, final BlockPos pos,
            final BlockState state)
    {
        final BlockState blockstate1 = Blocks.GRASS.defaultBlockState();
        final BlockState blockstate = world.getBlockState(pos);
        final BlockPos blockpos = pos.above();
        if (blockstate.is(BlockInit.DISTORTIC_GRASS.get()))
        {
            NetherForestVegetationFeature.place(world, random, blockpos, Features.Configs.CRIMSON_FOREST_CONFIG, 3, 1);
            NetherForestVegetationFeature.place(world, random, blockpos, Features.Configs.WARPED_FOREST_CONFIG, 3, 1);
            NetherForestVegetationFeature.place(world, random, blockpos, Features.Configs.NETHER_SPROUTS_CONFIG, 3, 1);
        }

        label48:
        for (int i = 0; i < 128; ++i)
        {
            BlockPos blockpos1 = blockpos;

            for (int i2 = 0; i2 < i / 16; ++i2)
            {
                blockpos1 = blockpos1.offset(random.nextInt(3) - 1, (random.nextInt(3) - 1) * random.nextInt(3) / 2,
                        random.nextInt(3) - 1);
                if (!world.getBlockState(blockpos1.below()).is(this) || world.getBlockState(blockpos1)
                        .isCollisionShapeFullBlock(world, blockpos1)) continue label48;
            }

            final BlockState blockstate2 = world.getBlockState(blockpos1);
            if (blockstate2.is(blockstate1.getBlock()) && random.nextInt(10) == 0) ((BonemealableBlock) blockstate1
                    .getBlock()).performBonemeal(world, random, blockpos1, blockstate2);

            if (blockstate2.isAir())
            {
                BlockState blockstate3;
                if (random.nextInt(8) == 0)
                {
                    final List<ConfiguredFeature<?, ?>> features = world.getBiome(blockpos1).getGenerationSettings()
                            .getFlowerFeatures();
                    if (features.isEmpty()) continue;

                    final ConfiguredFeature<?, ?> features2 = features.get(0);
                    final AbstractFlowerFeature flowers = (AbstractFlowerFeature) features2.feature;
                    blockstate3 = flowers.getRandomFlower(random, blockpos1, features2.config());
                }
                else blockstate3 = blockstate1;

                if (blockstate3.canSurvive(world, blockpos1)) world.setBlock(blockpos1, blockstate3, 3);
            }
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
            return false;
        }
    }

    @Override
    public void stepOn(final Level world, final BlockPos pos, final BlockState state, final Entity entity)
    {
        super.stepOn(world, pos, state, entity);
        GrassDistorticBlock.executeProcedure(entity);

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
            ((LivingEntity) entity).addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 120, 2));
    }
}
