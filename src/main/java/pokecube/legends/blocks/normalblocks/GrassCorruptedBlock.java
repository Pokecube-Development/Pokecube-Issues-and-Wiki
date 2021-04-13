package pokecube.legends.blocks.normalblocks;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.IGrowable;
import net.minecraft.block.NyliumBlock;
import net.minecraft.block.SnowBlock;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.Features;
import net.minecraft.world.gen.feature.NetherVegetationFeature;
import net.minecraft.world.gen.feature.TwistingVineFeature;
import net.minecraft.world.lighting.LightEngine;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.IPlantable;
import pokecube.legends.init.BlockInit;
import pokecube.legends.init.ItemInit;

public class GrassCorruptedBlock extends NyliumBlock implements IGrowable
{
    public static final BooleanProperty SNOWY = BlockStateProperties.SNOWY;

    public GrassCorruptedBlock(final Properties properties)
    {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(GrassCorruptedBlock.SNOWY, false));
    }

    @SuppressWarnings("deprecation")
    @Override
    public BlockState updateShape(final BlockState state, final Direction direction, final BlockState state1, final IWorld world, final BlockPos pos, final BlockPos pos1)
    {
        return direction != Direction.UP ? super.updateShape(state, direction, state1, world, pos, pos1) :
            (BlockState)state.setValue(GrassCorruptedBlock.SNOWY, state1.is(Blocks.SNOW_BLOCK) || state1.is(Blocks.SNOW));
    }

    @Override
    public BlockState getStateForPlacement(final BlockItemUseContext context)
    {
        final BlockState state = context.getLevel().getBlockState(context.getClickedPos().above());
        return this.defaultBlockState().setValue(GrassCorruptedBlock.SNOWY, state.is(Blocks.SNOW_BLOCK) || state.is(Blocks.SNOW));
    }

    @Override
    protected void createBlockStateDefinition(final StateContainer.Builder<Block, BlockState> builder)
    {
        builder.add(GrassCorruptedBlock.SNOWY);
    }

    private static boolean canBeGrass(final BlockState state, final IWorldReader world, final BlockPos pos)
    {
        final BlockPos blockpos = pos.above();
        final BlockState blockstate = world.getBlockState(blockpos);
        if (blockstate.is(Blocks.SNOW) && blockstate.getValue(SnowBlock.LAYERS) >= 1) return true;
        else if (blockstate.getFluidState().getAmount() == 8) return false;
        else {
            final int light = LightEngine.getLightBlockInto(world, state, pos, blockstate, blockpos, Direction.UP, blockstate.getLightBlock(world, blockpos));
            return light < world.getMaxLightLevel();
        }
    }

    @Override
    public void randomTick(final BlockState state, final ServerWorld world, final BlockPos pos, final Random random)
    {
        if (!GrassCorruptedBlock.canBeGrass(state, world, pos)) world.setBlockAndUpdate(pos, BlockInit.ULTRA_CORRUPTED_DIRT.get().defaultBlockState());
    }

    @Override
    public void performBonemeal(final ServerWorld world, final Random random, final BlockPos pos, final BlockState state)
    {
        final BlockState blockstate = world.getBlockState(pos);
        final BlockPos blockpos = pos.above();
        if (blockstate.is(BlockInit.ULTRA_CORRUPTED_GRASS.get()))
        {
            NetherVegetationFeature.place(world, random, blockpos, Features.Configs.CRIMSON_FOREST_CONFIG, 3, 1);
            NetherVegetationFeature.place(world, random, blockpos, Features.Configs.WARPED_FOREST_CONFIG, 3, 1);
            NetherVegetationFeature.place(world, random, blockpos, Features.Configs.NETHER_SPROUTS_CONFIG, 3, 1);
        } else if (blockstate.is(BlockInit.DISTORTIC_GRASS.get()))
        {
            NetherVegetationFeature.place(world, random, blockpos, Features.Configs.WARPED_FOREST_CONFIG, 3, 1);
            NetherVegetationFeature.place(world, random, blockpos, Features.Configs.CRIMSON_FOREST_CONFIG, 3, 1);
            NetherVegetationFeature.place(world, random, blockpos, Features.Configs.NETHER_SPROUTS_CONFIG, 3, 1);
            if (random.nextInt(8) == 0) TwistingVineFeature.place(world, random, blockpos, 3, 1, 2);
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
            GrassCorruptedBlock.executeProcedure($_dependencies);
        }
    }

    public static void executeProcedure(final java.util.HashMap<String, Object> dependencies)
    {
        if (dependencies.get("entity") == null)
        {
            System.err.println("Failed to WalkEffect!");
            return;
        }
        final Entity entity = (Entity) dependencies.get("entity");
        if (entity instanceof ServerPlayerEntity) if (((PlayerEntity) entity).inventory.armor.get(3).getItem() != new ItemStack(ItemInit.ULTRA_HELMET.get(), 1).getItem() ||
                ((PlayerEntity) entity).inventory.armor.get(2).getItem() != new ItemStack(ItemInit.ULTRA_CHESTPLATE.get(), 1).getItem() ||
                ((PlayerEntity) entity).inventory.armor.get(1).getItem() != new ItemStack(ItemInit.ULTRA_LEGGINGS.get(), 1).getItem() ||
                ((PlayerEntity) entity).inventory.armor.get(0).getItem() != new ItemStack(ItemInit.ULTRA_BOOTS.get(), 1).getItem()) ((LivingEntity) entity).addEffect(new EffectInstance(Effects.DIG_SLOWDOWN, 120, 1));
    }
}
