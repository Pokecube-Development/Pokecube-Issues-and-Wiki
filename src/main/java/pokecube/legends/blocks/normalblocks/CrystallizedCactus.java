package pokecube.legends.blocks.normalblocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.*;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.PlantType;

import java.util.Random;

public class CrystallizedCactus extends Block implements IPlantable
{  
	protected static final VoxelShape COLLISION_SHAPE = Block.makeCuboidShape(1.0D, 0.0D, 1.0D, 15.0D, 15.0D, 15.0D);
	protected static final VoxelShape OUTLINE_SHAPE = Block.makeCuboidShape(1.0D, 0.0D, 1.0D, 15.0D, 16.0D, 15.0D);
    
	public CrystallizedCactus(final String name, final Properties props)
    {
        super(props);
    }

	@Override
	public boolean shouldDisplayFluidOverlay(final BlockState state, final IBlockDisplayReader world, final BlockPos pos, final FluidState fluidstate) {
		return true;
	}

	public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
	      return COLLISION_SHAPE;
    }

    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
	      return OUTLINE_SHAPE;
    }
	
	public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity entityIn) {
		if ((entityIn instanceof PlayerEntity)) {
			entityIn.attackEntityFrom(DamageSource.CACTUS, 1.0F);
		}
    }

	public PlantType getPlantType(IBlockReader worldIn, BlockPos pos)
	{
		return PlantType.DESERT;
	}

	public BlockState getPlant(IBlockReader worldIn, BlockPos pos)
	{
		return this.getDefaultState();
	}

//	public boolean isValidPosition(BlockState state, IWorldReader world, BlockPos pos)
//	{
//		Iterator var = Direction.Plane.HORIZONTAL.iterator();
//
//		Direction direction;
//		Material material;
//		do {
//			if (!var.hasNext())
//			{
//				BlockState state1 = world.getBlockState(pos.down());
//				return state1.canSustainPlant(world, pos, Direction.UP, this)
//						&& !world.getBlockState(pos.up()).getMaterial().isLiquid();
//			}
//
//			direction = (Direction)var.next();
//			BlockState blockstate = world.getBlockState(pos.offset(direction));
//			material = blockstate.getMaterial();
//		} while(!material.isSolid() && !world.getFluidState(pos.offset(direction)).isTagged(FluidTags.LAVA));
//
//		return false;
//	}

	public void tick(BlockState state, ServerWorld world, BlockPos pos, Random rand) {
		if (world.isAreaLoaded(pos, 1)) {
			if (!state.isValidPosition(world, pos)) {
				world.destroyBlock(pos, true);
			}

		}
	}

//	public BlockState updatePostPlacement(BlockState state1, Direction direction, BlockState state2,
//										  IWorld world, BlockPos pos, BlockPos pos2)
//
//	{
//		if (!state1.isValidPosition(world, pos))
//		{
//			world.getPendingBlockTicks().scheduleTick(pos, this, 1);
//		}
//
//		return super.updatePostPlacement(state1, direction, state2, world, pos, pos2);
//	}

	public BlockState updatePostPlacement(BlockState state1, Direction direction, BlockState state2,
										  IWorld world, BlockPos pos, BlockPos pos2) {
		return !state1.isValidPosition(world, pos) ? Blocks.AIR.getDefaultState() :
				super.updatePostPlacement(state1, direction, state2, world, pos, pos2);
	}

	protected boolean isValidGround(BlockState state, IBlockReader world, BlockPos pos) {
		Block block = state.getBlock();
		return block == BlockTags.SAND;
	}

	public boolean isValidPosition(BlockState state, IWorldReader world, BlockPos pos) {
		BlockPos blockpos = pos.down();
		return this.isValidGround(world.getBlockState(blockpos), world, blockpos);
	}
}