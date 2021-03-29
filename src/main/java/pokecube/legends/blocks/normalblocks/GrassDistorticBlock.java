package pokecube.legends.blocks.normalblocks;

import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.*;
import net.minecraft.world.lighting.LightEngine;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.IPlantable;
import pokecube.legends.init.BlockInit;
import pokecube.legends.init.ItemInit;

import java.util.List;
import java.util.Random;

public class GrassDistorticBlock extends DirectionalBlock implements IGrowable
{
    public static final BooleanProperty SNOWY = BlockStateProperties.SNOWY;

    public GrassDistorticBlock(final AbstractBlock.Properties properties)
    {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(DirectionalBlock.FACING, Direction.UP).setValue(SNOWY, false));
    }

    @Override
    protected void createBlockStateDefinition(final StateContainer.Builder<Block, BlockState> builder)
    {
        builder.add(DirectionalBlock.FACING, SNOWY);
    }

    public BlockState updateShape(BlockState state, Direction direction, BlockState state1, IWorld world, BlockPos pos, BlockPos pos1)
    {
        return direction != Direction.UP ? super.updateShape(state, direction, state1, world, pos, pos1) :
            (BlockState)state.setValue(SNOWY, (state1.is(Blocks.SNOW_BLOCK) || state1.is(Blocks.SNOW)));
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
    public BlockState getStateForPlacement(final BlockItemUseContext context)
    {
        BlockState state = context.getLevel().getBlockState(context.getClickedPos().above());
        return this.defaultBlockState().setValue(DirectionalBlock.FACING, context.getNearestLookingDirection().getOpposite())
            .setValue(SNOWY, (state.is(Blocks.SNOW_BLOCK) || state.is(Blocks.SNOW)));
    }

    public boolean isValidBonemealTarget(IBlockReader block, BlockPos pos, BlockState state, boolean valid) {
        return block.getBlockState(pos.above()).isAir() && state.getValue(FACING) == Direction.UP;
    }

    public boolean isBonemealSuccess(World world, Random random, BlockPos pos, BlockState state) {
        return true;
    }

    private static boolean canBeGrass(BlockState state, IWorldReader world, BlockPos pos)
    {
        BlockPos blockpos = pos.above();
        BlockPos blockpos1 = pos.below();
        BlockPos blockpos2 = pos.north();
        BlockPos blockpos3 = pos.south();
        BlockPos blockpos4 = pos.east();
        BlockPos blockpos5 = pos.west();
        BlockState blockstate = world.getBlockState(blockpos);
        BlockState blockstate1 = world.getBlockState(blockpos1);
        BlockState blockstate2 = world.getBlockState(blockpos2);
        BlockState blockstate3 = world.getBlockState(blockpos3);
        BlockState blockstate4 = world.getBlockState(blockpos4);
        BlockState blockstate5 = world.getBlockState(blockpos5);
        if (blockstate.is(Blocks.SNOW) && (Integer)blockstate.getValue(SnowBlock.LAYERS) >= 1)
        {
            return true;
        } else if (blockstate.getFluidState().getAmount() == 8)
        {
            return false;
        } else {
            int up = LightEngine.getLightBlockInto(world, state, pos, blockstate, blockpos,
                Direction.UP, blockstate.getLightBlock(world, blockpos));
            int down = LightEngine.getLightBlockInto(world, state, pos, blockstate1, blockpos1,
                Direction.DOWN, blockstate1.getLightBlock(world, blockpos1));
            int north = LightEngine.getLightBlockInto(world, state, pos, blockstate2, blockpos2,
                Direction.NORTH, blockstate2.getLightBlock(world, blockpos2));
            int south = LightEngine.getLightBlockInto(world, state, pos, blockstate3, blockpos3,
                Direction.SOUTH, blockstate3.getLightBlock(world, blockpos3));
            int east = LightEngine.getLightBlockInto(world, state, pos, blockstate4, blockpos4,
                Direction.EAST, blockstate4.getLightBlock(world, blockpos4));
            int west = LightEngine.getLightBlockInto(world, state, pos, blockstate5, blockpos5,
                Direction.WEST, blockstate5.getLightBlock(world, blockpos5));
            if (state.getValue(FACING) == Direction.UP) {
                return up < world.getMaxLightLevel();
            } else if (state.getValue(FACING) == Direction.DOWN) {
                return down < world.getMaxLightLevel();
            } else if (state.getValue(FACING) == Direction.NORTH) {
                return north < world.getMaxLightLevel();
            } else if (state.getValue(FACING) == Direction.SOUTH) {
                return south < world.getMaxLightLevel();
            } else if (state.getValue(FACING) == Direction.EAST) {
                return east < world.getMaxLightLevel();
            } else {
                return west < world.getMaxLightLevel();
            }
        }
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random)
    {
        if (!canBeGrass(state, world, pos)) {
            world.setBlockAndUpdate(pos, BlockInit.DISTORTIC_STONE.get().defaultBlockState());
        }
    }

    @Override
    public void performBonemeal(ServerWorld world, Random random, BlockPos pos, BlockState state)
    {
        BlockState blockstate1 = Blocks.GRASS.defaultBlockState();
        BlockState blockstate = world.getBlockState(pos);
        BlockPos blockpos = pos.above();
        if (blockstate.is(BlockInit.DISTORTIC_GRASS.get()))
        {
            NetherVegetationFeature.place(world, random, blockpos, Features.Configs.CRIMSON_FOREST_CONFIG, 3, 1);
            NetherVegetationFeature.place(world, random, blockpos, Features.Configs.WARPED_FOREST_CONFIG, 3, 1);
            NetherVegetationFeature.place(world, random, blockpos, Features.Configs.NETHER_SPROUTS_CONFIG, 3, 1);
        } else if (blockstate.is(BlockInit.ULTRA_CORRUPTED_GRASS.get()))
        {
            NetherVegetationFeature.place(world, random, blockpos, Features.Configs.WARPED_FOREST_CONFIG, 3, 1);
            NetherVegetationFeature.place(world, random, blockpos, Features.Configs.CRIMSON_FOREST_CONFIG, 3, 1);
            NetherVegetationFeature.place(world, random, blockpos, Features.Configs.NETHER_SPROUTS_CONFIG, 3, 1);
            if (random.nextInt(8) == 0)
            {
                TwistingVineFeature.place(world, random, blockpos, 3, 1, 2);
            }
        }

        label48:
        for(int i = 0; i < 128; ++i) {
            BlockPos blockpos1 = blockpos;

            for(int i2 = 0; i2 < i / 16; ++i2) {
                blockpos1 = blockpos1.offset(random.nextInt(3) - 1, (random.nextInt(3) - 1) * random.nextInt(3) / 2, random.nextInt(3) - 1);
                if (!world.getBlockState(blockpos1.below()).is(this) || world.getBlockState(blockpos1).isCollisionShapeFullBlock(world, blockpos1)) {
                    continue label48;
                }
            }

            BlockState blockstate2 = world.getBlockState(blockpos1);
            if (blockstate2.is(blockstate1.getBlock()) && random.nextInt(10) == 0) {
                ((IGrowable)blockstate1.getBlock()).performBonemeal(world, random, blockpos1, blockstate2);
            }

            if (blockstate2.isAir()) {
                BlockState blockstate3;
                if (random.nextInt(8) == 0) {
                    List<ConfiguredFeature<?, ?>> features = world.getBiome(blockpos1).getGenerationSettings().getFlowerFeatures();
                    if (features.isEmpty()) {
                        continue;
                    }

                    ConfiguredFeature<?, ?> features2 = (ConfiguredFeature)features.get(0);
                    FlowersFeature flowers = (FlowersFeature)features2.feature;
                    blockstate3 = flowers.getRandomFlower(random, blockpos1, features2.config());
                } else {
                    blockstate3 = blockstate1;
                }

                if (blockstate3.canSurvive(world, blockpos1)) {
                    world.setBlock(blockpos1, blockstate3, 3);
                }
            }
        }
    }

    @Override
    public boolean canSustainPlant(final BlockState state, final IBlockReader world, final BlockPos pos,
            final Direction direction, final IPlantable plantable)
    {
        return true;
    }

    @Override
    public void stepOn(final World world, final BlockPos pos, final Entity entity)
    {
        super.stepOn(world, pos, entity);
        {
            final java.util.HashMap<String, Object> $_dependencies = new java.util.HashMap<>();
            $_dependencies.put("entity", entity);
            GrassDistorticBlock.executeProcedure($_dependencies);
        }
    }

    public static void executeProcedure(final java.util.HashMap<String, Object> dependencies)
    {
        if (dependencies.get("entity") == null)
        {
            System.err.println("Failed to WalkBlockEffect!");
            return;
        }
        final Entity entity = (Entity) dependencies.get("entity");
        if (entity instanceof ServerPlayerEntity) if (((PlayerEntity) entity).inventory.armor.get(3).getItem() != new ItemStack(
                ItemInit.ULTRA_HELMET.get(), 1).getItem() || ((PlayerEntity) entity).inventory.armor.get(
                        2).getItem() != new ItemStack(ItemInit.ULTRA_CHESTPLATE.get(), 1).getItem()
                || ((PlayerEntity) entity).inventory.armor.get(1).getItem() != new ItemStack(
                        ItemInit.ULTRA_LEGGINGS.get(), 1).getItem()
                || ((PlayerEntity) entity).inventory.armor.get(0).getItem() != new ItemStack(
                        ItemInit.ULTRA_BOOTS.get(), 1).getItem()) ((LivingEntity) entity).addEffect(new EffectInstance(Effects.MOVEMENT_SPEED, 120, 2));
    }
}
