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
import net.minecraft.state.Property;
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

import java.util.Random;

public class GrassCorruptedBlock extends NyliumBlock implements IGrowable
{
    public static final BooleanProperty SNOWY = BlockStateProperties.SNOWY;

    public GrassCorruptedBlock(final String name, final Properties properties)
    {
        super(properties);
        this.registerDefaultState((BlockState)((BlockState)this.stateDefinition.any()).setValue(SNOWY, false));
    }

    public BlockState updateShape(BlockState state, Direction direction, BlockState state1, IWorld world, BlockPos pos, BlockPos pos1)
    {
        return direction != Direction.UP ? super.updateShape(state, direction, state1, world, pos, pos1) :
            (BlockState)state.setValue(SNOWY, state1.is(Blocks.SNOW_BLOCK) || state1.is(Blocks.SNOW));
    }

    public BlockState getStateForPlacement(BlockItemUseContext context)
    {
        BlockState state = context.getLevel().getBlockState(context.getClickedPos().above());
        return (BlockState)this.defaultBlockState().setValue(SNOWY, state.is(Blocks.SNOW_BLOCK) || state.is(Blocks.SNOW));
    }

    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> p_206840_1_)
    {
        p_206840_1_.add(new Property[]{SNOWY});
    }

    private static boolean canBeNylium(BlockState state, IWorldReader world, BlockPos pos)
    {
        BlockPos blockpos = pos.above();
        BlockState blockstate = world.getBlockState(blockpos);
        if (blockstate.is(Blocks.SNOW) && (Integer)blockstate.getValue(SnowBlock.LAYERS) >= 1)
        {
            return true;
        } else if (blockstate.getFluidState().getAmount() == 8)
        {
            return false;
        } else {
            int light = LightEngine.getLightBlockInto(world, state, pos, blockstate, blockpos, Direction.UP, blockstate.getLightBlock(world, blockpos));
            return light < world.getMaxLightLevel();
        }
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random)
    {
        if (!canBeNylium(state, world, pos)) {
            world.setBlockAndUpdate(pos, BlockInit.ULTRA_CORRUPTED_DIRT.get().defaultBlockState());
        }
    }

    @Override
    public void performBonemeal(ServerWorld world, Random random, BlockPos pos, BlockState state)
    {
        BlockState blockstate = world.getBlockState(pos);
        BlockPos blockpos = pos.above();
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
            if (random.nextInt(8) == 0)
            {
                TwistingVineFeature.place(world, random, blockpos, 3, 1, 2);
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
        if (entity instanceof ServerPlayerEntity) {
        	if ((((PlayerEntity) entity).inventory.armor.get(3).getItem() != new ItemStack(ItemInit.ULTRA_HELMET.get(), 1).getItem()) ||
                    (((PlayerEntity) entity).inventory.armor.get(2).getItem() != new ItemStack(ItemInit.ULTRA_CHESTPLATE.get(), 1).getItem()) ||
                    (((PlayerEntity) entity).inventory.armor.get(1).getItem() != new ItemStack(ItemInit.ULTRA_LEGGINGS.get(), 1).getItem()) || 
                    (((PlayerEntity) entity).inventory.armor.get(0).getItem() != new ItemStack(ItemInit.ULTRA_BOOTS.get(), 1).getItem())) 
                {
        	((LivingEntity) entity).addEffect(new EffectInstance(Effects.DIG_SLOWDOWN, 120, 1));
                }
        }
    }
}
