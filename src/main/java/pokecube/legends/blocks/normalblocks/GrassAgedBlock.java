package pokecube.legends.blocks.normalblocks;

import java.util.Random;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.GrassBlock;
import net.minecraft.block.IGrowable;
import net.minecraft.block.SnowBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.lighting.LightEngine;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.IPlantable;
import pokecube.legends.init.BlockInit;
import pokecube.legends.init.ItemInit;

public class GrassAgedBlock extends GrassBlock implements IGrowable
{
    public GrassAgedBlock(final Properties properties)
    {
        super(properties);
        this.registerDefaultState((BlockState)((BlockState)this.stateDefinition.any()).setValue(SNOWY, false));
    }

    @Override
    public boolean canSustainPlant(final BlockState state, final IBlockReader world, final BlockPos pos,
                                   final Direction direction, final IPlantable plantable)
    {
        return true;
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {

        if (!canBeGrass(state, world, pos))
        {
            if (!world.isAreaLoaded(pos, 3))
            {
                return;
            }

            world.setBlockAndUpdate(pos, BlockInit.AGED_DIRT.get().defaultBlockState());
        } else if (world.getMaxLocalRawBrightness(pos.above()) >= 9)
        {
            BlockState blockstate = this.defaultBlockState();

            for(int i = 0; i < 4; ++i)
            {
                BlockPos blockpos = pos.offset(random.nextInt(3) - 1,
                    random.nextInt(5) - 3, random.nextInt(3) - 1);
                if (world.getBlockState(blockpos).is(BlockInit.AGED_DIRT.get()) &&
                    canPropagate(blockstate, world, blockpos))
                {
                    world.setBlockAndUpdate(blockpos, (BlockState)blockstate
                        .setValue(SNOWY, world.getBlockState(blockpos.above()).is(Blocks.SNOW)));
                }
            }
        }
    }

    public static boolean canPropagate(BlockState state, IWorldReader world, BlockPos pos)
    {
        BlockPos blockpos = pos.above();
        return canBeGrass(state, world, pos) && !world.getFluidState(blockpos).is(FluidTags.WATER);
    }

    public static boolean canBeGrass(BlockState state, IWorldReader world, BlockPos pos)
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
            int i = LightEngine.getLightBlockInto(world, state, pos, blockstate, blockpos, Direction.UP,
                blockstate.getLightBlock(world, blockpos));
            return i < world.getMaxLightLevel();
        }
    }

    @Override
    public void stepOn(final World world, final BlockPos pos, final Entity entity)
    {
        super.stepOn(world, pos, entity);
        {
            final java.util.HashMap<String, Object> $_dependencies = new java.util.HashMap<>();
            $_dependencies.put("entity", entity);
            GrassAgedBlock.executeProcedure($_dependencies);
        }
    }

    public static void executeProcedure(final java.util.HashMap<String, Object> dependencies)
    {
        if (dependencies.get("entity") == null)
        {
            System.err.println("Failed to WalkGrassEffect!");
            return;
        }
        final Entity entity = (Entity) dependencies.get("entity");
        if (entity instanceof ServerPlayerEntity) {
        	if ((((PlayerEntity) entity).inventory.armor.get(3).getItem() != new ItemStack(ItemInit.ULTRA_HELMET.get(), 1).getItem()) ||
                    (((PlayerEntity) entity).inventory.armor.get(2).getItem() != new ItemStack(ItemInit.ULTRA_CHESTPLATE.get(), 1).getItem()) ||
                    (((PlayerEntity) entity).inventory.armor.get(1).getItem() != new ItemStack(ItemInit.ULTRA_LEGGINGS.get(), 1).getItem()) || 
                    (((PlayerEntity) entity).inventory.armor.get(0).getItem() != new ItemStack(ItemInit.ULTRA_BOOTS.get(), 1).getItem())) 
                {
        	((LivingEntity) entity).addEffect(new EffectInstance(Effects.WITHER, 120, 1));
                }
        }
    }
}
