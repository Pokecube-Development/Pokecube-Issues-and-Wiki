package pokecube.legends.blocks.normalblocks;

import java.util.Random;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class InfectedTorchWall extends WallTorchBlock
{	
    public InfectedTorchWall() {
    	super(Properties.of(Material.DECORATION).strength(0.0f).noCollission(),
    			ParticleTypes.CAMPFIRE_COSY_SMOKE);
	}
    
    @OnlyIn(Dist.CLIENT)
    public void animateTick(BlockState stateIn, Level worldIn, BlockPos pos, Random rand) {
       Direction direction = stateIn.getValue(FACING);
       double d0 = (double)pos.getX() + 0.5D;
       double d1 = (double)pos.getY() + 0.7D;
       double d2 = (double)pos.getZ() + 0.5D;
       Direction direction1 = direction.getOpposite();
       worldIn.addParticle(ParticleTypes.DRAGON_BREATH, d0 + 0.27D * (double)direction1.getStepX(), d1 + 0.22D, d2 + 0.27D * (double)direction1.getStepZ(), 0.0D, 0.0D, 0.0D);
    }
}
