package pokecube.legends.blocks.customblocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.GlassBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

public class MirageGlass extends GlassBlock
{  
	protected static final VoxelShape OUTLINE_SHAPE = Block.makeCuboidShape(0D, 0D, 0D, 16D, 16D, 16D);
	   
    public MirageGlass(final String name, final Properties props)
    {
        super(props);
    }

	@Override
	public boolean shouldDisplayFluidOverlay(final BlockState state, final IBlockDisplayReader world, final BlockPos pos, final FluidState fluidstate) {
		return true;
	}

    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
	      return OUTLINE_SHAPE;
    }
	
	public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity entityIn) {
		if ((entityIn instanceof PlayerEntity)) {
			entityIn.attackEntityFrom(DamageSource.CACTUS, 1.0F);
		}
    }

    @Override
    public float[] getBeaconColorMultiplier(BlockState state, IWorldReader world, BlockPos pos, BlockPos beaconPos) {
        return new float[]{0.00f, 0.95f, 1.00f};
    }
}