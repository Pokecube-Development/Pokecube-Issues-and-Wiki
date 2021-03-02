package pokecube.legends.blocks.normalblocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class CrystallizedCactus extends Block
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
}